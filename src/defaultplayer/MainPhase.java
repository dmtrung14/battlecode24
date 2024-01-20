package defaultplayer;

import battlecode.common.*;
import com.sun.tools.internal.jxc.ap.Const;
import scala.collection.immutable.Stream;

import static defaultplayer.util.CheckWrapper.*;

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
        for (RobotInfo robot : nearbyEnemies) {
            if (robot.hasFlag()) {
                Pathfind.moveToward(rc, robot.getLocation(), false);
                if (rc.canAttack(robot.getLocation()))
                    rc.attack(robot.getLocation());
            }
        }
        for (RobotInfo robot : nearbyEnemies) {
            MapLocation current = rc.getLocation();
            if (current.distanceSquaredTo(robot.getLocation()) > 9) {
                builder.waitAndBuildTrapTurn(TrapType.STUN, current, 2);
                break;
            } else if (current.distanceSquaredTo(robot.getLocation()) > 4) {
                builder.waitAndBuildTrapTurn(TrapType.WATER, current, 2);
                break;
            } else if (current.distanceSquaredTo(robot.getLocation()) > 2) {
                builder.waitAndBuildTrapTurn(TrapType.EXPLOSIVE, current, 2);
                break;
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
        Constants.ENEMY_FLAGS_PING = rc.senseBroadcastFlagLocations();
    }
    public void run() throws GameActionException {
        if (isBuilder()) {
            if (!rc.isSpawned()) {
                setup.spawn();
                Pathfind.moveToward(rc, Constants.ALLY_FLAGS[Constants.myID - 1], false);
            } else {
                if (isFlagDanger(rc) != Constants.IS_MY_FLAG_DANGER) {
                    Comms.setFlagDanger(rc, Constants.myID - 1, isFlagDanger(rc));
                    Constants.IS_MY_FLAG_DANGER = isFlagDanger(rc);
                }
            }
        } else if (isExplorer()) {
            if (!rc.isSpawned()) setup.spawn();
            tryBuyGlobal();
            tryUpdateInfo();

            // TODO : Configure the logic for the order of attack, movement when flag is in danger.

            tryAttack();
            if (!rc.isSpawned()) return;
            tryHeal();
            // TODO : Configure this tryRebound in main phase .run();
            Pathfind.moveToward(rc, new MapLocation(Constants.mapWidth / 2, Constants.mapHeight / 2), false);
            if (!rc.isSpawned()) return;
            Pathfind.explore(rc);
            Comms.reportZoneInfo(rc);
        }
    }
}