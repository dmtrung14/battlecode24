package defaultplayer.util;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import defaultplayer.Constants;

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
        // TODO: implement checks when enemy_flags are pinpointed
//        for (MapLocation flag : Constants.ENEMY_FLAGS) {
//            if (current.distanceSquaredTo(flag) < minDistance) {
//                nearest = flag;
//                minDistance = current.distanceSquaredTo(flag);
//            }
//
        for (MapLocation ping : Constants.ENEMY_FLAGS_PING) {
            if (current.distanceSquaredTo(ping) < minDistance) {
                nearest = ping;
                minDistance = current.distanceSquaredTo(ping);
            }
        }

        return nearest;
    }

}
