package defaultplayer_sprint2;

import battlecode.common.*;

import static defaultplayer_sprint2.Constants.*;
import static defaultplayer_sprint2.util.CheckWrapper.*;
import static defaultplayer_sprint2.util.Micro.*;
import static defaultplayer_sprint2.util.Optimizer.nearestFlag;

public class MainPhase {
    private final RobotController rc;
    private final Setup setup;
    private final Builder builder;

    public MainPhase(RobotController rc) {
        this.rc = rc;
        this.setup = new Setup(rc);
        this.builder = new Builder(rc);
    }


    public void run() throws GameActionException {
        tryBuyGlobal(rc);
        tryUpdateInfo(rc);
        if (!rc.isSpawned()) {
            setup.trySpawn();
            if (!rc.isSpawned()) return;
        }
        if (isBuilder()) {
            Pathfind.moveToward(rc, ALLY_FLAGS[myID - 1], true);
            Comms.setFlagDanger(rc, myID - 1, isFlagInDanger(rc));
            if (isFlagInDanger(rc)) {
                tryAttack(rc);
            }
        }
//        else if (isGuard()) {
//            Pathfind.moveToward(rc, ALLY_FLAGS[(myID - 4)/2], true);
//            tryAttack(rc);
//        }
        else if (isExplorer()) {
            // TODO : Configure the logic for the order of attack, movement when flag is in danger.
            if (rc.hasFlag()) tryReturnFlag(rc);
            else {
                tryMoveAwayFromFlagHolder(rc);
                tryAttack(rc);
                tryCaptureFlag(rc, builder);
                if (!rc.isSpawned()) return;
                tryHeal(rc);
                MapLocation nearestFlag = nearestFlag(rc);
                if (nearestFlag != null) Pathfind.moveToward(rc, nearestFlag, true);
                else Pathfind.moveToward(rc, new MapLocation(mapWidth / 2, mapHeight / 2), true);
                if (!rc.isSpawned()) return;

                Pathfind.explore(rc);
            }

            Comms.reportZoneInfo(rc);
//            rc.setIndicatorString(Integer.toString(myID));
        }
    }
}