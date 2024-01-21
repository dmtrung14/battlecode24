package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Builder;
import defaultplayer.Comms;
import defaultplayer.Constants;
import defaultplayer.Pathfind;

import static defaultplayer.util.Optimizer.*;

public class Micro {
    public static int toAttack(RobotController rc) throws GameActionException {
        /* Return level of attack from 0 (retreat) to 3 (all out attack)*/
        /* attack based on distance to flag */
        int attackLv = Integer.MIN_VALUE;
        if (rc.senseNearbyFlags(4, rc.getTeam()).length > 0) attackLv = 3;
        else if (rc.senseNearbyFlags(9, rc.getTeam()).length > 0) attackLv = 2;
        else if (rc.senseNearbyFlags(16, rc.getTeam()).length > 0) attackLv = 1;

        /* attack lv based on enemy dynamics */
        // TODO : get nearby enemies and allies with zoning rather than just senseNearbyRobots
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
        double allyScore = 0;
        double enemyScore = 0;
        for (RobotInfo ally : nearbyAllies) allyScore += (1 + ally.getHealth() * 0.002);
        for (RobotInfo enemy : nearbyEnemies) enemyScore += (1 + enemy.getHealth() * 0.002);
        double ratio = allyScore / enemyScore;
        if (ratio > 1.2) attackLv = 3;
        else if (ratio > 1.1) attackLv = Math.max(attackLv, 2);
        else if (ratio > 1) attackLv = Math.max(attackLv, 1);
        else attackLv = Math.max(attackLv, 0);


        return attackLv;
    }

    public static void retreat(RobotController rc, Direction dir) throws GameActionException {
        // TODO : handle which direction to retreat;
        if (rc.canMove(dir)) rc.move(dir);
    }

    public static void attackLv3(RobotController rc) throws GameActionException {
        RobotInfo nearestEnemy = nearestEnemy(rc);
        if (nearestEnemy == null) return;
        MapLocation nearestEnemyLoc = nearestEnemy.getLocation();
        MapLocation current = rc.getLocation();
        if (current.distanceSquaredTo(nearestEnemyLoc) <= 2) {
            // then attack, and walk out.
            if (rc.isActionReady()) rc.attack(nearestEnemyLoc);
            if (rc.getLevel(SkillType.ATTACK) == 6 && rc.isActionReady()) {
                rc.attack(nearestEnemyLoc);
            }
            retreat(rc, current.directionTo(nearestEnemyLoc).opposite());
        } else if (current.distanceSquaredTo(nearestEnemyLoc) <= 4 && rc.isActionReady()) {
            Pathfind.moveToward(rc, nearestEnemyLoc, false);
            if (rc.isActionReady() && rc.canAttack(nearestEnemyLoc)) rc.attack(nearestEnemyLoc);
        }
        else if (current.distanceSquaredTo(nearestEnemyLoc) <= 8) {
            /* build stun trap so that we don't get hurt when we walk forward hopefully */
            Direction dirToEnemy = current.directionTo(nearestEnemyLoc);
            if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy))) rc.build(TrapType.STUN, current.add(dirToEnemy));
            if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy.rotateLeft()))) rc.build(TrapType.STUN, current.add(dirToEnemy.rotateLeft()));
            else if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy.rotateRight()))) rc.build(TrapType.STUN, current.add(dirToEnemy.rotateRight()));
            Pathfind.moveToward(rc, nearestEnemyLoc, false);
        } else {
            tryHeal(rc);
        }
    }

    public static void attackLv2(RobotController rc) throws GameActionException {
        RobotInfo nearestEnemy = nearestEnemy(rc);
        if (nearestEnemy == null) return;
        MapLocation nearestEnemyLoc = nearestEnemy.getLocation();
        MapLocation current = rc.getLocation();
        if (current.distanceSquaredTo(nearestEnemyLoc) <= 2) {
            if (rc.isActionReady()) rc.attack(nearestEnemyLoc);
            if (rc.getLevel(SkillType.ATTACK) == 6 && rc.isActionReady()) {
                rc.attack(nearestEnemyLoc);
            }
            retreat(rc, current.directionTo(nearestEnemyLoc).opposite());
        }
        else if (current.distanceSquaredTo(nearestEnemyLoc) <= 6) {
            Direction dirToEnemy = current.directionTo(nearestEnemyLoc);
            if (rc.canBuild(TrapType.EXPLOSIVE, current.add(dirToEnemy))) rc.build(TrapType.EXPLOSIVE, current.add(dirToEnemy));
            if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy.rotateLeft()))) rc.build(TrapType.STUN, current.add(dirToEnemy.rotateLeft()));
            else if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy.rotateRight()))) rc.build(TrapType.STUN, current.add(dirToEnemy.rotateRight()));
        } else {
            tryHeal(rc);
        }
    }

    public static void attackLv1(RobotController rc) throws GameActionException {
        RobotInfo nearestEnemy = nearestEnemy(rc);
        if (nearestEnemy == null) return;
        MapLocation nearestEnemyLoc = nearestEnemy.getLocation();
        MapLocation current = rc.getLocation();
        if (current.distanceSquaredTo(nearestEnemyLoc) <= 2) {
            if (rc.isActionReady()) rc.attack(nearestEnemyLoc);
            if (rc.getLevel(SkillType.ATTACK) == 6 && rc.isActionReady()) {
                rc.attack(nearestEnemyLoc);
            }
            retreat(rc, current.directionTo(nearestEnemyLoc).opposite());
        }
        else if (current.distanceSquaredTo(nearestEnemyLoc) <= 4) {
            Direction dirToEnemy = current.directionTo(nearestEnemyLoc);
            if (rc.canBuild(TrapType.STUN, current.add(dirToEnemy))) rc.build(TrapType.STUN, current.add(dirToEnemy));
            if (rc.canBuild(TrapType.WATER, current.add(dirToEnemy.rotateLeft()))) rc.build(TrapType.WATER, current.add(dirToEnemy.rotateLeft()));
            else if (rc.canBuild(TrapType.WATER, current.add(dirToEnemy.rotateRight()))) rc.build(TrapType.WATER, current.add(dirToEnemy.rotateRight()));
            retreat(rc, current.directionTo(nearestEnemyLoc).opposite());
        } else {
            tryHeal(rc);
        }
    }

    public static void tryAttack(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        int attackLv = toAttack(rc);
        switch (attackLv) {
            case 0: retreat(rc, Direction.NORTH); break; //PLACEHOLDER
            case 1: attackLv1(rc); break;
            case 2: attackLv2(rc); break;
            case 3: attackLv3(rc); break;
            default: break;
        }
    }
    public static void tryHeal(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        RobotInfo weakest = weakestAlly(rc);
        if (weakest == null) return;
        if (rc.canHeal(weakest.getLocation())) rc.heal(weakest.getLocation());
    }

    public static void tryCaptureFlag(RobotController rc, Builder builder) throws GameActionException {
        if (!rc.isSpawned()) return;
        Comms.reportNearbyEnemyFlags(rc);
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        if (flags.length > 0 && !flags[0].isPickedUp()){
            Pathfind.moveToward(rc, flags[0].getLocation(), true);
            if (rc.canPickupFlag(flags[0].getLocation())) rc.pickupFlag(flags[0].getLocation());
        } else if (flags.length > 0 && flags[0].isPickedUp()) {
            builder.clearWaterForFlag(flags[0].getLocation(), nearestSpawnZone(rc));
            Pathfind.moveToward(rc, nearestSpawnZone(rc), true);
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

    public static int toReturnAndGuard(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return -1;
        int currentZone = ZoneInfo.getZoneId(rc.getLocation());
        int guard = -1;
        double mapDiagonal = (new MapLocation(0,0)).distanceSquaredTo((new MapLocation(Constants.mapWidth -1, Constants.mapHeight - 1)));
        double balance = 0;
        for (int i = 0; i < 3; i ++ ) {
            if (Comms.isFlagInDanger(rc, i)) {
                int flagZone = ZoneInfo.getZoneId(Constants.ALLY_FLAGS[i]);
                /* decide if we should rush back by checking all zones in between the current position
                and the flag, plus the distance in case we have to go too far.
                 */
                for (int zone = 0 ; zone < 100; zone ++ ) {
                    if (Math.min(currentZone/10, flagZone/10) <= zone/10 && zone/10 <= Math.max(currentZone/10, flagZone/10) &&
                    Math.min(currentZone % 10, flagZone % 10) <= zone % 10 && zone % 10 <= Math.max(currentZone % 10, flagZone % 10)) {

                        double curBalance = Constants.ZONE_INFO[zone].getAllies() - Constants.ZONE_INFO[zone].getEnemies() + 3 * rc.getLocation().distanceSquaredTo(Constants.ALLY_FLAGS[i]) / mapDiagonal;
                        if (curBalance < balance) {
                            guard = i;
                            balance = curBalance;
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
