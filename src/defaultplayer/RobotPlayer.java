package defaultplayer;

import battlecode.common.*;

import static defaultplayer.Constants.myID;

public strictfp class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        Setup setup = new Setup(rc);
        MainPhase main = new MainPhase(rc);
        setup.initializeTurnQueue();
        setup.initializeStatic();
//        rc.setIndicatorString(String.valueOf(Constants.myID));

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