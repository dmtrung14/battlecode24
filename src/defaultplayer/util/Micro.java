package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Builder;
import defaultplayer.Comms;
import defaultplayer.Constants;
import defaultplayer.Pathfind;


import static defaultplayer.Constants.*;
import static defaultplayer.util.CheckWrapper.*;
import static defaultplayer.util.Optimizer.*;
import static defaultplayer.util.ZoneInfo.getZoneId;

public class Micro {
    public static int action(RobotController rc) {
        int bestZone = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 100; i++) {

            if (!isCriticalZone(i)) continue;
            ZoneInfo zone = ZONE_INFO[i];
            double score = zone.getScore();
            if (score > bestScore) {
                bestScore = score;
                bestZone = zone.getAddress();
            }
        }
        return bestZone;
    }

    public static int toAttack(RobotController rc) throws GameActionException {
        /* Return level of attack from 0 (retreat) to 3 (all out attack)*/
        /* attack based on distance to flag */
        int attackLv = 0;
        if (rc.senseNearbyFlags(4, OPPONENT).length > 0) attackLv = 3;
        else if (rc.senseNearbyFlags(9, OPPONENT).length > 0) attackLv = 2;
        else if (rc.senseNearbyFlags(16, OPPONENT).length > 0) attackLv = 1;

        /* attack lv based on enemy dynamics */
        // TODO : get nearby enemies and allies with zoning rather than just senseNearbyRobots
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(12, OPPONENT);
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(12, ALLY);
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
        MapLocation current = rc.getLocation();
        int safest = Integer.MAX_VALUE;
        Direction safestDir = Direction.CENTER;
        for (Direction dir : DIRECTIONS) {
            if (!rc.canMove(dir)) continue;
            MapLocation newLoc = current.add(dir);
            int numEnemies = rc.senseNearbyRobots(newLoc, 4, OPPONENT).length;
            if (numEnemies < safest) safestDir = dir;
        }
        if (rc.canMove(safestDir)) rc.move(safestDir);
    }

    public static void attackLv3(RobotController rc) throws GameActionException {
        RobotInfo weakestEnemy = weakestEnemy(rc);
        if (weakestEnemy == null) return;
        MapLocation weakestEnemyLoc = weakestEnemy.getLocation();
        MapLocation current = rc.getLocation();
        if (current.distanceSquaredTo(weakestEnemyLoc) <= 4) {
            // then attack, and walk out.
            if (rc.isActionReady()) rc.attack(weakestEnemyLoc);
            if (rc.getLevel(SkillType.ATTACK) == 6 && rc.canAttack(weakestEnemyLoc)) {
                rc.attack(weakestEnemyLoc);
            }
            retreat(rc);
        } else if (current.distanceSquaredTo(weakestEnemyLoc) <= 7 && rc.isActionReady()) {
            Pathfind.moveToward(rc, weakestEnemyLoc, false);
            if (rc.canAttack(weakestEnemyLoc)) rc.attack(weakestEnemyLoc);
            tryHeal(rc);
        } else if (current.distanceSquaredTo(weakestEnemyLoc) <= 10) {
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
        } else {
            tryHeal(rc);
        }
    }

    public static void attackLv1(RobotController rc) throws GameActionException {
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
        } else if (current.distanceSquaredTo(weakestEnemyLoc) <= 10) {
            retreat(rc);
        } else {
            tryHeal(rc);
        }
    }

    public static void tryAttack(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        int attackLv = toAttack(rc);
        switch (attackLv) {
            case 0:
                retreat(rc);
                break; //PLACEHOLDER
            case 1:
                attackLv1(rc);
                break;
            case 2:
                attackLv2(rc);
                break;
            case 3:
                attackLv3(rc);
                break;
        }
    }

    public static void tryHeal(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        RobotInfo weakest = weakestAllySurvive(rc);
        if (weakest == null) return;
        if (rc.canHeal(weakest.getLocation())) rc.heal(weakest.getLocation());
    }

    public static void tryBuildTrap(RobotController rc, MapInfo[] mapInfos, RobotInfo[] allies, RobotInfo[] enemies) throws GameActionException {
        for (MapInfo info : mapInfos) {
            if (info.getTrapType() != TrapType.NONE) return;
        }
        if (allies.length <= 1 && enemies.length >= 4 && rc.canBuild(TrapType.WATER, rc.getLocation())) {
            rc.build(TrapType.WATER, rc.getLocation());
            return;
        }
        if (enemies.length > 0) {
            boolean build = true;
            for (RobotInfo enemy : enemies) {
                if (enemy.location.isWithinDistanceSquared(rc.getLocation(), 16)) {
                    build = false;
                    break;
                }
            }
            if (build && rc.canBuild(TrapType.WATER, rc.getLocation())) {
                rc.build(TrapType.WATER, rc.getLocation());
                return;
            }
        }
        // build explosive trap?
        if (rc.canBuild(TrapType.STUN, rc.getLocation())) {
            rc.build(TrapType.STUN, rc.getLocation());
        }
    }

    public static void tryMoveAwayFromFlagHolder(RobotController rc) throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(2);
        MapLocation flagHolder = nearbyFlagHolder(rc, flags, rc.getLocation());
        if (flagHolder != null) Pathfind.moveAwayFrom(rc, flagHolder);
    }

    public static void moveTowardFlag(RobotController rc, Builder builder, FlagInfo[] flags) throws GameActionException {
        if (!rc.isSpawned()) return;
        if (flags.length > 0) Pathfind.moveToward(rc, flags[0].getLocation(), true);
//        if (flags.length > 0 && !rc.hasFlag()) {
//            builder.clearWaterForFlag(flags[0].getLocation(), nearestSpawnZone(rc));
//            Pathfind.moveToward(rc, nearestSpawnZone(rc), true);
//        }
    }

    public static void tryCaptureFlag(RobotController rc, FlagInfo[] nearbyFlags) throws GameActionException {
        if (!rc.isSpawned()) return;
        for (FlagInfo flag : nearbyFlags) {
            if (!flag.isPickedUp() && rc.canPickupFlag(flag.getLocation()) && rc.isMovementReady()) {
                rc.pickupFlag(flag.getLocation());
                // we might be standing in our spawn zone already
                if (!rc.hasFlag()) Comms.reportEnemyFlagCaptured(rc, flag.getID());
                else tryReturnFlag(rc);
                break;
            }
        }
    }

    public static void tryReturnFlag(RobotController rc) throws GameActionException {
        MapLocation spawnZone = nearestSpawnZone(rc);
        int flagId = rc.senseNearbyFlags(0, OPPONENT)[0].getID();
        Pathfind.moveToward(rc, spawnZone, false, true);
        if (!rc.hasFlag()) {
            Comms.reportEnemyFlagCaptured(rc, flagId);
            return;
        }
        if (!rc.isActionReady()) return;
        // try to ferry the flag
        Direction dir = Pathfind.bellmanFord(rc, spawnZone, false, true);
        if (dir == null) return;
        MapLocation loc = rc.adjacentLocation(dir);
        RobotInfo[] allies = rc.senseNearbyRobots(loc, 2, ALLY);
        for (RobotInfo ally : allies) {
            if (ally.ID != rc.getID() && ally.health >= 500 && rc.canDropFlag(loc)) {
                rc.dropFlag(loc);
                break;
            }
        }
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
        NEIGHBORING_ZONES = ZoneInfo.getNeighbors(getZoneId(rc.getLocation()));
        updateFlagZones(rc);
//        updateZoneInfo(rc);
    }

    public static void updateFlagZones(RobotController rc) throws GameActionException {
        FLAG_ZONES = new int[ENEMY_FLAGS_PING.length + ENEMY_FLAGS_COMMS.length + ALLY_FLAGS.length];
        int index = 0;
        for (MapLocation loc : ENEMY_FLAGS_PING) {
            FLAG_ZONES[index] = getZoneId(loc);
            index += 1;
        }
        for (MapLocation loc : ENEMY_FLAGS_COMMS) {
            FLAG_ZONES[index] = getZoneId(loc);
            index += 1;
        }
        for (MapLocation loc : ALLY_FLAGS) {
            FLAG_ZONES[index] = getZoneId(loc);
            index += 1;
        }
    }

    public static void updateZoneInfo(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        int zoneOfRobot = getZoneId(rc.getLocation());
        int zoneOfRobotX = zoneOfRobot / 10;
        int zoneOfRobotY = zoneOfRobot % 10;

        for (int i = 0; i < 100; i++) {
            if (!isCriticalZone(i)) continue;
            ZONE_INFO[i].setZoneInfo(
                    Comms.getZoneRobotsAlly(rc, i),
                    Comms.getZoneRobotsOpponent(rc, i),
                    false
            );

            ZONE_INFO[i].updateWeight(zoneOfRobotX, zoneOfRobotY);
            ZONE_INFO[i].allyFlags = 0;
            ZONE_INFO[i].enemyFlags = 0;
        }
        for (MapLocation loc : ENEMY_FLAGS_PING) {
            int id = getZoneId(loc);
            ZONE_INFO[id].enemyFlags++;
        }
        for (MapLocation loc : ENEMY_FLAGS_COMMS) {
            int id = getZoneId(loc);
            if (id < 100) ZONE_INFO[id].enemyFlags++;
        }
        for (MapLocation loc : ALLY_FLAGS) {
            int id = getZoneId(loc);
            ZONE_INFO[id].allyFlags++;
        }
    }

    public static int toReturnAndGuard(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return -1;
        int currentZone = getZoneId(rc.getLocation());
        int currentZoneX = currentZone / 10;
        int currentZoneY = currentZone % 10;
        int guard = -1;
//        double mapDiagonal = (new MapLocation(0,0)).distanceSquaredTo((new MapLocation(Constants.mapWidth -1, Constants.mapHeight - 1)));
        double minBalance = 0;
        for (int i = 0; i < 3; i++) {
            if (Comms.isFlagInDanger(rc, i)) {
                int flagZone = getZoneId(ALLY_FLAGS[i]);
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
}
