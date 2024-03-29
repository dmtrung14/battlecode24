package defaultplayer_intlqualifier;

import battlecode.common.*;
import defaultplayer_intlqualifier.util.ZoneInfo;

import java.util.*;

import static defaultplayer_intlqualifier.Constants.*;
import static defaultplayer_intlqualifier.util.CheckWrapper.*;
import static defaultplayer_intlqualifier.util.Optimizer.nearestSpawnZone;


public class Setup {
    private final RobotController rc;
    private final Builder builder;
    private final Random rand;
    private boolean afterBuildTrap = false;

    public Setup(RobotController rc) {
        this.rc = rc;
        this.builder = new Builder(rc);
        this.rand = new Random(rc.getID());
    }

    public void trySpawn() throws GameActionException {
        if (isBuilder()) {
            MapLocation spawnPosition = SPAWN_ZONE_CENTERS[myID - 1];
            if (rc.canSpawn(spawnPosition)) rc.spawn(spawnPosition);
        } else if(MY_LAST_LOCATION == null){
            int randomZone = rand.nextInt(27);
            for (int i = 27; i >= 1; i--) {
                if (rc.canSpawn(SPAWN_ZONES[(randomZone + i) % 27])) {
                    rc.spawn(SPAWN_ZONES[(randomZone + i) % 27]);
                }
            }
        } else {
            MapLocation spawnAt = nearestSpawnZone(rc, MY_LAST_LOCATION);

            if (rc.canSpawn(spawnAt)){
                rc.spawn(spawnAt);
            } else {
                for (Direction d : DIRECTIONS ) {
                    MapLocation spawnZone = spawnAt.add(d);
                    if (rc.canSpawn(spawnZone)) rc.spawn(spawnZone);
                }
            }
        }
    }

    public void initializeStatic() throws GameActionException {
        Comms.init(rc);
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        ALLY = rc.getTeam();
        OPPONENT = rc.getTeam().opponent();
        SPAWN_ZONES = rc.getAllySpawnLocations();
        SPAWN_ZONE_CENTERS = new MapLocation[3];
        EXPLORE_ROUNDS = 199 - (mapWidth + mapHeight) /2;
        int numCenters = 0;
        for (MapLocation loc : SPAWN_ZONES) {
            boolean isCenter = true;
            for (Direction dir : Direction.cardinalDirections()) {
                if (!contains(SPAWN_ZONES, loc.add(dir))) {
                    isCenter = false;
                    break;
                }
            }
            if (isCenter) {
                SPAWN_ZONE_CENTERS[numCenters] = loc;
                numCenters++;
            }
        }
        RANDOM = new Random(myID);
        for (int i = 0; i < 100; i++) ZONE_INFO[i] = new ZoneInfo();
    }

    public void initializeTurnQueue() throws GameActionException {
        Comms.postTurnQueue(rc);
        Clock.yield();
        TURN_QUEUE = Comms.getTurnQueue(rc);
        if (rc.getID() == TURN_QUEUE[49]) Comms.clear(rc);
        Clock.yield();
    }

    public void moveFlag() throws GameActionException {
        MapLocation flag = rc.senseNearbyFlags(2, rc.getTeam())[0].getLocation();
        if (rc.canPickupFlag(flag)) {
            rc.pickupFlag(flag);
        }
        MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
        while (rc.getRoundNum() <= FLAG_RUSH_ROUNDS) {
            while (!rc.isMovementReady()) Clock.yield();
            Direction dir = rc.getLocation().directionTo(center).opposite();
            int leftDist = rc.adjacentLocation(dir.rotateLeft().rotateLeft()).distanceSquaredTo(center);
            int rightDist = rc.adjacentLocation(dir.rotateRight().rotateRight()).distanceSquaredTo(center);
            Direction[] dirs = {
                    dir,
                    dir.rotateLeft(),
                    dir.rotateRight(),
                    leftDist < rightDist ? dir.rotateLeft().rotateLeft() : dir.rotateRight().rotateRight()
            };
            dirLoop:
            for (Direction d : dirs) {
                MapLocation loc = rc.adjacentLocation(d);
                for (MapInfo neighbor : rc.senseNearbyMapInfos(loc, 2)) {
                    if (neighbor.isDam()) continue dirLoop;
                }
                // check if current flag location is at least 6 from both the other 2 flags
                for (int j = 0; j < 3; j++) {
                    if (j + 1 != Constants.myID && loc.distanceSquaredTo(Comms.getFlagLocation(rc, rc.getTeam(), j)) < 36) {
                        continue dirLoop;
                    }
                }
                if (rc.canMove(d)) {
                    rc.move(d);
                    Comms.setFlagLocation(rc, rc.getTeam(), myID - 1, rc.getLocation());
                    ALLY_FLAGS[myID - 1] = rc.getLocation();
                }
            }
        }
        while (rc.hasFlag()) {
            if (rc.canDropFlag(rc.getLocation())) {
                rc.dropFlag(rc.getLocation());
            }
            Clock.yield();
        }
    }

    public void buildAroundFlags() throws GameActionException {
        MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
        Direction dir = rc.getLocation().directionTo(center);
        Direction dir1 = dir.rotateRight();
        Direction dir2 = dir.rotateLeft();
        if (rc.canBuild(TrapType.WATER, rc.getLocation().add(dir))) {
            rc.build(TrapType.WATER, rc.getLocation().add(dir));
        }
        if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation().add(dir1))) {
            rc.build(TrapType.EXPLOSIVE, rc.getLocation().add(dir1));
        }
        if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation().add(dir2))) {
            rc.build(TrapType.EXPLOSIVE, rc.getLocation().add(dir2));
        }
        afterBuildTrap = true;
    }

    public void digLand() throws GameActionException {
        if (!rc.isSpawned()) return;
        MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
        Direction dirToCenter = ALLY_FLAGS[myID - 1].directionTo(center);
        Direction[] dirSet = { dirToCenter, dirToCenter.rotateRight(), dirToCenter.rotateLeft() };
        Direction[] dirs = Direction.allDirections();
        Direction dir = dirSet[rand.nextInt(3)];
        MapLocation myFlag = ALLY_FLAGS[myID - 1];
        if (rc.canMove(dir) && contains(dirSet, myFlag.directionTo(rc.adjacentLocation(dir)))) {
            rc.move(dir);
            for (Direction d : dirs) {
                MapLocation site = rc.adjacentLocation(d);
                if (rc.canDig(site)
                        && (site.x + site.y) % 2 == 0) {
                    rc.dig(site);
                }
            }
        }
    }

    public void digLand2(MapLocation flag) throws GameActionException {
        if (!rc.isSpawned()) return;
        Direction[] dirs = Direction.allDirections();
        Direction dir = dirs[rand.nextInt(dirs.length)];
        while (rc.canMove(dir)) {
            rc.move(dir);
            for (Direction d : dirs) {
                MapLocation site = rc.getLocation().add(d);
                if (rc.canDig(site)
                        && site.distanceSquaredTo(flag) > 2
                        && site.distanceSquaredTo(flag) < 50) {
                    rc.dig(site);
                }
            }
        }
    }

    public void run() throws GameActionException {
        while (!rc.isSpawned()) {
            trySpawn();
        }
        if (isBuilder()) {
            if (rc.getRoundNum() <= FLAG_RUSH_ROUNDS) moveFlag();
            Pathfind.moveToward(rc, ALLY_FLAGS[myID - 1], false);
            buildAroundFlags();
        } else if (isExplorer()) {
            if (rc.getRoundNum() <= 30) {
                Pathfind.explore(rc, rc.senseNearbyRobots(-1, rc.getTeam()));
            }
            else if(rc.getRoundNum() <= EXPLORE_ROUNDS) Pathfind.exploreDVD(rc);
            else if (!isNearDam(rc)) {
                MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
//                Pathfind.moveToward(rc, !BORDERLINE.isEmpty() ? BORDERLINE.get(myID * 100 % BORDERLINE.size()) : center, true);
                Pathfind.moveToward(rc, !ENEMY_BORDER_LINE.isEmpty() ?
                        ENEMY_BORDER_LINE.get(myID % ENEMY_BORDER_LINE.size()) :
                        (!NEUTRAL_BORDERLINE.isEmpty() ? NEUTRAL_BORDERLINE.get(myID % NEUTRAL_BORDERLINE.size()) : center), true);
            } else if (rc.canBuild(TrapType.STUN, rc.getLocation()) && isNearEnemyTerritory(rc)) {
                MapLocation loc = rc.getLocation();
                if ((loc.x + loc.y) % 2 == 0) rc.build(TrapType.STUN, rc.getLocation());
            }
        }
    }
}