package defaultplayer;

import battlecode.common.*;

import java.util.ArrayList;

import defaultplayer.util.ZoneInfo;

public class Comms {
    private static final int BOT_ID_INDEX = 0;
    private static final int SYM_INDEX = 6;
    private static final int FLAG_DANGER_INDEX = 9;
    private static final int ALLY_FLAG_LOC_START_INDEX = 12;
    private static final int ENEMY_FLAG_LOC_START_INDEX = 48;
    private static final int ENEMY_FLAG_ID_START_INDEX = 84;
    private static final int ZONE_START_INDEX = 120;

    private static final int NULL_FLAG_ID = 0xFFF;

    public static void init(RobotController rc) throws GameActionException {
        // initially every symmetry is possible
        setBits(rc, SYM_INDEX, 0b111, 3);
        for (int i = 0; i < 3; i++) {
            setEnemyFlagId(rc, i, NULL_FLAG_ID);
            setFlagLocation(rc, rc.getTeam().opponent(), i, null);
        }
    }

    // can use this to assign robots roles at the start of the game
    // id ranges from 1 to 50
    public static int incrementAndGetId(RobotController rc) throws GameActionException {
        int value = getBits(rc, BOT_ID_INDEX, 6);
        setBits(rc, BOT_ID_INDEX, value + 1, 6);
        return value + 1;
    }

    private static int symmetryId(Symmetry sym) {
        switch (sym) {
            case HORIZONTAL:
                return 0;
            case VERTICAL:
                return 1;
            case ROTATIONAL:
                return 2;
            default:
                throw new RuntimeException();
        }
    }

    // whether a certain map symmetry is possible
    public static boolean isPossibleSymmetry(RobotController rc, Symmetry sym) throws GameActionException {
        return getBits(rc, SYM_INDEX + symmetryId(sym), 1) != 0;
    }

    public static void breakSymmetry(RobotController rc, Symmetry sym) throws GameActionException {
        setBits(rc, SYM_INDEX + symmetryId(sym), 0, 1);
    }

    public static Symmetry[] possibleSymmetries(RobotController rc) throws GameActionException {
        ArrayList<Symmetry> result = new ArrayList<>();
        for (Symmetry sym : Symmetry.values()) {
            if (isPossibleSymmetry(rc, sym)) {
                result.add(sym);
            }
        }
        return (Symmetry[]) result.toArray();
    }

    // whether a given flag is under attack
    // flag is either 0, 1, or 2
    public static boolean isFlagInDanger(RobotController rc, int flag) throws GameActionException {
        return getBits(rc, FLAG_DANGER_INDEX + flag, 1) != 0;
    }

    public static void setFlagDanger(RobotController rc, int flag, boolean inDanger) throws GameActionException {
        setBits(rc, FLAG_DANGER_INDEX + flag, inDanger ? 1 : 0, 1);
    }

    // add timestamps to flag locations?
    public static MapLocation getFlagLocation(RobotController rc, Team team, int flag) throws GameActionException {
        int index = (rc.getTeam() == team ? ALLY_FLAG_LOC_START_INDEX : ENEMY_FLAG_LOC_START_INDEX) + 12 * flag;
        int value = getBits(rc, index, 12);
        int x = value >>> 6;
        int y = value & 0b111111;
        return x > 60 ? null : new MapLocation(x, y);
    }

    public static void setFlagLocation(RobotController rc, Team team, int flag, MapLocation loc) throws GameActionException {
        int x = loc == null ? 61 : loc.x;
        int y = loc == null ? 61 : loc.y;
        int index = (rc.getTeam() == team ? ALLY_FLAG_LOC_START_INDEX : ENEMY_FLAG_ID_START_INDEX) + 12 * flag;
        int value = (x << 6) + y;
        setBits(rc, index, value, 12);
    }

    // we assume that a flag ID fits in 12 bits, which is an internal game engine detail
    private static int getEnemyFlagId(RobotController rc, int flag) throws GameActionException {
        return getBits(rc, ENEMY_FLAG_ID_START_INDEX + 12 * flag, 12);
    }

    private static void setEnemyFlagId(RobotController rc, int flag, int id) throws GameActionException {
        setBits(rc, ENEMY_FLAG_ID_START_INDEX + 12 * flag, id, 12);
    }

    private static void reportEnemyFlag(RobotController rc, int flagId, MapLocation flagLoc) throws GameActionException {
        for (int i = 0; i < 3; i++) {
            int id = getEnemyFlagId(rc, i);
            if (id == NULL_FLAG_ID || id == flagId) {
                setEnemyFlagId(rc, i, flagId);
                setFlagLocation(rc, rc.getTeam().opponent(), i, flagLoc);
                break;
            }
        }
    }

    public static void reportNearbyEnemyFlags(RobotController rc) throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : flags) {
            reportEnemyFlag(rc, flag.getID(), flag.getLocation());
        }
    }

    public static void reportEnemyFlagCaptured(RobotController rc, int flagId) throws GameActionException {
        reportEnemyFlag(rc, flagId, null);
    }

    private static int zoneBitIndex(MapLocation loc) {
        int zoneId = ZoneInfo.getZoneId(loc);
        return ZONE_START_INDEX + zoneId * 9;
    }

    private static int approximate(int num) {
        if (num == 0) return 0;
        else if (num <= 2) return 1;
        else if (num <= 6) return 2;
        else return 3;
    }

    // we only store an approximation to save bits
    // the ranges are: 0, 1-2, 3-6, and 7+
    public static int getZoneRobots(RobotController rc, MapLocation loc, Team team) throws GameActionException {
        int index = zoneBitIndex(loc) + (rc.getTeam() == team ? 0 : 2);
        int result = getBits(rc, index, 2);
        switch (result) {
            case 0: return 0;
            case 1: return 2;
            case 2: return 5;
            case 3: return 10;
            default: throw new RuntimeException();
        }
    }

    public static void setZoneRobots(RobotController rc, MapLocation loc, Team team, int numRobots) throws GameActionException {
        int index = zoneBitIndex(loc) + (rc.getTeam() == team ? 0 : 2);
        int approx = approximate(numRobots);
        setBits(rc, index, approx, 2);
    }

    // again we only store an approximation
    // the ranges are the same as above
    public static int getZoneTraps(RobotController rc, MapLocation loc) throws GameActionException {
        int result = getBits(rc, zoneBitIndex(loc) + 4, 2);
        switch (result) {
            case 0: return 0;
            case 1: return 2;
            case 2: return 5;
            case 3: return 10;
            default: throw new RuntimeException();
        }
    }

    public static void setZoneTraps(RobotController rc, MapLocation loc, int numTraps) throws GameActionException {
        int approx = approximate(numTraps);
        setBits(rc, zoneBitIndex(loc) + 4, approx, 2);
    }

    // timestamp approximation will be off by at most 250 turns
    public static int getZoneTimestamp(RobotController rc, MapLocation loc) throws GameActionException {
        return getBits(rc, zoneBitIndex(loc) + 6, 3) * 250;
    }

    public static void setZoneTimestamp(RobotController rc, MapLocation loc, int turnNum) throws GameActionException {
        setBits(rc, zoneBitIndex(loc) + 6, turnNum / 250, 3);
    }

    public static void reportZoneInfo(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        RobotInfo[] robots = rc.senseNearbyRobots();
        int numAllies = 0;
        int numEnemies = 0;
        for (RobotInfo robot : robots) {
            if (robot.team == rc.getTeam()) numAllies++;
            else numEnemies++;
        }
        setZoneRobots(rc, loc, rc.getTeam(), numAllies);
        setZoneRobots(rc, loc, rc.getTeam().opponent(), numEnemies);
        setZoneTimestamp(rc, loc, rc.getRoundNum());
    }

    public static void getZoneInfo(RobotController rc, int id) throws GameActionException {

    }

    private static int getBits32(RobotController rc, int arrayIndex) throws GameActionException {
        int a = rc.readSharedArray(arrayIndex);
        int b = rc.readSharedArray(arrayIndex + 1);
        return (a << 16) + b;
    }

    private static void setBits32(RobotController rc, int arrayIndex, int value) throws GameActionException {
        rc.writeSharedArray(arrayIndex, value >>> 16);
        rc.writeSharedArray(arrayIndex + 1, value & ((1 << 16) - 1));
    }

    private static int getBits(RobotController rc, int bitIndex, int numBits) throws GameActionException {
        if (numBits > 16) throw new RuntimeException();
        int arrayIndex = bitIndex / 16;
        if (arrayIndex == 63) {
            int bits = rc.readSharedArray(arrayIndex);
            int shift = 16 - numBits - (bitIndex % 16);
            return (bits >>> shift) & ((1 << numBits) - 1);
        } else {
            int bits = getBits32(rc, arrayIndex);
            int shift = 32 - numBits - (bitIndex % 16);
            return (bits >>> shift) & ((1 << numBits) - 1);
        }
    }

    private static void setBits(RobotController rc, int bitIndex, int value, int numBits) throws GameActionException {
        if (numBits > 16) throw new RuntimeException();
        value &= ((1 << numBits) - 1);
        int arrayIndex = bitIndex / 16;
        if (arrayIndex == 63) {
            int bits = rc.readSharedArray(arrayIndex);
            int shift = 16 - numBits - (bitIndex % 16);
            int mask = ((1 << numBits) - 1) << shift;
            int newBits = (bits & ~mask) | (value << shift);
            rc.writeSharedArray(arrayIndex, newBits);
        } else {
            int bits = getBits32(rc, arrayIndex);
            int shift = 32 - numBits - (bitIndex % 16);
            int mask = ((1 << numBits) - 1) << shift;
            int newBits = (bits & ~mask) | (value << shift);
            setBits32(rc, arrayIndex, newBits);
        }
    }
}
