package defaultplayer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    private static int mapWidth;
    private static int mapHeight;
    private static MapLocation[] spawnZones;
    private static int myID;
    private static MapLocation mainFlag;
    private static MapLocation[] minorFlags;
    
    private static int FLAG_RUNNER = 2;
    private static int[] TRAP_BUILDERS = {3, 4, 5};

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        spawnZones = rc.getAllySpawnLocations();
        minorFlags = {spawnZones[0],  spawnZones[2]};
        mainFlag = spawnZones[1];
        Bulder builder = new Builder(rc);

        while (true) {
            try {

                if (! rc.isSpawned()) {
                    if (int spawnID = builder.spawn() > 0) {
                        myID = spawnID;
                    }
                } else {
                    // during setup phase
                    if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
                        switch (myID) {
                            case FLAG_RUNNER:
                                // if main flag in comms is set
                                if (mainFlag) {
                                    builder.pickupMainFlag();
                                    builder.moveToCorner();
                                    // let's use this duck to back out and build the traps too
                                } else if {
                                    // if main flag is successfully placed in the corner
                                    FLAG_RUNNER = 0; // free the duck from duty
                                    
                                }                            
                                break;
                            case TRAP_BUILDERS[0]:
                                break;
                            case TRAP_BUILDERS[1]:
                                break;
                            case TRAP_BUILDERS[2]:

                                builder.moveTo(mainFlag);
                                break;
                        }
                    } else {
                        // after setup phase
                        if (myID == 1) {


                }
            }

            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }
    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)){
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }

}
}