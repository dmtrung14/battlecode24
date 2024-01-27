package defaultplayer;

import battlecode.common.*;

import java.util.Arrays;

import static defaultplayer.Constants.*;
import static defaultplayer.util.Micro.*;

public strictfp class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        Setup setup = new Setup(rc);
        MainPhase main = new MainPhase(rc);

        setup.initializeTurnQueue();
        setup.initializeStatic();

        while (true) {
            try {
                Comms.loadComms(rc);
                tryUpdateInfo(rc);
                tryBuyGlobal(rc);
                if (rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) {
                    setup.run();
                } else {
                    main.run();
                }
                Comms.postComms(rc);
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