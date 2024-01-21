package defaultplayer.util;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import defaultplayer.Constants;

import java.util.Arrays;

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
        for (MapLocation ping : ENEMY_FLAGS_PING) {
            if (ping != null && current.distanceSquaredTo(ping) < minDistance) {
                nearest = ping;
                minDistance = current.distanceSquaredTo(ping);
            }
        }

        return nearest;
    }

}
