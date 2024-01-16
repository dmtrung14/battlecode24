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

    public void buildCornerDefense(MapLocation loc) throws GameActionException {
        Direction left = Direction.NORTH;
        Direction right = Direction.EAST;
        Direction lr = Direction.SOUTHEAST;
        Direction rl = Direction.NORTHEAST;
        int rotation;
        // set directions in different location cases
        if (loc.equals(new MapLocation(0,0))) {
            rotation = 0;
        } else if (loc.equals(new MapLocation(0, rc.getMapHeight()-1))){
            rotation = 2;
        } else if (loc.equals(new MapLocation(rc.getMapWidth() -1, 0))) {
            rotation = 6;
        } else {
            rotation = 4;
        }
        for (int r = 0; r < rotation; r ++ ) {
            left = left.rotateRight();
            right = right.rotateRight();
            lr = lr.rotateRight();
            rl = rl.rotateRight();
        }
        Direction lro = lr.opposite();
        // manually go through the desired strategy
        for (int i = 0; i < 2; i++) {rc.move(left);}
        waitAndBuildTrap(TrapType.WATER, rc.getLocation());
        rc.move(lr);
        waitAndDig(rc.getLocation());
        waitAndBuildTrap(TrapType.EXPLOSIVE, rc.getLocation());
        rc.move(lr);
        waitAndBuildTrap(TrapType.WATER, rc.getLocation());
        for (int i = 0; i < 2; i ++){rc.move(right);}
        waitAndBuildTrap(TrapType.WATER, rc.getLocation());
        rc.move(lro);
        waitAndDig(rc.getLocation());
        rc.move(lro);
        waitAndBuildTrap(TrapType.WATER, rc.getLocation());
        rc.move(lro);
        waitAndDig(rc.getLocation());
        rc.move(lro);
        waitAndBuildTrap(TrapType.WATER, rc.getLocation());
        rc.move(rl);
        waitAndBuildTrap(TrapType.WATER, rc.getLocation());
        rc.move(rl);
        waitAndDig(rc.getLocation());
        rc.move(rl);
        waitAndBuildTrap(TrapType.WATER, rc.getLocation());
        rc.move(rl);
        waitAndDig(rc.getLocation());
        rc.move(rl);
        waitAndBuildTrap(TrapType.WATER, rc.getLocation());
    }




    public void run(int myID) {

    }
}
