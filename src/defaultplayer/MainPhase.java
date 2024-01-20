package defaultplayer;

import battlecode.common.*;
import com.sun.tools.internal.jxc.ap.Const;
import scala.collection.immutable.Stream;

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
        for (int i = 0;  i < 3; i ++ ) {
            MapLocation opponentFlag = Comms.getFlagLocation(rc, rc.getTeam().opponent(), 2 - i);
            MapLocation[] flagPings = rc.senseBroadcastFlagLocations();
            if (i < flagPings.length ) ENEMY_FLAGS_PING[i] = flagPings[i];
            else if (opponentFlag!= null) {
                ENEMY_FLAGS_PING[i] = opponentFlag;
            } else {
                ENEMY_FLAGS_PING[i] = new MapLocation(NULL_COOR, NULL_COOR);
            }
            if (opponentFlag != null && 2 - i> KNOWN_ENEMY_FLAGS) {
                KNOWN_ENEMY_FLAGS = 2 - i;
                ENEMY_FLAGS[2 - i] = opponentFlag;
            }
        }
    }

    public void tryCaptureFlag() throws GameActionException {
        if (!rc.isSpawned()) return;
        if (!rc.hasFlag()) {
            FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
            if (flags.length > 0 && !flags[0].isPickedUp()){
                if (!contains(ENEMY_FLAGS, flags[0].getLocation())) {
                    KNOWN_ENEMY_FLAGS += 1;
                    ENEMY_FLAGS[KNOWN_ENEMY_FLAGS] = flags[0].getLocation();
                    Comms.setFlagLocation(rc, rc.getTeam().opponent(), KNOWN_ENEMY_FLAGS, flags[0].getLocation());
                }
                Pathfind.moveToward(rc, flags[0].getLocation(), true);
                if (rc.canPickupFlag(flags[0].getLocation())) rc.pickupFlag(flags[0].getLocation());
            } else if (flags.length > 0  && flags[0].isPickedUp()) {
                builder.clearWaterForFlag(flags[0].getLocation(), nearestSpawnZone(rc));
                Pathfind.moveToward(rc, nearestSpawnZone(rc), true);
            }
        }
        if (rc.hasFlag()) {
            Pathfind.moveToward(rc, nearestSpawnZone(rc), true);
            MapInfo[] nearby = rc.senseNearbyMapInfos(2);
            boolean isHome = false;
            for (MapInfo location : nearby) if (location.getSpawnZoneTeamObject() == rc.getTeam()) isHome = true;
            if (!isHome) Comms.setFlagLocation(rc, rc.getTeam().opponent(), myFlagLocalId(rc), rc.getLocation());
            else Comms.setFlagLocation(rc, rc.getTeam().opponent(), myID -1, new MapLocation(NULL_COOR, NULL_COOR));
        }
    }
    public void run() throws GameActionException {
        if (isBuilder()) {
            if (!rc.isSpawned()) {
                setup.spawn();
                Pathfind.moveToward(rc, ALLY_FLAGS[myID - 1], true);
            } else {
                if (isFlagDanger(rc)) tryAttack();
                if (isFlagDanger(rc) != IS_MY_FLAG_DANGER) {
                    Comms.setFlagDanger(rc, myID - 1, isFlagDanger(rc));
                    IS_MY_FLAG_DANGER = isFlagDanger(rc);
                }
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