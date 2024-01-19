package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Constants;

import java.awt.*;

public class CheckWrapper {
    public static <T> boolean contains(T[] array, T value){
        for (T key : array) {
            if (value.equals(key)) return true;
        }
        return false;
    }

    public static boolean isBuilder() {
        return contains(Constants.BUILDERS, Constants.myID);
    }
    public static boolean isExplorer() {
        return 4 <= Constants.myID;
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

    public static boolean isFlagDanger(RobotController rc) throws GameActionException {
        return rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length > 0;
    }
}
