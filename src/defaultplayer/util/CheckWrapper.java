package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Comms;

import static defaultplayer.Constants.*;
import static defaultplayer.util.Optimizer.*;

public class CheckWrapper {
    public static <T> boolean contains(T[] array, T value){
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

    public static boolean isNearDam(RobotController rc) throws GameActionException {
        MapInfo[] nearby = rc.senseNearbyMapInfos(2);
        for (MapInfo mapInfo : nearby){
            if (mapInfo.isDam()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFlagInDanger(RobotController rc) throws GameActionException {
        boolean isDanger = false;
        MapLocation myFlag = ALLY_FLAGS[myID - 1];
        for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam().opponent())) {
            if (myFlag.distanceSquaredTo(robot.getLocation()) < 20) isDanger = true; break;
        }
        return isBuilder() && isDanger;
    }
    public static boolean flagInDanger(RobotController rc) throws GameActionException {
        return Comms.isFlagInDanger(rc, 0) || Comms.isFlagInDanger(rc, 1) || Comms.isFlagInDanger(rc, 2);
    }

//    public static boolean hasObjective(RobotController rc) throws GameActionException {
//        return isFlagDanger(rc) || nearestFlag(rc) != null;
//    }

}
