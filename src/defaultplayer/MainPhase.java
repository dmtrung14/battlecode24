package defaultplayer;

import java.awt.*;
import java.util.*;

import battlecode.common.*;
import com.sun.tools.internal.jxc.ap.Const;

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
                Pathfind.moveTowardMain(rc, robot.getLocation(), false);
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
            Pathfind.moveTowardMain(rc, robot.getLocation(), false);
            if (rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
        }
    }

    public void tryHeal() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
            if (rc.canHeal(robot.getLocation()))
                rc.heal(robot.getLocation());
        }
    }

    public void tryRebound(MapLocation center, int depth) throws GameActionException {
        if (!rc.isSpawned()) return;
        Queue<Integer> pastDistance = new LinkedList<Integer>();
        for (int i = 0; i < depth; i++) pastDistance.add(rc.getLocation().distanceSquaredTo(center));
        Direction lastDir = Direction.CENTER;
        MapLocation[] nextLocation = Pathfind.attract(rc, center, pastDistance.remove());
        int counter = 0;
        int avg = (Constants.mapHeight + Constants.mapWidth) / 2;
        while ((nextLocation.length > 0) && counter <= avg) {
            pastDistance.add(rc.getLocation().distanceSquaredTo(center));
            if (nextLocation.length > 0) {
                for (int i = 0; i < nextLocation.length; i++) {
                    if (!rc.isSpawned()) return;
                    Direction dir = rc.getLocation().directionTo(nextLocation[i]);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                        lastDir = dir;
                        break;
                    }
                    if (i == (nextLocation.length - 1) && counter <= avg) {
                        i = -1;
                        counter += 1;
                        Clock.yield();
                    }
                }
                if (!rc.isSpawned()) return;
                nextLocation = Pathfind.attract(rc, center, pastDistance.remove());
            } else {
                if (rc.canMove(lastDir)) rc.move(lastDir);
                else if (rc.canMove(lastDir.rotateLeft())) {
                    rc.move(lastDir.rotateLeft());
                    lastDir = lastDir.rotateLeft();
                } else if (rc.canMove(lastDir.rotateRight())) {
                    rc.move(lastDir.rotateRight());
                    lastDir = lastDir.rotateRight();
                }
            }
            counter += 1;
        }
    }

    public void run() throws GameActionException {
        if (isBuilder()) {
            if (!rc.isSpawned()) {
                setup.spawn();
                tryRebound(Constants.FLAGS[Constants.myID - 1], 2);
            } else {
                if (isFlagDanger(rc) != Constants.IS_MY_FLAG_DANGER) {
                    Comms.setFlagDanger(rc, Constants.myID - 1, isFlagDanger(rc));
                    Constants.IS_MY_FLAG_DANGER = isFlagDanger(rc);
                }
            }
        } else if (isExplorer()) {
            if (!rc.isSpawned()) {
                setup.spawn();
                for (int i = 0; i <= 5; i++) {
                    Pathfind.explore(rc);
                    Clock.yield();
                }
            } else {
                tryBuyGlobal();

                // TODO : Configure the logic for the order of attack, movement when flag is in danger.

                tryAttack();
                tryHeal();
                // TODO : Configure this tryRebound in main phase .run();
                tryRebound(new MapLocation(Constants.mapWidth / 2, Constants.mapHeight / 2), 2);
                if (rc.isSpawned()) {
                    Pathfind.explore(rc);
                    Comms.reportZoneInfo(rc);
                }
            }
        }

    }
}