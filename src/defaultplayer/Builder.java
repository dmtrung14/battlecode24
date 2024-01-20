package defaultplayer;

import battlecode.common.*;

import java.util.*;

// the builder unit moves the main flag to the corner during the setup phase and builds traps around it
public class Builder {
    public static int FLAG_RUNNER = 2;
    public static int[] TRAP_BUILDERS = {3, 4, 5};
    public static int[] MAIN_FLAG_BUILDERS = {6, 7, 8, 9 , 10};


    private final RobotController rc;


    public Builder(RobotController rc) {
        this.rc = rc;
    }



    // TODO: use a better pathfinding algorithm
    // this is temporary and very basic
    // it sometimes goes in circles, it never fills in water, and you can't take any other actions until you reach the destination
    public void moveTo(MapLocation dest) throws GameActionException {
        while (!rc.getLocation().equals(dest)) {
            Direction dir = rc.getLocation().directionTo(dest);
            while (!rc.sensePassability(rc.getLocation().add(dir))) {
                dir = dir.rotateLeft();
            }
            while (!rc.canMove(dir)) {
                Clock.yield();
            }
            rc.move(dir);
        }
    }

    public void waitAndBuildTrap(TrapType type, MapLocation loc) throws GameActionException {
        while (!rc.canBuild(type, loc)) {
            Clock.yield();
        }
        rc.build(type, loc);
    }

    public void waitAndBuildTrapTurn(TrapType type, MapLocation loc, int turn) throws GameActionException {
        for (int i = 0; i < turn; i ++ ){
            if (!rc.isSpawned()) return;
            if (rc.canBuild(type, loc)) {
                rc.build(type, loc);
                return;
            }
            Clock.yield();
        }
    }
    public void waitAndDig(MapLocation loc) throws GameActionException {
        while (!rc.canDig(loc)) {
            MapInfo locInfo = rc.senseMapInfo(loc);
            if (locInfo.isWall() || locInfo.isWater() || locInfo.isDam() || locInfo.isSpawnZone()){
                return;
            }
            Clock.yield();
        }
        rc.dig(loc);

    }

    public void clearWaterForFlag(MapLocation flag, MapLocation home) throws GameActionException {
        for (MapInfo site : rc.senseNearbyMapInfos()) {
            if (site.getMapLocation().distanceSquaredTo(home) <= flag.distanceSquaredTo(home) && rc.canFill(site.getMapLocation())) rc.fill(site.getMapLocation());
        }
    }



    public void run(int myID) {

    }
}
