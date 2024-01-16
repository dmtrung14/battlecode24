package defaultplayer;

import battlecode.common.*;
import scala.collection.Map;

import java.lang.Math;
import java.util.*;
public class Setup {

    public static int FLAG_RUNNER = 2;
    public static int[] TRAP_BUILDERS = {3, 4, 5};
    public static int[] MAIN_FLAG_BUILDERS = {6, 7, 8, 9 , 10};
    private static final int EXPLORE_ROUNDS = 150;
//    private static final int CORNER;
    private static RobotController rc = null;



    public Setup(RobotController rc) {
        this.rc = rc;
    }
    public void deployment(){

    }
    private static final Builder builder = new Builder(rc);
    private final Random rand = new Random(rc.getID());
    public static MapLocation runFindDam() throws GameActionException {
        // get the robot id to see if it should be finding dam
        return new MapLocation(0, 0);
    }

    public static MapLocation getMainSpawn() throws GameActionException {
        return new MapLocation(0, 0);
    }

    public static MapLocation spawnNearMain() throws GameActionException {
        return new MapLocation(0, 0);
    }
    
    public MapLocation findAllyCorner(MapLocation dam){
        MapLocation secondCoordinateDam = new MapLocation(RobotPlayer.mapWidth - dam.x, RobotPlayer.mapHeight - dam.y);
        Team ourTeam = rc.getTeam();
        MapLocation corner1 = new MapLocation(0,0);
        MapLocation corner2 = new MapLocation(0, RobotPlayer.mapHeight - 1);
        MapLocation corner3 = new MapLocation(RobotPlayer.mapWidth - 1, 0);
        MapLocation corner4 = new MapLocation(RobotPlayer.mapWidth - 1, RobotPlayer.mapHeight - 1);
        MapLocation currentCoorRobot = rc.getLocation();
        // Process to find corner
        HashMap<MapLocation, Integer> mp = new HashMap<MapLocation, Integer>();
        MapLocation[] corner = new MapLocation[2];
        mp.put(corner1, currentCoorRobot.distanceSquaredTo(corner1));
        mp.put(corner2, currentCoorRobot.distanceSquaredTo(corner2));
        mp.put(corner3, currentCoorRobot.distanceSquaredTo(corner3));
        mp.put(corner4, currentCoorRobot.distanceSquaredTo(corner4));
        Set<MapLocation> set = mp.keySet();
        for (MapLocation key : set){
            try {
                Pathfind.BFS(rc, currentCoorRobot, key);
                if(rc.getLocation().equals(key)){
                    return key;
                }
                else{
                    mp.remove(key);
                }
            } catch (GameActionException e){
                e.printStackTrace();
            }

        }
        return rc.getLocation();
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
                    rc.spawn(getMainSpawn());
                } else {
                    rc.spawn(locs[rand.nextInt(locs.length)]);
                }
                return newID;
            } else {
                rc.spawn(locs[rand.nextInt(locs.length)]);
            }
            return 0;
        }
        return 0;
    }

    public void pickupMainFlag() throws GameActionException {
        // TODO: calculate location of main flag
        // right now it's hard coded for the default small map
        MapLocation flag = Setup.getMainSpawn();
        builder.moveTo(flag);
        rc.pickupFlag(rc.getLocation());
    }

    public void moveToCorner() throws GameActionException {

        // move most aggressively

        MapLocation corner = findAllyCorner(Setup.runFindDam()); // Replace with the actual corner location

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
    public void run(int myID) {
        try {
            if (myID == FLAG_RUNNER){
                pickupMainFlag();
                moveToCorner();
                rc.dropFlag(rc.getLocation());
                builder.waitAndBuildTrap(TrapType.WATER, rc.getLocation());
            } else if (Arrays.asList(TRAP_BUILDERS).contains(myID)) {
                builder.waitAndBuildTrap(TrapType.WATER, rc.getLocation());
            }
            // TODO: do something after the setup phase

        } catch (GameActionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}