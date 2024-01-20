package defaultplayer;

import battlecode.common.*;

import java.util.*;

import static defaultplayer.util.CheckWrapper.isNearDam;


public class Pathfind {
    private static MapLocation curTarget = null;
    private static boolean goingAroundObstacle = false;
    private static int obstacleStartDistSquared;
    private static Direction obstacleDir;

    public static void explore(RobotController rc) throws GameActionException {
        collectCrumbs(rc);
        moveAwayFromEdge(rc);
        moveAwayFromAllies(rc);
        moveRandomly(rc);
    }

    private static void collectCrumbs(RobotController rc) throws GameActionException {
        MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
        if (crumbs.length > 0) {
            moveToward(rc, crumbs[0], true);
        }
    }

    private static void moveAwayFromEdge(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        if (rc.getLocation().x < 3) loc = loc.add(Direction.EAST);
        if (rc.getLocation().x > Constants.mapWidth - 4) loc = loc.add(Direction.WEST);
        if (rc.getLocation().y < 3) loc = loc.add(Direction.NORTH);
        if (rc.getLocation().y > Constants.mapHeight - 4) loc = loc.add(Direction.SOUTH);
        if (!rc.getLocation().equals(loc)) moveToward(rc, loc, true);
    }

    private static void moveAwayFromAllies(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
        if (allies.length > 0) {
            for (RobotInfo ally : allies) {
                Direction dir = rc.getLocation().directionTo(ally.location).opposite();
                loc = loc.add(dir);
            }
            if (!rc.getLocation().equals(loc)) {
                Direction dir = rc.getLocation().directionTo(loc);
                if (rc.canMove(dir)) rc.move(dir);
            }
        }
    }

    private static void moveRandomly(RobotController rc) throws GameActionException {
        Direction dir = Constants.DIRECTIONS[Constants.RANDOM.nextInt(8)];
        if (rc.canMove(dir)) rc.move(dir);
    }

    public static void moveToward(RobotController rc, MapLocation target, boolean fill) throws GameActionException {
        if (!rc.isSpawned()) return;
        if (target != curTarget) {
            curTarget = target;
            goingAroundObstacle = false;
        }
        if (!goingAroundObstacle) {
            MapLocation current = rc.getLocation();
            Direction dir = current.directionTo(target);
            MapLocation loc = current.add(dir);
            if (isPassable(rc, loc, fill)) {
                if (rc.canFill(loc)) rc.fill(loc);
                if (rc.canMove(dir)) rc.move(dir);
            } else {
                goingAroundObstacle = true;
                obstacleStartDistSquared = rc.getLocation().distanceSquaredTo(target);
                obstacleDir = dir;
            }
        }
        if (goingAroundObstacle) {
            for (int i = 0; i < 8; i++) {
                MapLocation loc = rc.getLocation().add(obstacleDir);
                if (isPassable(rc, loc, fill)) {
                    if (rc.canFill(loc)) rc.fill(loc);
                    if (rc.canMove(obstacleDir)) rc.move(obstacleDir);
                    obstacleDir = obstacleDir.rotateRight().rotateRight();
                    break;
                } else {
                    obstacleDir = obstacleDir.rotateLeft(); // this is probably what causes the bot to go in circle
                }
            }
            int distSquared = rc.getLocation().distanceSquaredTo(target);
            if (distSquared < obstacleStartDistSquared) {
                goingAroundObstacle = false;
            }
        }
    }

    private static boolean isPassable(RobotController rc, MapLocation loc, boolean fill) throws GameActionException {
        return rc.onTheMap(loc) &&
                (rc.senseRobotAtLocation(loc) == null) &&
                (rc.sensePassability(loc) || fill && rc.canFill(loc));
    }

    public static MapLocation[] avoid(RobotController rc, MapLocation center, int distanceSquared) throws GameActionException {
        ArrayList<MapLocation> possibleMoves = new ArrayList<>();
        Comparator<MapLocation> comparator = (a, b) -> {
            // sort in reverse order of distance
            MapLocation current = rc.getLocation();
            int minDistanceToOtherA = Integer.MAX_VALUE;
            int minDistanceToOtherB = Integer.MAX_VALUE;
            for (int j = 0; j < 3; j ++ ){
                try {
                    MapLocation flag = Comms.getFlagLocation(rc, rc.getTeam(), j);
                    if (j + 1 != Constants.myID && current.distanceSquaredTo(flag) < minDistanceToOtherA) {
                        minDistanceToOtherA = a.distanceSquaredTo(flag);
                    }
                    if (j + 1 != Constants.myID && current.distanceSquaredTo(flag) < minDistanceToOtherB) {
                        minDistanceToOtherB = b.distanceSquaredTo(flag);
                    }
                } catch (GameActionException e) {
                    throw new RuntimeException(e);
                }
            }
            if (a.equals(current.add(current.directionTo(center).opposite()))) return -1;
            else if (b.equals(current.add(current.directionTo(center).opposite()))) return 1;
            else if (minDistanceToOtherA != minDistanceToOtherB) return Integer.compare(minDistanceToOtherA, minDistanceToOtherB);
            else return Integer.compare(b.distanceSquaredTo(center), a.distanceSquaredTo(center));
        };
        for (MapInfo locInfo : rc.senseNearbyMapInfos(2)){
            MapLocation location = locInfo.getMapLocation();
            if (location.distanceSquaredTo(center) >= distanceSquared && !locInfo.isWall() && !locInfo.isDam()){
                // check if current flag location is at least 6 from both the other 2 flags:
                boolean valid = true;
                for (int j = 0; j < 3; j ++ ){
                    if (j + 1 != Constants.myID && location.distanceSquaredTo(Comms.getFlagLocation(rc, rc.getTeam(), j)) < 36){
                        valid = false;
                        break;
                    }
                }
                if (valid) possibleMoves.add(location);
            }
        }
        possibleMoves.sort(comparator);
        MapLocation[] results = new MapLocation[possibleMoves.size()];
        return possibleMoves.toArray(results);
    }

    public static MapLocation[] attract(RobotController rc, MapLocation center, int distanceSquared) throws GameActionException {
        ArrayList<MapLocation> possibleMoves = new ArrayList<>();
        Comparator<MapLocation> comparator = Comparator.comparingInt(A -> A.distanceSquaredTo(center));
        for (MapInfo locInfo : rc.senseNearbyMapInfos(2)) {
            MapLocation location = locInfo.getMapLocation();
            if (locInfo.isDam()) return new MapLocation[0];
            else if (location.distanceSquaredTo(center) <= distanceSquared && !locInfo.isWall() && !locInfo.isWater()) {
                possibleMoves.add(location);
            }
        }
        possibleMoves.sort(comparator);
        MapLocation[] results = new MapLocation[possibleMoves.size()];
        return possibleMoves.toArray(results);
    }
    // execute these algorithms within vision range
}