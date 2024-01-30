package defaultplayer_intlqualifier;

import battlecode.common.*;

import static defaultplayer_intlqualifier.Constants.*;
import static defaultplayer_intlqualifier.util.CheckWrapper.*;
import static defaultplayer_intlqualifier.util.Micro.*;
import static defaultplayer_intlqualifier.util.Optimizer.*;

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
//                tryMoveAwayFromFlagHolder(rc);
                FlagInfo[] nearbyEnemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
                RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
                tryCaptureFlag(rc, nearbyEnemyFlags);
                tryAttack(rc);
                moveTowardFlag(rc, builder, nearbyEnemyFlags);
                if (!rc.isSpawned()) return;
                tryHeal(rc);
                MapLocation nearestFlag = nearestFlag(rc);
                if (nearestFlag != null && (nearbyEnemies.length < nearbyAllies.length || nearbyEnemies.length == 0) ) Pathfind.moveToward(rc, nearestFlag, false);
                else if (nearestFlag != null) {
                    RobotInfo weakestA = weakestAlly(rc);
                    if (weakestA != null) Pathfind.moveToward(rc, weakestA.getLocation(), false);
                    else {
                        RobotInfo weakestE = weakestEnemy(rc);
                        if (weakestE != null) Pathfind.moveToward(rc, weakestE.getLocation(), false);
                    }
                }
                else Pathfind.moveToward(rc, MY_LAST_LOCATION, true);;
                if (!rc.isSpawned()) return;

                Pathfind.explore(rc, nearbyAllies);
                MY_LAST_LOCATION = rc.getLocation();
            }

            Comms.reportZoneInfo(rc);
//            rc.setIndicatorString(Integer.toString(myID));
        }
    }
}