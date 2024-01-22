package defaultplayer21;

import battlecode.common.*;

import static defaultplayer21.Constants.*;
import static defaultplayer21.util.CheckWrapper.*;
import static defaultplayer21.util.Micro.*;
import static defaultplayer21.util.Optimizer.*;

public class MainPhase {
    private final RobotController rc;
    private final Setup setup;
    private final Builder builder;

    public MainPhase(RobotController rc) {
        this.rc = rc;
        this.setup = new Setup(rc);
        this.builder = new Builder(rc);
    }

    public void tryBuyGlobal() throws GameActionException {
        if (rc.canBuyGlobal(GlobalUpgrade.ATTACK)) rc.buyGlobal(GlobalUpgrade.ATTACK);
        else if (rc.canBuyGlobal(GlobalUpgrade.HEALING)) rc.buyGlobal(GlobalUpgrade.HEALING);
        else if (rc.canBuyGlobal(GlobalUpgrade.CAPTURING)) rc.buyGlobal(GlobalUpgrade.CAPTURING);
    }


    public void tryUpdateInfo() throws GameActionException {
        if (!rc.isSpawned()) return;
        Comms.reportNearbyEnemyFlags(rc);
        ENEMY_FLAGS_PING = rc.senseBroadcastFlagLocations();
        ENEMY_FLAGS_COMMS = Comms.getEnemyFlagLocations(rc);
    }

    public void run() throws GameActionException {
//        if (rc.getRoundNum() > 400) rc.resign();
        if (isBuilder()) {
            if (!rc.isSpawned()) {
                setup.spawn();
                Pathfind.moveToward(rc, ALLY_FLAGS[myID - 1], true);
            } else {
                Comms.setFlagDanger(rc, myID - 1, isFlagInDanger(rc));
                if (isFlagInDanger(rc)) {
//                    System.out.println(toAttack(rc));
                    tryAttack(rc);
                }
            }
        }
        else if (isExplorer()) {
            if (!rc.isSpawned()) setup.spawn();
            tryBuyGlobal();
            tryUpdateInfo();

            // TODO : Configure the logic for the order of attack, movement when flag is in danger.
            if (rc.hasFlag()) tryReturnFlag(rc);
//            else if (flagInDanger(rc)) {
//                int toGuard = toReturnAndGuard(rc);
//                if (toGuard != (-1)) Pathfind.moveToward(rc, ALLY_FLAGS[toGuard], false);
//            }
            else {
                tryAttack(rc);
                tryCaptureFlag(rc, builder);
                if (!rc.isSpawned()) return;
                tryHeal(rc);
                if (nearestFlag(rc) != null) Pathfind.moveToward(rc, nearestFlag(rc), true);
                else Pathfind.moveToward(rc, new MapLocation(mapWidth / 2, mapHeight / 2), true);
                if (!rc.isSpawned()) return;

                Pathfind.explore(rc);
            }

            Comms.reportZoneInfo(rc);
        }
    }
}