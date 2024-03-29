package defaultplayer_intlqualifier;

import battlecode.common.*;

import java.util.ArrayList;

import defaultplayer_intlqualifier.util.ZoneInfo;

import static defaultplayer_intlqualifier.Constants.*;

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

    public static void postTurnQueue(RobotController rc) throws GameActionException{
        for (int i = 0; i < 50; i++) {
            if (rc.readSharedArray(i) == 0){
                rc.writeSharedArray(i, rc.getID());
                myID = i + 1;
                break;
            }
        }
    }

    public static int[] getTurnQueue(RobotController rc) throws GameActionException {
        int[] queue = new int[50];
        for (int i = 0; i < 50; i++) {
            queue[i] = rc.readSharedArray(i);
        }
        return queue;
    }

    public static void clear(RobotController rc) throws GameActionException {
        for (int i = 0; i < 64; i++) {
            rc.writeSharedArray(i, 0);
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
        if (flag < 0 || flag >= 3) throw new RuntimeException();
        return getBits(rc, FLAG_DANGER_INDEX + flag, 1) != 0;
    }

    public static void setFlagDanger(RobotController rc, int flag, boolean inDanger) throws GameActionException {
        if (flag < 0 || flag >= 3) throw new RuntimeException();
        setBits(rc, FLAG_DANGER_INDEX + flag, inDanger ? 1 : 0, 1);
    }

    // add timestamps to flag locations?
    public static MapLocation getFlagLocation(RobotController rc, Team team, int flag) throws GameActionException {
        if (flag < 0 || flag >= 3) throw new RuntimeException();
        int index = (rc.getTeam() == team ? ALLY_FLAG_LOC_START_INDEX : ENEMY_FLAG_LOC_START_INDEX) + 12 * flag;
        int value = getBits(rc, index, 12);
        int x = value >>> 6;
        int y = value & 0b111111;
        return x > 60 ? null : new MapLocation(x, y);
    }

    public static void setFlagLocation(RobotController rc, Team team, int flag, MapLocation loc) throws GameActionException {
        if (flag < 0 || flag >= 3) throw new RuntimeException();
        int x = loc == null ? 61 : loc.x;
        int y = loc == null ? 61 : loc.y;
        int index = (rc.getTeam() == team ? ALLY_FLAG_LOC_START_INDEX : ENEMY_FLAG_LOC_START_INDEX) + 12 * flag;
        int value = (x << 6) + y;
        setBits(rc, index, value, 12);
    }

    // we assume that a flag ID fits in 12 bits, which is an internal game engine detail
    private static int getEnemyFlagId(RobotController rc, int flag) throws GameActionException {
        if (flag < 0 || flag >= 3) throw new RuntimeException();
        return getBits(rc, ENEMY_FLAG_ID_START_INDEX + 12 * flag, 12);
    }

    private static void setEnemyFlagId(RobotController rc, int flag, int id) throws GameActionException {
        if (flag < 0 || flag >= 3) throw new RuntimeException();
        setBits(rc, ENEMY_FLAG_ID_START_INDEX + 12 * flag, id, 12);
    }

    public static MapLocation[] getAllyFlagLocations(RobotController rc) throws GameActionException {
        ArrayList<MapLocation> locs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            MapLocation loc = getFlagLocation(rc, rc.getTeam(), i);
            if (loc != null) locs.add(loc);
        }
        return locs.toArray(new MapLocation[0]);
    }

    public static MapLocation[] getEnemyFlagLocations(RobotController rc) throws GameActionException {
        ArrayList<MapLocation> locs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            MapLocation loc = getFlagLocation(rc, rc.getTeam().opponent(), i);
            if (loc != null) locs.add(loc);
        }
        return locs.toArray(new MapLocation[0]);
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

    // returns an even number between 0 and 30 inclusive
    public static int getZoneRobots(RobotController rc, int id, Team team) throws GameActionException {
        int index = ZONE_START_INDEX + id * 9 + (rc.getTeam() == team ? 0 : 4);
        return 2 * getBits(rc, index, 4);
    }

    public static void setZoneRobots(RobotController rc, MapLocation loc, Team team, int numRobots) throws GameActionException {
        int index = zoneBitIndex(loc) + (rc.getTeam() == team ? 0 : 4);
        setBits(rc, index, Math.min(numRobots / 2, 30), 4);
    }

    public static boolean zoneHasTraps(RobotController rc, MapLocation loc) throws GameActionException {
        return getBits(rc, zoneBitIndex(loc) + 8, 1) != 0;
    }

    public static boolean zoneHasTraps(RobotController rc, int id) throws GameActionException {
        return getBits(rc, ZONE_START_INDEX + 9 * id + 8, 1) != 0;
    }

    public static void setZoneTraps(RobotController rc, MapLocation loc, boolean hasTraps) throws GameActionException {
        setBits(rc, zoneBitIndex(loc) + 8, hasTraps ? 1 : 0, 1);
    }

    public static void resetZoneInfo(RobotController rc) throws GameActionException {
        // we avoid using setBits for efficiency
        // this also assumes zone info is the last thing we store in the array
        int arrayIndex = ZONE_START_INDEX / 16;
        int value = rc.readSharedArray(arrayIndex);
        int shift = 16 - (ZONE_START_INDEX % 16);
        int mask = (1 << shift) - 1;
        rc.writeSharedArray(arrayIndex, value & ~mask);
        for (int i = arrayIndex + 1; i < GameConstants.SHARED_ARRAY_LENGTH; i++) {
            rc.writeSharedArray(i, 0);
        }
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
    }

    public static void updateZoneInfo(RobotController rc, int id) throws GameActionException {
        if (!rc.isSpawned()) return;
        Constants.ZONE_INFO[id].setZoneInfo(
                getZoneRobots(rc, id, rc.getTeam()),
                getZoneRobots(rc, id, rc.getTeam().opponent()),
                zoneHasTraps(rc, id)
        );
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
