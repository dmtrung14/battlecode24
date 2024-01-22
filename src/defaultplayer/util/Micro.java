package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Builder;
import defaultplayer.Comms;
import defaultplayer.Constants;
import defaultplayer.Pathfind;

import static defaultplayer.Constants.*;
import static defaultplayer.util.CheckWrapper.*;
import static defaultplayer.util.Optimizer.*;

public class Micro {
    public static int toAttack(RobotController rc) throws GameActionException {
        /* Return level of attack from 0 (retreat) to 3 (all out attack)*/
        /* attack based on distance to flag */
        int attackLv = 0;
        if (rc.senseNearbyFlags(4, rc.getTeam().opponent()).length > 0) attackLv = 3;
        else if (rc.senseNearbyFlags(9, rc.getTeam().opponent()).length > 0) attackLv = 2;
        else if (rc.senseNearbyFlags(16, rc.getTeam().opponent()).length > 0) attackLv = 1;

        /* attack lv based on enemy dynamics */
        // TODO : get nearby enemies and allies with zoning rather than just senseNearbyRobots
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(12, rc.getTeam().opponent());
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(12, rc.getTeam());
        double allyScore = 0;
        double enemyScore = 0;
        for (RobotInfo ally : nearbyAllies) allyScore += (1 + ally.getHealth() * 0.002);
        for (RobotInfo enemy : nearbyEnemies) enemyScore += (1 + enemy.getHealth() * 0.002);
        double ratio = allyScore / enemyScore;
        if (ratio > 1.2) attackLv = 3;
        else if (ratio > 1 || nearbyEnemyHasFlag(rc)) attackLv = Math.max(attackLv, 2);
        else if (ratio > 0.8) attackLv = Math.max(attackLv, 1);
        else attackLv = Math.max(attackLv, 0);

        return attackLv;
    }

    public static void retreat(RobotController rc) throws GameActionException {
        // TODO : handle which direction to retreat;
        MapLocation current = rc.getLocation();
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(10, rc.getTeam().opponent());
        RobotInfo bestEnemy = null;
        double bestScore = 0;
        for (RobotInfo enemy : nearbyEnemies) {
            double enemyScore = enemy.getAttackLevel() + (double) enemy.getHealth() * 0.002;
            if (enemyScore > bestScore) {
                bestScore = enemyScore;
                bestEnemy = enemy;
            }
        }
        if (bestEnemy != null) {
            Direction awayFromEnemy = current.directionTo(bestEnemy.getLocation()).opposite();
            if (rc.canMove(awayFromEnemy)) rc.move(awayFromEnemy);
            else if (rc.canMove(awayFromEnemy.rotateRight())) rc.move(awayFromEnemy.rotateRight());
            else if (rc.canMove(awayFromEnemy.rotateLeft())) rc.move(awayFromEnemy.rotateLeft());
        }
    }

    public static void attackLv3(RobotController rc) throws GameActionException {
        RobotInfo weakestEnemy = nearestEnemy(rc);
        if (weakestEnemy == null) return;
        MapLocation weakestEnemyLoc = weakestEnemy.getLocation();
        MapLocation current = rc.getLocation();
        if (current.distanceSquaredTo(weakestEnemyLoc) <= 4) {
            // then attack, and walk out.
            if (rc.isActionReady()) rc.attack(weakestEnemyLoc);
            if (rc.getLevel(SkillType.ATTACK) == 6 && rc.isActionReady() && rc.canAttack(weakestEnemyLoc)) {
                rc.attack(weakestEnemyLoc);
            }
            retreat(rc);
        } else if (current.distanceSquaredTo(weakestEnemyLoc) <= 7 && rc.isActionReady()) {
            Pathfind.moveToward(rc, weakestEnemyLoc, false);
            if (rc.isActionReady() && rc.canAttack(weakestEnemyLoc)) rc.attack(weakestEnemyLoc);
            tryHeal(rc);
        }
        else if (current.distanceSquaredTo(weakestEnemyLoc) <= 10) {
            /* build stun trap so that we don't get hurt when we walk forward hopefully */
            Direction dirToEnemy = current.directionTo(weakestEnemyLoc);
            if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy))) rc.build(TrapType.STUN, current.add(dirToEnemy));
//            if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy.rotateLeft()))) rc.build(TrapType.STUN, current.add(dirToEnemy.rotateLeft()));
//            else if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy.rotateRight()))) rc.build(TrapType.STUN, current.add(dirToEnemy.rotateRight()));
            Pathfind.moveToward(rc, weakestEnemyLoc, false);
        } else {
            tryHeal(rc);
        }
    }

    public static void attackLv2(RobotController rc) throws GameActionException {
        RobotInfo weakestEnemy = weakestEnemy(rc);
        if (weakestEnemy == null) return;
        MapLocation weakestEnemyLoc = weakestEnemy.getLocation();
        MapLocation current = rc.getLocation();
        if (current.distanceSquaredTo(weakestEnemyLoc) <= 4) {
            if (rc.isActionReady() && rc.canAttack(weakestEnemyLoc)) rc.attack(weakestEnemyLoc);
            if (rc.getLevel(SkillType.ATTACK) == 6 && rc.isActionReady() && rc.canAttack(weakestEnemyLoc)) {
                rc.attack(weakestEnemyLoc);
            }
            retreat(rc);
        }
        else if (current.distanceSquaredTo(weakestEnemyLoc) <= 10) {
            Direction dirToEnemy = current.directionTo(weakestEnemyLoc);
            if (rc.canBuild(TrapType.EXPLOSIVE, current.add(dirToEnemy))) rc.build(TrapType.EXPLOSIVE, current.add(dirToEnemy));
            if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy.rotateLeft()))) rc.build(TrapType.STUN, current.add(dirToEnemy.rotateLeft()));
            else if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy.rotateRight()))) rc.build(TrapType.STUN, current.add(dirToEnemy.rotateRight()));
        } else {
            tryHeal(rc);
        }
    }


    public static void attackLv1(RobotController rc) throws GameActionException {
        RobotInfo weakestEnemy = nearestEnemy(rc);
        if (weakestEnemy == null) return;
        MapLocation weakestEnemyLoc = weakestEnemy.getLocation();
        MapLocation current = rc.getLocation();
        if (current.distanceSquaredTo(weakestEnemyLoc) <= 4) {
            if (rc.isActionReady() && rc.canAttack(weakestEnemyLoc)) rc.attack(weakestEnemyLoc);
            if (rc.getLevel(SkillType.ATTACK) == 6 && rc.isActionReady() && rc.canAttack(weakestEnemyLoc)) {
                rc.attack(weakestEnemyLoc);
            }
            retreat(rc);
        }
        else if (current.distanceSquaredTo(weakestEnemyLoc) <= 10) {
            Direction dirToEnemy = current.directionTo(weakestEnemyLoc);
            if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy))) rc.build(TrapType.STUN, current.add(dirToEnemy));
            if (rc.canBuild(TrapType.WATER, current.add(dirToEnemy.rotateLeft()))) rc.build(TrapType.WATER, current.add(dirToEnemy.rotateLeft()));
            else if (rc.canBuild(TrapType.WATER, current.add(dirToEnemy.rotateRight()))) rc.build(TrapType.WATER, current.add(dirToEnemy.rotateRight()));
            retreat(rc);
        } else {
            tryHeal(rc);
        }
    }

    public static void tryAttack(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        int attackLv = toAttack(rc);
        switch (attackLv) {
            case 0: retreat(rc); break; //PLACEHOLDER
            case 1: attackLv1(rc); break;
            case 2: attackLv2(rc); break;
            case 3: attackLv3(rc); break;
        }
    }

    public static void tryHeal(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        RobotInfo weakest = weakestAlly(rc);
        if (weakest == null) return;
        if (rc.canHeal(weakest.getLocation())) rc.heal(weakest.getLocation());
    }

    public static void tryMoveAwayFromFlagHolder(RobotController rc) throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(2);
        MapLocation flagHolder = nearbyFlagHolder(rc, flags, rc.getLocation());
        if (flagHolder != null) Pathfind.moveAwayFrom(rc, flagHolder);
    }

    public static void tryCaptureFlag(RobotController rc, Builder builder) throws GameActionException {
        if (!rc.isSpawned()) return;
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
//        if (flags.length > 0 && !flags[0].isPickedUp()){
//            Pathfind.moveToward(rc, flags[0].getLocation(), true);
//            if (rc.canPickupFlag(flags[0].getLocation())) rc.pickupFlag(flags[0].getLocation());
//        } else if (flags.length > 0 && flags[0].isPickedUp()) {
//            builder.clearWaterForFlag(flags[0].getLocation(), nearestSpawnZone(rc));
//            Pathfind.moveToward(rc, nearestSpawnZone(rc), true);
//        }
        if (flags.length > 0) {
            for (FlagInfo flag : flags) {
                if (!flag.isPickedUp()) {
                    Pathfind.moveToward(rc, flag.getLocation(), true);
                    if (rc.canPickupFlag(flag.getLocation())) rc.pickupFlag(flag.getLocation());
                }
            }
            if (!rc.hasFlag()) {
                builder.clearWaterForFlag(flags[0].getLocation(), nearestSpawnZone(rc));
                Pathfind.moveToward(rc, nearestSpawnZone(rc), true);
            }
        }
    }

    public static void tryReturnFlag(RobotController rc) throws GameActionException {
        MapLocation spawnZone = nearestSpawnZone(rc);
        // we must report the flag capture before the flag disappears
        if (rc.isMovementReady() && rc.getLocation().isAdjacentTo(spawnZone)) {
            FlagInfo[] flag = rc.senseNearbyFlags(0, rc.getTeam().opponent());
            Comms.reportEnemyFlagCaptured(rc, flag[0].getID());
        }
        Pathfind.moveToward(rc, spawnZone, false);
    }

    public static void tryBuyGlobal(RobotController rc) throws GameActionException {
        if (rc.canBuyGlobal(GlobalUpgrade.ATTACK)) rc.buyGlobal(GlobalUpgrade.ATTACK);
        else if (rc.canBuyGlobal(GlobalUpgrade.HEALING)) rc.buyGlobal(GlobalUpgrade.HEALING);
        else if (rc.canBuyGlobal(GlobalUpgrade.CAPTURING)) rc.buyGlobal(GlobalUpgrade.CAPTURING);
    }


    public static void tryUpdateInfo(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        if (myID == 1 && rc.getRoundNum() % 200 == 0) Comms.resetZoneInfo(rc);
        Comms.reportNearbyEnemyFlags(rc);
        ENEMY_FLAGS_PING = rc.senseBroadcastFlagLocations();
        ENEMY_FLAGS_COMMS = Comms.getEnemyFlagLocations(rc);
        ALLY_FLAGS = Comms.getAllyFlagLocations(rc);
    }

    public static int toReturnAndGuard(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return -1;
        int currentZone = ZoneInfo.getZoneId(rc.getLocation());
        int currentZoneX = currentZone / 10;
        int currentZoneY = currentZone % 10;
        int guard = -1;
//        double mapDiagonal = (new MapLocation(0,0)).distanceSquaredTo((new MapLocation(Constants.mapWidth -1, Constants.mapHeight - 1)));
        double minBalance = 0;
        for (int i = 0; i < 3; i ++ ) {
            if (Comms.isFlagInDanger(rc, i)) {
                int flagZone = ZoneInfo.getZoneId(ALLY_FLAGS[i]);
                int flagZoneX = flagZone / 10;
                int flagZoneY = flagZone % 10;
                /* decide if we should rush back by checking all zones in between the current position
                and the flag, plus the distance in case we have to go too far.
                 */
                for (int zoneX = Math.min(currentZoneX, flagZoneX); zoneX <= Math.max(currentZoneX, flagZoneX); zoneX++) {
                    for (int zoneY = Math.min(currentZoneX, flagZoneX); zoneY <= Math.max(currentZoneY, flagZoneY); zoneY++) {
                        int zone = 10 * zoneX + zoneY;
                        double balance = Constants.ZONE_INFO[zone].getAllies()
                                - Constants.ZONE_INFO[zone].getEnemies();
//                                + 3 * rc.getLocation().distanceSquaredTo(ALLY_FLAGS[i]) / mapDiagonal;
                        if (balance < minBalance) {
                            minBalance = balance;
                            guard = i;
                        }
                    }
                }
            }
        }
        return guard;
    }

    public static boolean toHelpCaptureFlag() {
        return false;
    }

    public static boolean toRushCenter() {
        return false;
    }
}
