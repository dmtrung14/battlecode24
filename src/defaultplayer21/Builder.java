package defaultplayer21;

import battlecode.common.*;

// the builder unit moves the main flag to the corner during the setup phase and builds traps around it
public class Builder {
    private final RobotController rc;

    public Builder(RobotController rc) {
        this.rc = rc;
    }

    public void waitAndBuildTrap(TrapType type, MapLocation loc) throws GameActionException {
        while (!rc.canBuild(type, loc)) {
            Clock.yield();
        }
        rc.build(type, loc);
    }

    public void waitAndBuildTrapTurn(TrapType type, MapLocation loc, int turn) throws GameActionException {
        for (int i = 0; i < turn; i++){
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
