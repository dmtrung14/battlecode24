package defaultplayer;

import battlecode.common.*;
import defaultplayer.util.FastIterableLocSet;
import defaultplayer.util.ZoneInfo;

import java.util.*;

import static defaultplayer.Constants.*;
import static defaultplayer.util.Optimizer.*;


public class Pathfind {
    private static MapLocation curTarget = null;
    private static boolean goingAroundObstacle = false;
    private static int obstacleStartDistSquared;
    private static Direction obstacleDir;
    private static boolean obstacleTurningLeft;

    private static Direction DVDDir;

    private static MapLocation center() {
        return new MapLocation(mapWidth / 2, mapHeight / 2);
    }

    public static void spread(RobotController rc) throws GameActionException {
        ArrayList<Direction> possible = new ArrayList<>();
        for (Direction dir : DIRECTIONS) if (rc.canMove(dir)) possible.add(dir);
        if (!possible.isEmpty()) {
            rc.move(possible.get(RANDOM.nextInt(possible.size())));
        }
    }

    public static void explore(RobotController rc) throws GameActionException {
        collectCrumbs(rc);
        moveAwayFromEdge(rc);
        moveAwayFromAllies(rc);
        moveRandomly(rc);
    }

    public static void exploreDVD(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return; // <-- I don't quite need it but to make sure
        MapLocation current = rc.getLocation();
        EXPLORED.add(current);
        bfsInSight(rc, current);
        if (DVDDir == null) DVDDir = current.directionTo(center);
        MapLocation newLoc = current.add(DVDDir);
        if (rc.canMove(DVDDir) && !EXPLORED.contains(newLoc)) {
            rc.move(DVDDir);
        } else if ((newLoc.x + newLoc.y) % 2 == 0 && rc.canFill(newLoc)){
            rc.fill(newLoc);
            if (rc.canMove(DVDDir)) rc.move(DVDDir);
        }else {
            Direction newDir = DVDDir;
            for (int i = 0; i < 8; i ++ ){
                newDir = (myID + rc.getRoundNum()) % 2 == 0 ? newDir.rotateLeft() : newDir.rotateRight();
                newLoc = current.add(newDir);
                if (rc.canMove(newDir)) {
                    rc.move(newDir);
                    DVDDir = newDir;
                    break;
                } else if ((newLoc.x + newLoc.y) % 2 == 0 && rc.canFill(newLoc)){
                    rc.fill(newLoc);
                    if (rc.canMove(newDir)) rc.move(newDir);
                }
            }
        }
    }

    public static void bfsInSight(RobotController rc, MapLocation center) throws GameActionException {
        Queue<MapLocation> queue = new LinkedList<>();
        FastIterableLocSet visited = new FastIterableLocSet(1000);
        int distance = 0;
        int neutral = Integer.MAX_VALUE;
        queue.add(center);
        while (!queue.isEmpty() && distance <= 3) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                MapLocation loc = queue.remove();
                visited.add(loc);
                for (Direction dir : DIRECTIONS) {
                    assert loc != null;
                    MapLocation newLoc = loc.add(dir);
                    if (!rc.canSenseLocation(newLoc) || visited.contains(newLoc)) continue;
                    MapInfo newLocInfo = rc.senseMapInfo(newLoc);
                    if (newLocInfo.getTeamTerritory() == rc.getTeam().opponent() && !newLocInfo.isWall()) {
                        ENEMY_BORDER_LINE.add(newLoc);
                        return;
                    }
                    else if (!newLocInfo.isWall() && !newLocInfo.isDam() && newLocInfo.getTeamTerritory() == Team.NEUTRAL)
                        NEUTRAL_BORDERLINE.add(newLoc);
                    if (Clock.getBytecodesLeft() < 500) return;
                    else queue.add(newLoc);
                }
            }
            distance += 1;
        }
    }

    private static void collectCrumbs(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
        if (crumbs.length > 0) {
            moveToward(rc, crumbs[0], true);
        }
    }

    private static void moveAwayFromEdge(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) return;
        MapLocation loc = rc.getLocation();
        if (rc.getLocation().x < 3) loc = loc.add(Direction.EAST);
        if (rc.getLocation().x > Constants.mapWidth - 4) loc = loc.add(Direction.WEST);
        if (rc.getLocation().y < 3) loc = loc.add(Direction.NORTH);
        if (rc.getLocation().y > Constants.mapHeight - 4) loc = loc.add(Direction.SOUTH);
        if (!rc.getLocation().equals(loc)) moveToward(rc, loc, true);
    }

    private static void moveAwayFromAllies(RobotController rc) throws GameActionException {
        moveAwayFromTeam(rc, rc.getTeam());
    }

    public static void moveAwayFromTeam(RobotController rc, Team team) throws GameActionException {
        if (!rc.isSpawned()) return;
        MapLocation loc = rc.getLocation();
        RobotInfo[] robots = rc.senseNearbyRobots(-1, team);
        for (RobotInfo robot : robots) {
            Direction dir = rc.getLocation().directionTo(robot.location).opposite();
            loc = loc.add(dir);
        }
        if (!rc.getLocation().equals(loc)) {
            Direction dir = rc.getLocation().directionTo(loc);
            if (rc.canMove(dir)) rc.move(dir);
        }
    }

    private static Direction randomDir() {
        return DIRECTIONS[Constants.RANDOM.nextInt(8)];
    }

    private static void moveRandomly(RobotController rc) throws GameActionException {
        Direction dir = randomDir();
        if (rc.canMove(dir)) rc.move(dir);
    }

    public static void moveToward(RobotController rc, MapLocation target, boolean fill) throws GameActionException {
        moveToward(rc, target, fill, false);
    }

    public static void moveToward(RobotController rc, MapLocation target, boolean fill, boolean safe) throws GameActionException {
        if (!rc.isSpawned()) return;
        if (!rc.isMovementReady()) return;
        if (rc.getLocation().equals(target)) return;
        if (!target.equals(curTarget)) {
            curTarget = target;
            goingAroundObstacle = false;
        }
        rc.setIndicatorDot(target, 255, 0, 0);
        Direction bestDir = bellmanFord(rc, target, fill, safe);
        if (bestDir != null) {
            FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
            int leftDist = rc.adjacentLocation(bestDir.rotateLeft().rotateLeft()).distanceSquaredTo(target);
            int rightDist = rc.adjacentLocation(bestDir.rotateRight().rotateRight()).distanceSquaredTo(target);
            Direction perpDir = leftDist < rightDist ? bestDir.rotateLeft().rotateLeft() : bestDir.rotateRight().rotateRight();
            Direction[] dirs = { bestDir, bestDir.rotateLeft(), bestDir.rotateRight(), perpDir };
            for (Direction dir : dirs) {
                if (fill && rc.canFill(rc.adjacentLocation(dir))) rc.fill(rc.adjacentLocation(dir));
                if (rc.canMove(dir) && (rc.hasFlag() || nearbyFlagHolder(rc, flags, rc.adjacentLocation(dir)) == null)) {
                    rc.move(dir);
                    break;
                }
            }
        } else {
            bugNav(rc, target, fill);
        }
    }

    public static void moveAwayFrom(RobotController rc, MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target).opposite();
        if (rc.canMove(dir)) rc.move(dir);
        else if (rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
        else if (rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
    }

    private static boolean isPassable(RobotController rc, MapLocation loc, boolean fill) throws GameActionException {
        if (!rc.canSenseLocation(loc)) return false;
        MapInfo info = rc.senseMapInfo(loc);
        return info.isPassable() || fill && info.isWater();
    }

    private static void bugNav(RobotController rc, MapLocation target, boolean fill) throws GameActionException {
        if (!goingAroundObstacle) {
            MapLocation current = rc.getLocation();
            Direction dir = current.directionTo(target);
            MapLocation loc = current.add(dir);
            if (isPassable(rc, loc, fill)) {
                if (fill && rc.canFill(loc)) rc.fill(loc);
                if (rc.canMove(dir)) rc.move(dir);
                // if there is a robot in the way, try to go around it
                else if (rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
                else if (rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
            } else {
                goingAroundObstacle = true;
                obstacleStartDistSquared = current.distanceSquaredTo(target);
                obstacleDir = dir;
                // turn towards the center
                MapLocation center = new MapLocation(Constants.mapWidth / 2, Constants.mapHeight / 2);
                int leftDist = rc.adjacentLocation(dir.rotateLeft()).distanceSquaredTo(center);
                int rightDist = rc.adjacentLocation(dir.rotateRight()).distanceSquaredTo(center);
                obstacleTurningLeft = leftDist < rightDist;
            }
        }
        if (goingAroundObstacle) {
            // otherwise, try to go around obstacle
            for (int i = 0; i < 8; i++) {
                MapLocation loc = rc.adjacentLocation(obstacleDir);
                if (isPassable(rc, loc, fill)) {
                    if (fill && rc.canFill(rc.adjacentLocation(obstacleDir))) rc.fill(rc.adjacentLocation(obstacleDir));
                    if (rc.canMove(obstacleDir)) {
                        rc.move(obstacleDir);
                        obstacleDir = obstacleTurningLeft ? obstacleDir.rotateRight().rotateRight() : obstacleDir.rotateLeft().rotateLeft();
                    }
                    // if there is a robot in the way, do nothing
                    break;
                } else {
                    obstacleDir = obstacleTurningLeft ? obstacleDir.rotateLeft() : obstacleDir.rotateRight();
                }
            }
            int distSquared = rc.getLocation().distanceSquaredTo(target);
            if (distSquared < obstacleStartDistSquared) {
                goingAroundObstacle = false;
            }
        }
    }

    public static Direction bellmanFord(RobotController rc, MapLocation target, boolean fill, boolean safe) throws GameActionException {
        if (!rc.isSpawned()) return null;
        if (rc.getLocation().equals(target)) return null;
        MapLocation start = rc.getLocation();
        long START_BIT = coordsToBit(3, 3);
        long[] reachable = new long[15];
        reachable[0] = START_BIT;

        long passable = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                MapLocation loc = coordsToLoc(start, x, y);
                if (isPassable(rc, loc, fill)) {
                    passable |= coordsToBit(x, y);
                }
            }
        }

        for (int i = 1; i < reachable.length; i++) {
            reachable[i] = neighborhood(reachable[i - 1]) & passable;
        }

        long targetBit;
        int x = 3 + target.x - start.x;
        int y = 3 + target.y - start.y;
        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
            // if target is within vision range, try to move toward it
            targetBit = coordsToBit(x, y);
        } else {
            // use a heuristic to pick a square on the vision boundary to move to
            targetBit = findBestTarget(reachable, start, target, safe);
        }

        if (!isReachable(reachable, targetBit)) return null;
        int cost = computeCost(reachable, targetBit);
        while (cost > 1) {
            targetBit = neighborhood(targetBit) & reachable[cost - 1];
            cost--;
        }

        // there can be multiple fastest directions, so pick the one that minimizes distance to the target
        int minDist = Integer.MAX_VALUE;
        Direction minDir = null;
        for (Direction dir : Direction.allDirections()) {
            if ((targetBit & translate(START_BIT, dir)) != 0) {
                int dist = rc.adjacentLocation(dir).distanceSquaredTo(target);
                if (dist < minDist) {
                    minDist = dist;
                    minDir = dir;
                }
            }
        }
        return minDir;
    }

    private static long findBestTarget(long[] reachable, MapLocation start, MapLocation target, boolean safe) {
//        long VISION_BOUNDARY = 0xFF818181818181FFL;
        long VISION_BOUNDARY = 0xFF818181818386FCL;
        double bestHeuristic = Double.MIN_VALUE;
        long bestTargetBit = 0;
        double initialDist = Math.sqrt(start.distanceSquaredTo(target));
        // optimize this if needed
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                long locBit = coordsToBit(x, y);
                if ((locBit & VISION_BOUNDARY) != 0 && isReachable(reachable, locBit)) {
                    int cost = computeCost(reachable, locBit);
                    MapLocation loc = coordsToLoc(start, x, y);
                    double newDist = Math.sqrt(loc.distanceSquaredTo(target));
                    double heuristic = (initialDist - newDist) / cost;
                    if (safe) {
                        int zoneId = ZoneInfo.getZoneId(loc);
                        ZoneInfo info = ZONE_INFO[zoneId];
                        double enemies = info.getEnemies();
                        heuristic += 1 / (1 + enemies);
                    }
                    if (heuristic > bestHeuristic) {
                        bestHeuristic = heuristic;
                        bestTargetBit = locBit;
                    }
                }
            }
        }
        return bestTargetBit;
    }

    private static int computeCost(long[] reachable, long locBit) {
        int cost = 0;
        while ((locBit & reachable[cost]) == 0) cost++;
        return cost;
    }

    private static long translate(long bit, Direction dir) {
        switch (dir) {
            case NORTHWEST: return bit >>> 7;
            case NORTH: return bit >>> 8;
            case NORTHEAST: return bit >>> 9;
            case WEST: return bit << 1;
            case CENTER: return bit;
            case EAST: return bit >>> 1;
            case SOUTHWEST: return bit << 9;
            case SOUTH: return bit << 8;
            case SOUTHEAST: return bit << 7;
        }
        throw new RuntimeException();
    }

    private static long neighborhood(long bits) {
        long RIGHT_EDGE = 0x0101010101010101L;
        long LEFT_EDGE = 0x8080808080808080L;
        bits = bits | ((bits << 1) & ~RIGHT_EDGE) | ((bits >>> 1) & ~LEFT_EDGE);
        bits = bits | (bits << 8) | (bits >>> 8);
        return bits;
    }

    private static boolean isReachable(long[] reachable, long locBit) {
        return (locBit & reachable[reachable.length - 1]) != 0;
    }

    private static long coordsToBit(int x, int y) {
        int shift = 8 * (7 - y) + (7 - x);
        return 1L << shift;
    }

    private static MapLocation coordsToLoc(MapLocation start, int x, int y) {
        return new MapLocation(start.x + x - 3, start.y + y - 3);
    }

}