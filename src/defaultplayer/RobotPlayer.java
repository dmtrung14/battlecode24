package defaultplayer;

import battlecode.common.*;

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
        Setup setup = new Setup(rc);
        MainPhase main = new MainPhase(rc);
        setup.intializeTurnQueue();
        setup.initializeStatic();


        while (true) {
            try {
                if (rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) {
                    setup.run();
                } else {
                    main.run();
                }
            } catch (Exception e) {
                String message = "Exception";
                MapLocation loc = rc.getLocation();
                if (loc != null) message += " at " + loc;
                System.out.println(message);
                e.printStackTrace();
                System.out.println();
            } finally {
                Clock.yield();
            }
        }
        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

}
