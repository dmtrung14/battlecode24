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

//    public void tryAttack() throws GameActionException {
//        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//        MapLocation current = rc.getLocation();
//        Arrays.sort(nearbyEnemies, new Comparator<RobotInfo>() {
//            @Override
//            public int compare(RobotInfo o1, RobotInfo o2) {
//                return Integer.compare(o1.getLocation().distanceSquaredTo(current), o2.getLocation().distanceSquaredTo(current));
//            }
//        });
//        for (RobotInfo robot : nearbyEnemies) {
//            if (robot.hasFlag()) {
//                Pathfind.moveToward(rc, robot.getLocation(), false);
//                if (rc.canAttack(robot.getLocation()))
//                    rc.attack(robot.getLocation());
//            }
//        }
//        for (RobotInfo robot : nearbyEnemies) {
//            if (current.distanceSquaredTo(robot.getLocation()) > 10) {
//                tryHeal();
//                break;
//            } else if (current.distanceSquaredTo(robot.getLocation()) > 8) {
//                tryHeal();
//                builder.waitAndBuildTrapTurn(TrapType.WATER, current, 2);
//                break;
//            } else if (current.distanceSquaredTo(robot.getLocation()) > 6) {
//                tryHeal();
//                builder.waitAndBuildTrapTurn(TrapType.EXPLOSIVE, current, 2);
//                break;
//            } else if (current.distanceSquaredTo(robot.getLocation()) > 4) {
//                builder.waitAndBuildTrapTurn(TrapType.STUN, current, 2);
//            }
//            Pathfind.moveToward(rc, robot.getLocation(), false);
//            if (rc.canAttack(robot.getLocation())) {
//                rc.attack(robot.getLocation());
//            }
//        }
//    }

//    public void tryHeal() throws GameActionException {
//        if (!rc.isSpawned()) return;
//        for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
//            if (rc.canHeal(robot.getLocation()))
//                rc.heal(robot.getLocation());
//        }
//    }

    public void tryUpdateInfo() throws GameActionException {
        if (!rc.isSpawned()) return;
        Comms.updateEnemyFlagPing(rc);
        for (int i = 0; i < 100; i ++ ) Comms.updateZoneInfo(rc, i);

    }



    public void run() throws GameActionException {
        if (rc.getRoundNum() > 400) rc.resign();
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

            tryAttack(rc);
            tryCaptureFlag(rc, builder);
            if (!rc.isSpawned()) return;
            tryHeal(rc);

            if (nearestFlag(rc) != null) Pathfind.moveToward(rc, nearestFlag(rc), true);
            else Pathfind.moveToward(rc, new MapLocation(mapWidth/2, mapHeight/2), true);
            if (!rc.isSpawned()) return;

            Pathfind.explore(rc);

            Comms.reportZoneInfo(rc);
        }
    }
}