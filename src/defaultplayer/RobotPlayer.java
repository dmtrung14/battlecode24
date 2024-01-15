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

    public static int mapWidth;
    public static int mapHeight;
    public static MapLocation[] spawnZones;
    public static int myID = 0;
    public static MapLocation mainFlag;
    public static MapLocation[] minorFlags;

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
        minorFlags = {spawnZones[0], spawnZones[2]};
        mainFlag = spawnZones[1];
        Builder builder = new Builder(rc);

        while (true) {
            try {
                if (!rc.isSpawned()) {
                    int spawnID = builder.spawn(myID);
                    if (spawnID > 0) {
                        myID = spawnID;
                    }
                } else {
                    builder.run(myID);
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

}
