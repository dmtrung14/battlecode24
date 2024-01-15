package defaultplayer;

import battlecode.common.*;

import java.util.*;

// the builder unit moves the main flag to the corner during the setup phase and builds traps around it
public class Builder {
    public static int FLAG_RUNNER = 2;
    public static int[] TRAP_BUILDERS = {3, 4, 5};


    private final RobotController rc;
    

    public Builder(RobotController rc) {
        this.rc = rc;
    }

    public int spawn(int myID) throws GameActionException {
        // TODO: spawn closer to main flag? <-- this is probably not necessary
        while (!rc.isSpawned()) {

            // spawn evenly across all ally spawn locations
            MapLocation[] locs = rc.getAllySpawnLocations();
            rc.spawn(locs[Random.nextInt(locs.length)]);

            // set ID for the spawned robot during the setup phase because they don't die
            if (myID == 0) {
                return Comms.incrementAndGetId(rc)
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
        MapLocation flag = new MapLocation(27, 12);
        moveTo(flag);
        rc.pickupFlag(rc.getLocation());
    }

    public void moveToCorner() throws GameActionException {
        MapLocation corner = new MapLocation(30, 0);
        moveTo(corner);
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
            return;
        } else if (Arrays.asList(TRAP_BUILDERS).contains(myID)) {
            waitAndBuildTrap(TrapType.WATER, rc.getLocation());
            return;
        }
        /* I thought once we place one water trap it automatically fills the water for all squares in radius 3 ? 
        MapLocation[] neighbors = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 2);
        for (MapLocation neighbor : neighbors) {
            if (!neighbor.equals(rc.getLocation())) {
                waitAndBuildTrap(TrapType.WATER, neighbor);
            }
        }*/
    }

    public void run(int myID) {
        try {
            if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
                setupPhase(myID);
            }
            // TODO: do something after the setup phase
            while (true) {
                Clock.yield();
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
