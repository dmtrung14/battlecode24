package defaultplayer;

import battlecode.common.*;

import java.util.*;

import static defaultplayer.Constants.*;
import static defaultplayer.util.CheckWrapper.*;
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

    public void tryAttack() throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation current = rc.getLocation();
        Arrays.sort(nearbyEnemies, new Comparator<RobotInfo>() {
            @Override
            public int compare(RobotInfo o1, RobotInfo o2) {
                return Integer.compare(o1.getLocation().distanceSquaredTo(current), o2.getLocation().distanceSquaredTo(current));
            }
        });
        for (RobotInfo robot : nearbyEnemies) {
            if (robot.hasFlag()) {
                Pathfind.moveToward(rc, robot.getLocation(), false);
                if (rc.canAttack(robot.getLocation()))
                    rc.attack(robot.getLocation());
            }
        }
        for (RobotInfo robot : nearbyEnemies) {
            if (current.distanceSquaredTo(robot.getLocation()) > 10) {
                tryHeal();
                break;
            } else if (current.distanceSquaredTo(robot.getLocation()) > 8) {
                tryHeal();
                builder.waitAndBuildTrapTurn(TrapType.WATER, current, 2);
                break;
            } else if (current.distanceSquaredTo(robot.getLocation()) > 6) {
                tryHeal();
                builder.waitAndBuildTrapTurn(TrapType.EXPLOSIVE, current, 2);
                break;
            } else if (current.distanceSquaredTo(robot.getLocation()) > 4) {
                builder.waitAndBuildTrapTurn(TrapType.STUN, current, 2);
            }
            Pathfind.moveToward(rc, robot.getLocation(), false);
            if (rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
        }
    }

    public void tryHeal() throws GameActionException {
        if (!rc.isSpawned()) return;
        for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
            if (rc.canHeal(robot.getLocation()))
                rc.heal(robot.getLocation());
        }
    }

    public void tryUpdateInfo() throws GameActionException {
        if (!rc.isSpawned()) return;
        MapLocation[] flagPings = rc.senseBroadcastFlagLocations();
        for (int i = 0; i < 3; i++) {
            if (i < flagPings.length) ENEMY_FLAGS_PING[i] = flagPings[i];
            else ENEMY_FLAGS_PING[i] = Comms.getFlagLocation(rc, rc.getTeam().opponent(), 2 - i);
        }
    }

    public void tryCaptureFlag() throws GameActionException {
        if (!rc.isSpawned()) return;
        Comms.reportNearbyEnemyFlags(rc);
        if (!rc.hasFlag()) {
            FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
            if (flags.length > 0 && !flags[0].isPickedUp()){
                Pathfind.moveToward(rc, flags[0].getLocation(), true);
                if (rc.canPickupFlag(flags[0].getLocation())) rc.pickupFlag(flags[0].getLocation());
            } else if (flags.length > 0 && flags[0].isPickedUp()) {
                builder.clearWaterForFlag(flags[0].getLocation(), nearestSpawnZone(rc));
                Pathfind.moveToward(rc, nearestSpawnZone(rc), true);
            }
        } else {
            Pathfind.moveToward(rc, nearestSpawnZone(rc), false);
            MapInfo[] nearby = rc.senseNearbyMapInfos(2);
            boolean isHome = false;
            for (MapInfo location : nearby) {
                if (location.getSpawnZoneTeamObject() == rc.getTeam()) isHome = true;
            }
            if (isHome) Comms.reportEnemyFlagCaptured(rc, myFlagLocalId(rc));
        }
    }

    public void run() throws GameActionException {
        if (isBuilder()) {
            if (!rc.isSpawned()) {
                setup.spawn();
                Pathfind.moveToward(rc, ALLY_FLAGS[myID - 1], true);
            } else {
                Comms.setFlagDanger(rc, myID - 1, isFlagDanger(rc));
                if (isFlagDanger(rc)) tryAttack();
            }
        } else if (isExplorer()) {
            if (!rc.isSpawned()) setup.spawn();
            tryBuyGlobal();
            tryUpdateInfo();

            // TODO : Configure the logic for the order of attack, movement when flag is in danger.
            tryAttack();
            tryCaptureFlag();
            if (!rc.isSpawned()) return;
            tryHeal();
            // TODO : Configure this tryRebound in main phase .run();

            if (nearestFlag(rc) != null) Pathfind.moveToward(rc, nearestFlag(rc), true);
            else Pathfind.moveToward(rc, new MapLocation(mapWidth/2, mapHeight/2), true);
            if (!rc.isSpawned()) return;

            Pathfind.explore(rc);

            Comms.reportZoneInfo(rc);
        }
    }
}