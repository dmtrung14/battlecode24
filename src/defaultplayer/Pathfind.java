package defaultplayer;

import battlecode.common.*;

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

    public static MapLocation[] avoid(RobotController rc, MapLocation center, int distanceSquared) throws GameActionException {
        ArrayList<MapLocation> possibleMoves = new ArrayList<MapLocation>();
        Comparator<MapLocation> comparator = new Comparator<MapLocation>() {
            public int compare(MapLocation A, MapLocation B) {
                // sort in reverse order of distance
                if (A.distanceSquaredTo(center) < B.distanceSquaredTo(center)) return 1;
                else if (A.distanceSquaredTo(center) > B.distanceSquaredTo(center)) return -1;
                else return 0;
            }
        };
        for (MapInfo locInfo : rc.senseNearbyMapInfos(2)){
            MapLocation location = locInfo.getMapLocation();
            if (location.distanceSquaredTo(center) > distanceSquared && !locInfo.isWall() && !locInfo.isDam()){
                possibleMoves.add(location);
            }
        }
        possibleMoves.sort(comparator);
        MapLocation[] results = new MapLocation[possibleMoves.size()];
        results = possibleMoves.toArray(results);
        return results;
    }

    // execute these algorithms within vision range

    public static void BFS(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {
        ;
    }

    public static void DFS(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {

    }

    public static void AStar(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {

    }

    public static void Dijkstra(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {

    }

    public static void Bellman_Ford(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {

    }
        

}