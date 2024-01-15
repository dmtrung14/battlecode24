package defaultplayer;

import battlecode.common.*;

import java.util.*;

// the builder unit moves the main flag to the corner during the setup phase and builds traps around it
public class Builder {
    private final RobotController rc;

    public Builder(RobotController rc) {
        this.rc = rc;
    }

    public int spawn(MapLocation locoation) throws GameActionException {
        // TODO: spawn closer to main flag? <-- this is probably not necessary
        while (!rc.isSpawned()) {

            

            // spawn evenly across all ally spawn locations
            MapLocation[] locs = rc.getAllySpawnLocations();
            rc.spawn(locs[Random.nextInt(locs.length)]);

            // set ID for the spawned robot during the setup phase because they don't die
            if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
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

    public void setupPhase() throws GameActionException {
        // add exploration and crumb collection?
        spawn();
        pickupMainFlag();
        moveToCorner();
        rc.dropFlag(rc.getLocation());
        MapLocation[] neighbors = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 2);
        for (MapLocation neighbor : neighbors) {
            if (!neighbor.equals(rc.getLocation())) {
                waitAndBuildTrap(TrapType.WATER, neighbor);
            }
        }
    }

    public void run() {
        try {
            setupPhase();
            // TODO: do something after the setup phase
            while (true) {
                Clock.yield();
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
