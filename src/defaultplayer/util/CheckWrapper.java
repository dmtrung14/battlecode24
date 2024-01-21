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

    public static int myFlagLocalId(RobotController rc) throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : flags) {
            if (flag.getLocation().equals(rc.getLocation())) {
                return flag.getID();
            }
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

    public static boolean flagInDanger(RobotController rc) throws GameActionException {
        return Comms.isFlagInDanger(rc, 0) || Comms.isFlagInDanger(rc, 1) || Comms.isFlagInDanger(rc, 2);
    }

//    public static boolean hasObjective(RobotController rc) throws GameActionException {
//        return isFlagDanger(rc) || nearestFlag(rc) != null;
//    }

}
