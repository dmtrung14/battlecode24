package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Constants;
import defaultplayer.Pathfind;

import java.awt.*;

import static defaultplayer.util.Optimizer.*;

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
        return isBuilder() && rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length > 0;
    }

    public static boolean hasObjective(RobotController rc) throws GameActionException {
        return isFlagDanger(rc) || nearestFlag(rc) != null;
    }

    public static boolean guardThisFlag(RobotController rc, int flag) {
        // TODO : Configure the logic for which robots to rush back when a flag is in danger
        return 4 + flag * 5 <= Constants.myID && Constants.myID < 9 + flag * 5;
    }
}
