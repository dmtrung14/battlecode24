package defaultplayer;

import battlecode.common.*;

import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        Constants.mapWidth = rc.getMapWidth();
        Constants.mapHeight = rc.getMapHeight();
        Constants.SPAWN_ZONES = rc.getAllySpawnLocations();
        Constants.myID = Comms.incrementAndGetId(rc);
        Constants.RANDOM = new Random(Constants.myID);
        Setup setup = new Setup(rc);
        MainPhase main = new MainPhase(rc);
        
        while (true) {
            try {
                if (rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) {
                    setup.run();
//                    rc.setIndicatorString(String.format("myID: %s", Constants.myID));
                } else {
                    main.run();
                }


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
