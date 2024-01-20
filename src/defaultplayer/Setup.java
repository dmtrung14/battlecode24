package defaultplayer;

import battlecode.common.*;

import java.util.*;

import static defaultplayer.util.CheckWrapper.*;

public class Setup {

    private final RobotController rc;
    private final Builder builder;
    private final Random rand;
    private boolean afterBuildTrap = false;
    private final MapLocation[] ourFlag = new MapLocation[4];

    public Setup(RobotController rc) {
        this.rc = rc;
        this.builder = new Builder(rc);
        this.rand = new Random(rc.getID());
    }

    public void spawn() throws GameActionException {
        while (!rc.isSpawned()) {
            if (isBuilder()) {
                MapLocation spawnZone = Constants.SPAWN_ZONES[9 * (Constants.myID - 1) + 4];
                if (rc.canSpawn(spawnZone)) {
                    rc.spawn(spawnZone);
                    return;
                }
            } else {
                int randomZone = rand.nextInt(27);
                for (int i = 27; i >= 1; i--) {
                    if (rc.canSpawn(Constants.SPAWN_ZONES[(randomZone + i) % 27])) {
                        rc.spawn(Constants.SPAWN_ZONES[(randomZone + i) % 27]);
                        return;
                    }
                }
            }
            Clock.yield();
        }
    }

    public void moveToGoal() throws GameActionException {
        MapLocation flag = rc.senseNearbyFlags(-1, rc.getTeam())[0].getLocation();
        if (Constants.HAS_MOVED_FLAG) return;
        if (rc.canPickupFlag(flag)) {
            rc.pickupFlag(flag);
            Constants.HAS_MOVED_FLAG = true;
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
                ourFlag[Constants.myID] = rc.getLocation();
            }
            Clock.yield();
        }
    }


    public void buildAroundFlags() throws GameActionException {
        MapLocation flagLoc = rc.getLocation();
        MapInfo[] around_flag = rc.senseNearbyMapInfos(flagLoc, 2);
        for (MapInfo a : around_flag) {
            if (a.isPassable() && !a.getMapLocation().equals(flagLoc)) {
                if ((a.getMapLocation().x + a.getMapLocation().y) % 2 == 1) {
                    builder.waitAndBuildTrap(TrapType.WATER, a.getMapLocation());
                } else {
                    builder.waitAndBuildTrap(TrapType.EXPLOSIVE, a.getMapLocation());
                }
            }
        }
        afterBuildTrap = true;
    }

    public void digLand() throws GameActionException {
        Direction[] dirs = Direction.allDirections();
        Direction dir = dirs[rand.nextInt(dirs.length)];
        while (rc.canMove(dir) && rc.getLocation().distanceSquaredTo(ourFlag[Constants.myID]) <= 20) {
            rc.move(dir);
            for (Direction d : dirs) {
                if (rc.canDig(rc.getLocation().add(d))
                        && (rc.getLocation().add(d).x + rc.getLocation().add(d).y) % 2 == 0) {
                    rc.dig(rc.getLocation().add(d));
                }
            }
        }
        if (rc.getLocation().distanceSquaredTo(ourFlag[Constants.myID]) > 2) {
            dir = rc.getLocation().directionTo(ourFlag[Constants.myID]);
            if (rc.canMove(dir)) {
                rc.move(dir);
            } else if (rc.canMove(dir.rotateRight())) {
                rc.move(dir.rotateRight());
            } else if (rc.canMove(dir.rotateLeft())) {
                rc.move(dir.rotateLeft());
            }
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
                Constants.FLAGS[i] = Comms.getFlagLocation(rc, rc.getTeam(), i);
            }
            if (!afterBuildTrap) {
                buildAroundFlags();
            }
            digLand();

        } else if (isExplorer()) {
            if (rc.getRoundNum() <= Constants.EXPLORE_ROUNDS) {
                Pathfind.explore(rc);
                Comms.reportZoneInfo(rc);
            } else {
                Pathfind.moveToward(rc, new MapLocation(Constants.mapWidth / 2, Constants.mapHeight / 2), false);
            }
        }
    }
}