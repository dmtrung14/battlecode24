package defaultplayer;

import battlecode.common.*;

import java.util.*;

import static defaultplayer.Constants.*;
import static defaultplayer.util.CheckWrapper.*;
import static defaultplayer.util.Micro.*;
import static defaultplayer.util.Optimizer.*;

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
        MapLocation[] flagPings = rc.senseBroadcastFlagLocations();
        for (int i = 0; i < 3; i++) {
            if (i < flagPings.length) ENEMY_FLAGS_PING[i] = flagPings[i];
            else ENEMY_FLAGS_PING[i] = Comms.getFlagLocation(rc, rc.getTeam().opponent(), 2 - i);
        }
    }

//    public void tryCaptureFlag() throws GameActionException {
//        if (!rc.isSpawned()) return;
//        if (!rc.hasFlag()) {
//            FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
//            if (flags.length > 0 && !flags[0].isPickedUp()){
//                Pathfind.moveToward(rc, flags[0].getLocation(), true);
//                if (rc.canPickupFlag(flags[0].getLocation())) rc.pickupFlag(flags[0].getLocation());
//            } else if (flags.length > 0 && flags[0].isPickedUp()) {
//                builder.clearWaterForFlag(flags[0].getLocation(), nearestSpawnZone(rc));
//                Pathfind.moveToward(rc, nearestSpawnZone(rc), true);
//            }
//        }
//    }



    public void run() throws GameActionException {
//        if (rc.getRoundNum() > 400) rc.resign();
        if (isBuilder()) {
            if (!rc.isSpawned()) {
                setup.spawn();
                Pathfind.moveToward(rc, ALLY_FLAGS[myID - 1], true);
            } else {
                Comms.setFlagDanger(rc, myID - 1, Comms.isFlagInDanger(rc, myID -1));
                if (Comms.isFlagInDanger(rc, myID - 1)) tryAttack(rc);
            }
        } else if (isExplorer()) {
            if (!rc.isSpawned()) setup.spawn();
            tryBuyGlobal();
            tryUpdateInfo();

            // TODO : Configure the logic for the order of attack, movement when flag is in danger.
            if (rc.hasFlag()) tryReturnFlag(rc);
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