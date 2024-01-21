package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Constants;

import static defaultplayer.Constants.*;

public class Optimizer {
    public static MapLocation nearestSpawnZone(RobotController rc) {
        if (!rc.isSpawned()) return null;
        int minDistance = Integer.MAX_VALUE;
        MapLocation nearest = null;
        MapLocation current = rc.getLocation();
        for (MapLocation home : Constants.SPAWN_ZONES) {
            if (current.distanceSquaredTo(home) < minDistance) {
                nearest = home;
                minDistance = current.distanceSquaredTo(home);
            }
        }
        return nearest;
    }

    public static MapLocation nearestFlag(RobotController rc) {
        if (!rc.isSpawned()) return null;
        int minDistance = Integer.MAX_VALUE;
        MapLocation nearest = null;
        MapLocation current = rc.getLocation();
//        System.out.println(Integer.toString(rc.getRoundNum()).concat(Arrays.toString(ENEMY_FLAGS_PING)));
        // prioritize reported comms flags over flag pings
        for (MapLocation flag : ENEMY_FLAGS_COMMS) {
            int distance = current.distanceSquaredTo(flag);
            if (distance < minDistance) {
                nearest = flag;
                minDistance = distance;
            }
        }
        if (nearest != null) return nearest;
        for (MapLocation ping : ENEMY_FLAGS_PING) {
            int distance = current.distanceSquaredTo(ping);
            if (distance < minDistance) {
                nearest = ping;
                minDistance = distance;
            }
        }
        return nearest;
    }

    public static RobotInfo nearestEnemy(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return null;
        int minDistance = Integer.MAX_VALUE;
        RobotInfo nearest = null;
        MapLocation current = rc.getLocation();
        for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam().opponent())) {
            if (current.distanceSquaredTo(robot.getLocation()) < minDistance) {
                nearest = robot;
                minDistance = current.distanceSquaredTo(robot.getLocation());
            }
        }
        return nearest;
    }

    public static RobotInfo weakestAlly(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return null;
        int minHealth = Integer.MAX_VALUE;
        RobotInfo weakest = null;
        for (RobotInfo robot : rc.senseNearbyRobots(4, rc.getTeam())) {
            if (robot.getHealth() < minHealth) {
                weakest = robot;
                minHealth = robot.getHealth();
            }
        }
        return weakest;
    }
}
