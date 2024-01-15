package defaultplayer;

import battlecode.common.*;

import java.util.*;

// the builder unit moves the main flag to the corner during the setup phase and builds traps around it
public class Builder {
    public static int FLAG_RUNNER = 2;
    public static int[] TRAP_BUILDERS = {3, 4, 5};
    public static int[] MAIN_FLAG_BUILDERS = {6, 7, 8, 9 , 10}


    private final RobotController rc;
    

    public Builder(RobotController rc) {
        this.rc = rc;
    }

    public int spawn(int myID, MapLocation[] locs) throws GameActionException {
        // TODO: spawn closer to main flag? <-- this is probably not necessary
        while (!rc.isSpawned()) {
            // too high overhead for getAllySpawnLocations I made it a parameter

            // set ID for the spawned robot during the setup phase because they don't die
            if (myID == 0) {
                int newID = Comms.incrementAndGetId(rc);
                if (Arrays.asList(TRAP_BUILDERS).contains(newID)) {
                    rc.spawn(locs[newID - 3]); /* spawn at the desired place */
                } else if (newID == FLAG_RUNNER) {
                    rc.spawn(/* get location of middle flag */);
                } else {
                    rc.spawn(locs[Random.nextInt(locs.length)]);
                }
                return newID;
            } else if (/* some mishaps happened and we need specifications for the spawned robot */ false) {
                /*spawning algorithms */
            } else {
                rc.spawn(locs[Random.nextInt(locs.length)]);
            }
            return 0;
        }
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

    public void pickupMainFlag() throws GameActionException {
        // TODO: calculate location of main flag
        // right now it's hard coded for the default small map
        MapLocation flag = Setup.getMainFlag();
        moveTo(flag);
        rc.pickupFlag(rc.getLocation());
    }

    public void moveToCorner() throws GameActionException {

        // move most aggressively

        MapLocation corner = Setup.findCorner(); // Replace with the actual corner location
        
        while (!rc.getLocation().equals(corner)) {
            Direction dir = rc.getLocation().directionTo(corner);
            
            // Check if there is a wall in the desired direction
            MapLocation next = rc.getLocation().add(dir);
            while (!rc.sensePassability(next)) {
                if (rc.senseMapInfo(next).isWall() || rc.senseMapInfo(next).isDam()) {
                    /*
                    // If there is a wall, check if it is a corner
                     */
                    dir = dir.rotateLeft();
                    next = rc.getLocation().add(dir);
                    
                }
                else if (rc.senseMapInfo(next).isWater()) {
                    rc.fill(next);
                }
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

    public void setupPhase(int myID) throws GameActionException {
        // add exploration and crumb collection?
        if (myID == FLAG_RUNNER){
            pickupMainFlag();
            moveToCorner();
            rc.dropFlag(rc.getLocation());
            waitAndBuildTrap(TrapType.WATER, rc.getLocation());
            return;
        } else if (Arrays.asList(TRAP_BUILDERS).contains(myID)) {
            waitAndBuildTrap(TrapType.WATER, rc.getLocation());
            return;
        }
    }

    public void run(int myID) {
        try {
            if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
                setupPhase(myID);
            }
            // TODO: do something after the setup phase

        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
