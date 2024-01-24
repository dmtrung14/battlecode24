package defaultplayer;

import battlecode.common.*;
import defaultplayer.util.ZoneInfo;

import java.util.*;

import static defaultplayer.Constants.*;
import static defaultplayer.util.CheckWrapper.*;
import static defaultplayer.util.Optimizer.*;


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
        } else {
            int randomZone = rand.nextInt(27);
            for (int i = 27; i >= 1; i--) {
                if (rc.canSpawn(SPAWN_ZONES[(randomZone + i) % 27])) {
                    rc.spawn(SPAWN_ZONES[(randomZone + i) % 27]);
                }
            }
        }
    }

    public void initializeStatic() throws GameActionException {
        Comms.init(rc);
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        myID = Comms.incrementAndGetId(rc);
        SPAWN_ZONES = rc.getAllySpawnLocations();
        SPAWN_ZONE_CENTERS = new MapLocation[3];
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
            dirLoop: for (Direction d : dirs) {
                MapLocation loc = rc.adjacentLocation(d);
                for (MapInfo neighbor : rc.senseNearbyMapInfos(loc, 2)) {
                    if (neighbor.isDam()) continue dirLoop;
                }
                // check if current flag location is at least 6 from both the other 2 flags
                for (int j = 0; j < 3; j++) {
                    if (j + 1 != Constants.myID && loc.distanceSquaredTo(Comms.getFlagLocation(rc, rc.getTeam(), j)) < 36){
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
        MapLocation flagLoc = rc.getLocation();
        MapInfo[] aroundFlag = rc.senseNearbyMapInfos(flagLoc, 2);
        for (MapInfo a : aroundFlag) {
            if (a.isPassable() && !a.getMapLocation().equals(flagLoc)) {
                MapLocation loc = a.getMapLocation();
                if ((loc.x + loc.y) % 2 == 1) {
                    builder.waitAndBuildTrapTurn(TrapType.STUN, a.getMapLocation(), 1);
                } else {
                    builder.waitAndBuildTrapTurn(TrapType.EXPLOSIVE, a.getMapLocation(), 1);
                }
            }
        }
        afterBuildTrap = true;
    }

    public void digLand(int radius) throws GameActionException {
        if (!rc.isSpawned()) return;
        Direction[] dirs = Direction.allDirections();
        Direction dir = dirs[rand.nextInt(dirs.length)];
        MapLocation myFlag = ALLY_FLAGS[myID - 1];
        while (rc.canMove(dir) && rc.getLocation().distanceSquaredTo(myFlag) <= radius) {
            rc.move(dir);
            for (Direction d : dirs) {
                MapLocation site = rc.adjacentLocation(d);
                if (rc.canDig(site)
                        && (site.x + site.y) % 2 == 0) {
                    rc.dig(site);
                }
//                else if (rc.canBuild(TrapType.EXPLOSIVE, site)) {
//                    builder.waitAndBuildTrapTurn(TrapType.EXPLOSIVE, site, 2);
//                }
            }
        }
        if (rc.getLocation().distanceSquaredTo(myFlag) > radius) {
            Pathfind.moveToward(rc, myFlag, false);
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
        if (!rc.isSpawned()) {
            trySpawn();
            if (!rc.isSpawned()) return;
        }
        if (isBuilder()) {
            if (rc.getRoundNum() <= FLAG_RUSH_ROUNDS) {
                moveFlag();
            }
            if (!afterBuildTrap) {
                buildAroundFlags();
            }
            digLand(40);
            Pathfind.moveToward(rc, ALLY_FLAGS[myID - 1], false);
        } else if (4 <= myID && myID <= 9) {
            ALLY_FLAGS = Comms.getAllyFlagLocations(rc);
            MapLocation myFlag = ALLY_FLAGS[(myID - 4) / 2];
            Pathfind.moveToward(rc, myFlag, true);
            if (rc.getRoundNum() > FLAG_RUSH_ROUNDS) digLand2(myFlag);
        } else if (isExplorer()) {
            if (rc.getRoundNum() <= 50) Pathfind.explore(rc);
            else if(rc.getRoundNum() <= EXPLORE_ROUNDS) Pathfind.exploreDVD(rc);
            else if (!isNearDam(rc)) {
                MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
                Pathfind.moveToward(rc, center, true);
            } else if (rc.canBuild(TrapType.STUN, rc.getLocation())) {
                rc.build(TrapType.STUN, rc.getLocation());
            }
        }
    }
}