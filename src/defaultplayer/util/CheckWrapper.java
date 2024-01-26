package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Comms;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;

import static defaultplayer.Constants.*;
import static defaultplayer.util.Optimizer.*;

public class CheckWrapper {
    public static boolean contains(int[] array, int value) {
        for (int key : array) {
            if (value == key) return true;
        }
        return false;
    }

    public static <T> boolean contains(T[] array, T value) {
        for (T key : array) {
            if (value.equals(key)) return true;
        }
        return false;
    }

    public static <T> int getIndex(T[] array, T value) {
        for (int i = 0; i < array.length; i++) {
            if (value.equals(array[i])) return i;
        }
        return -1;
    }

    public static boolean isBuilder() {
        return contains(BUILDERS, myID);
    }

    public static boolean isExplorer() {
        return 4 <= myID;
    }

    public static boolean isGuard() {
        return 4 <= myID && myID <= 9;
    }

    public static boolean isNearDam(RobotController rc) throws GameActionException {
        MapInfo[] nearby = rc.senseNearbyMapInfos(2);
        for (MapInfo mapInfo : nearby) if (mapInfo.isDam()) return true;
        return false;
    }

    public static boolean isNearDam(RobotController rc, MapLocation center) throws GameActionException {
        MapInfo[] nearby = rc.senseNearbyMapInfos(center, 2);
        for (MapInfo mapInfo : nearby) if (mapInfo.isDam()) return true;
        return false;
    }

    public static boolean isNearDam(RobotController rc, MapLocation center, int radius) throws GameActionException {
        MapInfo[] nearby = rc.senseNearbyMapInfos(center, radius);
        for (MapInfo mapInfo : nearby) if (mapInfo.isDam()) return true;
        return false;
    }

    public static boolean isFlagInDanger(RobotController rc) throws GameActionException {
        boolean isDanger = false;
        MapLocation myFlag = ALLY_FLAGS[myID - 1];
        for (RobotInfo robot : rc.senseNearbyRobots(-1, OPPONENT)) {
            if (myFlag.distanceSquaredTo(robot.getLocation()) < 20) {
                isDanger = true;
                break;
            }
        }
        return isBuilder() && isDanger;
    }

    public static boolean flagInDanger(RobotController rc) throws GameActionException {
        return Comms.isFlagInDanger(rc, 0) || Comms.isFlagInDanger(rc, 1) || Comms.isFlagInDanger(rc, 2);
    }

    public static boolean nearbyEnemyHasFlag(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(10, OPPONENT);
        for (RobotInfo robot : nearbyEnemies) {
            if (robot.hasFlag()) return true;
        }
        return false;
    }


    public static boolean isNearEnemyTerritory(RobotController rc) throws GameActionException {
        for (MapInfo mapInfo : rc.senseNearbyMapInfos(16))
            if (mapInfo.getTeamTerritory() == OPPONENT) return true;
        return false;
    }

    public static boolean isCriticalZone(int id) {
        return (contains(FLAG_ZONES, id) || contains(NEIGHBORING_ZONES, id));
    }


}
