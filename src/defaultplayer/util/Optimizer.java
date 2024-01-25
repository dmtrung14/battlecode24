package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Constants;

import java.util.Arrays;
import java.util.Comparator;

import static defaultplayer.Constants.*;
import static defaultplayer.util.Micro.*;

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

    public static MapLocation nearestFlag(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return null;
//        int minDistance = Integer.MAX_VALUE;
//        MapLocation nearest = null;
//        MapLocation current = rc.getLocation();
//        // prioritize reported comms flags over flag pings
//        for (MapLocation flag : ENEMY_FLAGS_COMMS) {
//            int distance = current.distanceSquaredTo(flag);
//            if (distance < minDistance) {
//                nearest = flag;
//                minDistance = distance;
//            }
//        }
//        if (nearest != null) return nearest;
//        for (MapLocation ping : ENEMY_FLAGS_PING) {
//            int distance = current.distanceSquaredTo(ping);
//            if (distance < minDistance) {
//                nearest = ping;
//                minDistance = distance;
//            }
//        }
//        return nearest;
        MapLocation[] enemyFlags = new MapLocation[ENEMY_FLAGS_COMMS.length + ENEMY_FLAGS_PING.length];
        for (int i = 0; i < enemyFlags.length; i++) {
            if (i < ENEMY_FLAGS_COMMS.length) enemyFlags[i] = ENEMY_FLAGS_COMMS[i];
            else enemyFlags[i] = ENEMY_FLAGS_PING[i - ENEMY_FLAGS_COMMS.length];
        }
//            if (myID < 10) return ALLY_FLAGS[myID % ALLY_FLAGS.length];
        int toGuard = toReturnAndGuard(rc);
        if (toGuard != -1) return ALLY_FLAGS[toGuard];
        return enemyFlags.length > 0 ? enemyFlags[myID % enemyFlags.length] : null;
    }

    public static MapLocation nearbyFlagHolder(RobotController rc, FlagInfo[] flags, MapLocation loc) {
        for (FlagInfo flag : flags) {
            if (flag.isPickedUp() && flag.getTeam() == rc.getTeam().opponent() && flag.getLocation().isAdjacentTo(loc)) {
                return flag.getLocation();
            }
        }
        return null;
    }

    public static MapLocation nearestAllyFlag(RobotController rc) {
        int minDistance = Integer.MAX_VALUE;
        MapLocation nearest = null;
        MapLocation current = rc.getLocation();
        // prioritize reported comms flags over flag pings
        for (MapLocation flag : ALLY_FLAGS) {
            int distance = current.distanceSquaredTo(flag);
            if (distance < minDistance) {
                nearest = flag;
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

    public static RobotInfo weakestEnemy(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return null;
        int minHealth = Integer.MAX_VALUE;
        RobotInfo weakest = null;
        for (RobotInfo robot : rc.senseNearbyRobots(4, rc.getTeam().opponent())) {
            if (robot.getHealth() < minHealth) {
                weakest = robot;
                minHealth = robot.getHealth();
            }
        }
        return weakest;
    }

    public static int nearDirection(Direction ogDir, Direction tagDir) {
        if (tagDir.equals(ogDir)) return 0;
        else if (tagDir.equals(ogDir.rotateLeft())) return 1;
        else if (tagDir.equals(ogDir.rotateRight())) return 1;
        else if (tagDir.equals(ogDir.rotateLeft().rotateLeft())) return 2;
        else if (tagDir.equals(ogDir.rotateRight().rotateRight())) return 2;
        else if (tagDir.equals(ogDir.rotateRight().rotateRight().rotateRight())) return 3;
        else if (tagDir.equals(ogDir.rotateLeft().rotateLeft().rotateLeft())) return 3;
        else return 4;
    }


}
