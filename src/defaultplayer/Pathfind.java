package defaultplayer;

import battlecode.common.*;

import java.util.Random;

public class Pathfind {
    static final Random rng = new Random();

    public static void explore(RobotController rc) throws GameActionException {
        // prioritize collecting crumbs
        MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
        if (crumbs.length > 0) {
            moveToward(rc, crumbs[0]);
        }
        // try to run away from allies
        RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
        if (allies.length > 0) {
            MapLocation loc = rc.getLocation();
            for (RobotInfo ally : allies) {
                Direction dir = rc.getLocation().directionTo(ally.location).opposite();
                loc = loc.add(dir);
            }
            moveToward(rc, loc);
        } else {
            // move randomly
            Direction[] dirs = Direction.allDirections();
            Direction dir = dirs[rng.nextInt(dirs.length)];
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

    public static void moveToward(RobotController rc, MapLocation target) throws GameActionException {
        // temporary
        Direction dir = rc.getLocation().directionTo(target);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
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