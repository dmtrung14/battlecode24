package defaultplayer;

import battlecode.common.*;
import com.sun.tools.internal.jxc.ap.Const;

import java.awt.*;
import java.util.*;


public class Pathfind {
    static final Random rng = new Random();

    public static void explore(RobotController rc) throws GameActionException {
        // prioritize collecting crumbs
        MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
        if (crumbs.length > 0) {
            moveToward(rc, crumbs[0]);
        }
        MapLocation loc = rc.getLocation();
        // move away from the map edge
        if (rc.getLocation().x < 3) loc = loc.add(Direction.EAST);
        if (rc.getLocation().x > Constants.mapWidth - 4) loc = loc.add(Direction.WEST);
        if (rc.getLocation().y < 3) loc = loc.add(Direction.NORTH);
        if (rc.getLocation().y > Constants.mapHeight - 4) loc = loc.add(Direction.SOUTH);
        moveToward(rc, loc);
        // try to run away from allies
        RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
        if (allies.length > 0) {
            for (RobotInfo ally : allies) {
                Direction dir = rc.getLocation().directionTo(ally.location).opposite();
                loc = loc.add(dir);
            }
            moveToward(rc, loc);
        }
        // move randomly
        Direction[] dirs = Direction.allDirections();
        Direction dir = dirs[rng.nextInt(dirs.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
    public static void moveToward(RobotController rc, MapLocation target) throws GameActionException {
        // temporary
        Direction dir = rc.getLocation().directionTo(target);
        MapLocation loc = rc.getLocation().add(dir);
        if (rc.canFill(loc)) rc.fill(loc);
        if (rc.canMove(dir)) rc.move(dir);
    }

    public static void moveTowardMain(RobotController rc, MapLocation target, boolean fill) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        MapLocation loc = rc.getLocation().add(dir);
        MapInfo locInfo = rc.senseMapInfo(loc);

        if(fill && locInfo.getTeamTerritory() == rc.getTeam().opponent() && rc.canFill(loc)) rc.fill(loc);

        if(rc.canMove(dir)) rc.move(dir);
        else if(rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
        else if(rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
        else if (rc.canMove(dir.rotateLeft().rotateLeft())) rc.move(dir.rotateLeft().rotateLeft());
        else if (rc.canMove(dir.rotateRight().rotateRight())) rc.move(dir.rotateRight().rotateRight());
        else {
            Direction randDir = Constants.DIRECTIONS[Constants.RANDOM.nextInt(8)];
            if(rc.canMove(randDir)) rc.move(randDir);
        }
    }

    public static MapLocation[] avoid(RobotController rc, MapLocation center, int distanceSquared) throws GameActionException {
        ArrayList<MapLocation> possibleMoves = new ArrayList<MapLocation>();
        Comparator<MapLocation> comparator = new Comparator<MapLocation>() {
            public int compare(MapLocation A, MapLocation B){
                // sort in reverse order of distance
                MapLocation current = rc.getLocation();
                int minDistanceToOtherA = Integer.MAX_VALUE;
                int minDistanceToOtherB = Integer.MAX_VALUE;
                for (int j = 0; j < 3; j ++ ){
                    try {
                        if (j + 1 != Constants.myID && current.distanceSquaredTo(Comms.getFlagLocation(rc, rc.getTeam(), j)) < minDistanceToOtherA){
                            minDistanceToOtherA = A.distanceSquaredTo(Comms.getFlagLocation(rc, rc.getTeam(), j));
                        }
                        if (j + 1 != Constants.myID && current.distanceSquaredTo(Comms.getFlagLocation(rc, rc.getTeam(), j)) < minDistanceToOtherB){
                            minDistanceToOtherB = B.distanceSquaredTo(Comms.getFlagLocation(rc, rc.getTeam(), j));
                        }

                    } catch (GameActionException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (A.equals(current.add(current.directionTo(center).opposite()))) return -1;
                else if (B.equals(current.add(current.directionTo(center).opposite()))) return 1;
                else if (minDistanceToOtherA != minDistanceToOtherB) return Integer.compare(minDistanceToOtherA, minDistanceToOtherB);
                else return Integer.compare(B.distanceSquaredTo(center), A.distanceSquaredTo(center));
            }
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
        results = possibleMoves.toArray(results);
        return results;
    }

    public static MapLocation[] attract(RobotController rc, MapLocation center, int distanceSquared) throws GameActionException {
        ArrayList<MapLocation> possibleMoves = new ArrayList<MapLocation>();
        Comparator<MapLocation> comparator = new Comparator<MapLocation>() {
            public int compare(MapLocation A, MapLocation B){
                return Integer.compare(A.distanceSquaredTo(center), B.distanceSquaredTo(center));
            }
        };
        for (MapInfo locInfo : rc.senseNearbyMapInfos(2)){
            MapLocation location = locInfo.getMapLocation();
            if (locInfo.isDam()) return new MapLocation[0];
            else if (location.distanceSquaredTo(center) <= distanceSquared && !locInfo.isWall() && !locInfo.isWater()){
                possibleMoves.add(location);
            }
        }
        possibleMoves.sort(comparator);
        MapLocation[] results = new MapLocation[possibleMoves.size()];
        results = possibleMoves.toArray(results);
        return results;
    }
    // execute these algorithms within vision range

    public static MapLocation nearestFlag(RobotController rc) {
        if (!rc.isSpawned()) return null;
        int minDistance = Integer.MAX_VALUE;
        MapLocation nearest = null;
        MapLocation current = rc.getLocation();
        // TODO: implement checks when enemy_flags are pinpointed
//        for (MapLocation flag : Constants.ENEMY_FLAGS) {
//            if (current.distanceSquaredTo(flag) < minDistance) {
//                nearest = flag;
//                minDistance = current.distanceSquaredTo(flag);
//            }
//        }
        if (nearest == null) {
            for (MapLocation ping : Constants.ENEMY_FLAGS_PING) {
                if (current.distanceSquaredTo(ping) < minDistance) {
                    nearest = ping;
                    minDistance = current.distanceSquaredTo(ping);
                }
            }
        }
        return nearest;
    }

        

}