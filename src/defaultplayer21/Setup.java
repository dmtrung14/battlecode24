package defaultplayer21;

import battlecode.common.*;
import defaultplayer21.util.ZoneInfo;

import java.util.*;

import static defaultplayer21.Constants.*;
import static defaultplayer21.util.CheckWrapper.*;


public class Setup {
    private final RobotController rc;
    private final Builder builder;
    private final Random rand;
    private boolean afterBuildTrap = false;
    private MapLocation ourFlag;

    public Setup(RobotController rc) {
        this.rc = rc;
        this.builder = new Builder(rc);
        this.rand = new Random(rc.getID());
    }

    public void spawn() throws GameActionException {
        while (!rc.isSpawned()) {
            if (isBuilder()) {
                MapLocation spawnPosition = SPAWN_ZONES[9 * (myID - 1) + 4];
                if (rc.canSpawn(spawnPosition)) rc.spawn(spawnPosition);
            } else {
                int randomZone = rand.nextInt(27);
                for (int i = 27; i >= 1; i--) {
                    if (rc.canSpawn(Constants.SPAWN_ZONES[(randomZone + i) % 27])) {
                        rc.spawn(Constants.SPAWN_ZONES[(randomZone + i) % 27]);
                    }
                }
            }

        }
    }

    public void initializeStatic() throws GameActionException {
        Comms.init(rc);
        Constants.mapWidth = rc.getMapWidth();
        Constants.mapHeight = rc.getMapHeight();
        Constants.myID = Comms.incrementAndGetId(rc);
        Constants.SPAWN_ZONES = rc.getAllySpawnLocations();
        Constants.RANDOM = new Random(Constants.myID);
        for (int i = 0; i < 100; i++) Constants.ZONE_INFO[i] = new ZoneInfo();
    }

    public void moveToGoal() throws GameActionException {
        if (Constants.hasMovedFlag) return;
        MapLocation flag = rc.senseNearbyFlags(-1, rc.getTeam())[0].getLocation();
        if (rc.canPickupFlag(flag)) {
            rc.pickupFlag(flag);
            Constants.hasMovedFlag = true;
        }
        MapLocation center = new MapLocation(Constants.mapWidth / 2, Constants.mapHeight / 2);
        Queue<Integer> pastDistance = new LinkedList<>();
        pastDistance.add(rc.getLocation().distanceSquaredTo(center));
        MapLocation[] nextLocation = Pathfind.avoid(rc, center, pastDistance.remove());
        while (nextLocation.length > 0 && rc.getRoundNum() <= Constants.FLAG_RUSH_ROUNDS) {
            pastDistance.add(rc.getLocation().distanceSquaredTo(center));
            for (int i = 0; i < nextLocation.length; i++) {
                Direction dir = rc.getLocation().directionTo(nextLocation[i]);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    break;
                } else if (rc.canFill(nextLocation[i])) {
                    rc.dropFlag(rc.getLocation());
                    rc.fill(nextLocation[i]);
                    rc.pickupFlag(rc.getLocation());
                    rc.move(dir);
                    break;
                }
                if (i == (nextLocation.length - 1) && rc.getRoundNum() < Constants.FLAG_RUSH_ROUNDS) {
                    i = -1;
                    Clock.yield();
                }
            }
            Comms.setFlagLocation(rc, rc.getTeam(), Constants.myID - 1, rc.getLocation());
            nextLocation = Pathfind.avoid(rc, center, pastDistance.remove());
        }
        while (rc.hasFlag()) {
            if (rc.canDropFlag(rc.getLocation())) {
                rc.dropFlag(rc.getLocation());
                ourFlag = rc.getLocation();
            }
            Clock.yield();
        }
    }

    public void moveToCenter() throws GameActionException {
        MapLocation center = new MapLocation(Constants.mapWidth / 2, Constants.mapHeight / 2);
        Queue<Integer> pastDistance = new LinkedList<>();
        pastDistance.add(rc.getLocation().distanceSquaredTo(center));
        Direction lastDir = Direction.CENTER;
        MapLocation[] nextLocation = Pathfind.attract(rc, center, pastDistance.remove());
        while ((nextLocation.length > 0 || !isNearDam(rc)) && rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) {
            pastDistance.add(rc.getLocation().distanceSquaredTo(center));

            if (nextLocation.length > 0) {
                for (int i = 0; i < nextLocation.length; i++) {
                    Direction dir = rc.getLocation().directionTo(nextLocation[i]);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                        lastDir = dir;
                        break;
                    }
                    if (i == (nextLocation.length - 1) && rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
                        i = -1;
                        Clock.yield();
                    }
                }
                nextLocation = Pathfind.attract(rc, center, pastDistance.remove());
            } else {
                if (rc.canMove(lastDir)) rc.move(lastDir);
                else if (rc.canMove(lastDir.rotateLeft())) {
                    rc.move(lastDir.rotateLeft());
                    lastDir = lastDir.rotateLeft();
                } else if (rc.canMove(lastDir.rotateRight())) {
                    rc.move(lastDir.rotateRight());
                    lastDir = lastDir.rotateRight();
                }
            }
        }
    }

    public void buildAroundFlags() throws GameActionException {
        MapLocation flagLoc = rc.getLocation();
        MapInfo[] aroundFlag = rc.senseNearbyMapInfos(flagLoc, 5);
        for (MapInfo a : aroundFlag) {
            if (a.isPassable() && !a.getMapLocation().equals(flagLoc)) {
                if ((a.getMapLocation().x + a.getMapLocation().y) % 2 == 1) {
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
        while (rc.canMove(dir) && rc.getLocation().distanceSquaredTo(ourFlag) <= radius) {
            rc.move(dir);
            for (Direction d : dirs) {
                MapLocation site = rc.getLocation().add(d);
                if (rc.canDig(site)
                        && (site.x + site.y) % 2 == 0) {
                    rc.dig(site);
                } else if (rc.canBuild(TrapType.WATER, site)) {
                    builder.waitAndBuildTrapTurn(TrapType.WATER, site, 2);
                }
            }
        }
        if (rc.getLocation().distanceSquaredTo(ourFlag) > 20) {
            dir = rc.getLocation().directionTo(ourFlag);
            if (rc.canMove(dir)) {
                rc.move(dir);
            } else if (rc.canMove(dir.rotateRight())) {
                rc.move(dir.rotateRight());
            } else if (rc.canMove(dir.rotateLeft())) {
                rc.move(dir.rotateLeft());
            }
        }
    }

    public void backFlagLoc() throws GameActionException {
        if (!isBuilder() || !rc.isSpawned()) return;
        MapLocation flagLoc = Constants.ALLY_FLAGS[Constants.myID - 1];
        while (rc.isSpawned() && !rc.getLocation().equals(flagLoc)) {
            Pathfind.moveToward(rc, flagLoc, false);
            Clock.yield();
        }
    }

    public void run() throws GameActionException {
        if (!rc.isSpawned()) {
            spawn();
        }
        if (isBuilder()) {
            if (rc.getRoundNum() <= Constants.FLAG_RUSH_ROUNDS) {
                moveToGoal();
            }
            for (int i = 0; i < 3; i++) {
                Constants.ALLY_FLAGS[i] = Comms.getFlagLocation(rc, rc.getTeam(), i);
            }
            if (!afterBuildTrap) {
                buildAroundFlags();
            }
            digLand(100);
            Pathfind.moveToward(rc, Constants.ALLY_FLAGS[Constants.myID - 1], false);
        } else if (isExplorer()) {
            if (rc.getRoundNum() <= Constants.EXPLORE_ROUNDS) Pathfind.explore(rc);
            else {
                moveToCenter();
            }
        }
    }
}