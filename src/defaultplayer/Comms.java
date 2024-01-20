package defaultplayer;

import battlecode.common.*;

import java.util.ArrayList;

import static defaultplayer.util.ZoneInfo.*;

public class Comms {
    private static final int BOT_ID_INDEX = 0;
    private static final int SYM_INDEX = 0;
    private static final int FLAG_DANGER_INDEX = 0;
    private static final int FLAG_LOC_START_INDEX = 1;
    private static final int ZONE_START_INDEX = 7;

    public static void init(RobotController rc) throws GameActionException {
        // initially every symmetry is possible
        rc.writeSharedArray(SYM_INDEX, 0b111 << 7);
    }

    // can use this to assign robots roles at the start of the game
    public static int incrementAndGetId(RobotController rc) throws GameActionException {
        int value = rc.readSharedArray(BOT_ID_INDEX);
        int newValue = value + (1 << 10);
        rc.writeSharedArray(BOT_ID_INDEX, newValue);
        return value >>> 10;
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
        int value = rc.readSharedArray(SYM_INDEX);
        int mask = 1 << (9 - symmetryId(sym));
        return (value & mask) != 0;
    }

    public static void breakSymmetry(RobotController rc, Symmetry sym) throws GameActionException {
        int value = rc.readSharedArray(SYM_INDEX);
        int mask = 1 << (9 - symmetryId(sym));
        int newValue = value & ~mask;
        rc.writeSharedArray(SYM_INDEX, newValue);
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
        int value = rc.readSharedArray(FLAG_DANGER_INDEX);
        int mask = 1 << (6 - flag);
        return (value & mask) != 0;
    }

    public static void setFlagDanger(RobotController rc, int flag, boolean inDanger) throws GameActionException {
        int value = rc.readSharedArray(FLAG_DANGER_INDEX);
        int mask = 1 << (6 - flag);
        int newValue = (value & ~mask) | (inDanger ? mask : 0);
        rc.writeSharedArray(FLAG_DANGER_INDEX, newValue);
    }

    // add timestamps to flag locations?
    public static MapLocation getFlagLocation(RobotController rc, Team team, int flag) throws GameActionException {
        int index = FLAG_LOC_START_INDEX + (rc.getTeam() == team ? 0 : 3) + flag;
        int value = rc.readSharedArray(index);
        int x = value >>> 10;
        int y = (value >>> 4) & 0b111111;
        if (x > 60) return null;
        return new MapLocation(x, y);
    }

    public static void setFlagLocation(RobotController rc, Team team, int flag, MapLocation loc) throws GameActionException {
        int index = FLAG_LOC_START_INDEX + (rc.getTeam() == team ? 0 : 3) + flag;
        int value = (loc.x << 10) + (loc.y << 4);
        rc.writeSharedArray(index, value);
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

    private static int getBits9(RobotController rc, int bitIndex) throws GameActionException {
        int arrayIndex = bitIndex / 16;
        int bits = getBits32(rc, arrayIndex);
        int shift = 32 - 9 - (bitIndex & ((1 << 16) - 1));
        return (bits >>> shift) & 0b111111111;
    }

    private static void setBits9(RobotController rc, int bitIndex, int value) throws GameActionException {
        int arrayIndex = bitIndex / 16;
        int bits = getBits32(rc, arrayIndex);
        int shift = 32 - 9 - (bitIndex & ((1 << 16) - 1));
        int mask = 0b111111111 << shift;
        int newBits = (bits & ~mask) | (value << shift);
        setBits32(rc, arrayIndex, newBits);
    }

    private static int computeBitIndex(MapLocation loc) {
        double zoneWidth = Constants.mapWidth * 0.1;
        double zoneHeight = Constants.mapHeight * 0.1;
        int zoneX = (int) Math.floor(loc.x / zoneWidth);
        int zoneY = (int) Math.floor(loc.y / zoneHeight);
        int zoneId = getZoneId(loc);
        return ZONE_START_INDEX * 16 + zoneId * 9;
    }

    private static int getZoneBits(RobotController rc, MapLocation loc, int shift, int numBits) throws GameActionException {
        int bitIndex = computeBitIndex(loc);
        int bits = getBits9(rc, bitIndex);
        return (bits >>> shift) & ((1 << numBits) - 1);
    }

    private static void setZoneBits(RobotController rc, MapLocation loc, int shift, int numBits, int value) throws GameActionException {
        int bitIndex = computeBitIndex(loc);
        int bits = getBits9(rc, bitIndex);
        int mask = ((1 << numBits) - 1) << shift;
        int newBits = (bits & ~mask) | (value << shift);
        setBits9(rc, bitIndex, newBits);
    }

    // we only store an approximation to save bits
    // the ranges are: 0, 1-2, 3-6, and 7+
    public static int getZoneRobots(RobotController rc, MapLocation loc, Team team) throws GameActionException {
        int shift = 5 + (rc.getTeam() == team ? 2 : 0);
        int result = getZoneBits(rc, loc, shift, 2);
        switch (result) {
            case 0: return 0;
            case 1: return 2;
            case 2: return 5;
            case 3: return 10;
            default: throw new RuntimeException();
        }
    }

    private static int approximate(int num) {
        if (num == 0) return 0;
        else if (num <= 2) return 1;
        else if (num <= 6) return 2;
        else return 3;
    }

    public static void setZoneRobots(RobotController rc, MapLocation loc, Team team, int numRobots) throws GameActionException {
        int shift = 5 + (rc.getTeam() == team ? 2 : 0);
        int approx = approximate(numRobots);
        setZoneBits(rc, loc, shift, 2, approx);
    }

    // again we only store an approximation
    // the ranges are the same as above
    public static int getZoneTraps(RobotController rc, MapLocation loc) throws GameActionException {
        int result = getZoneBits(rc, loc, 3, 2);
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
        setZoneBits(rc, loc, 3, 2, approx);
    }

    // timestamp approximation will be off by at most 250 turns
    public static int getZoneTimestamp(RobotController rc, MapLocation loc) throws GameActionException {
        return getZoneBits(rc, loc, 0, 3) * 250;
    }

    public static void setZoneTimestamp(RobotController rc, MapLocation loc, int turnNum) throws GameActionException {
        setZoneBits(rc, loc, 0, 3, turnNum / 250);
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

    public static void fetchZoneInfo(RobotController rc, int id) throws GameActionException {

    }
}
