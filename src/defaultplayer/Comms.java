package defaultplayer;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

import java.util.ArrayList;

public class Comms {
    // this wastes a lot of bits but works for now
    private static final int BOT_ID_INDEX = 0;
    private static final int[] SYM_INDICES = { 1, 2, 3 };
    private static final int[] FLAG_DANGER_INDICES = { 4, 5, 6 };
    private static final int[] ALLY_FLAG_LOC_INDICES = { 7, 9, 11 };
    private static final int[] ENEMY_FLAG_LOC_INDICES = { 13, 15, 17 };

    public static void init(RobotController rc) throws GameActionException {
        for (int index : SYM_INDICES) {
            rc.writeSharedArray(index, 1);
        } // <-- not sure what is this for ?
    }

    // can use this to assign robots roles at the start of the game
    public static int incrementAndGetId(RobotController rc) throws GameActionException {
        int id = rc.readSharedArray(BOT_ID_INDEX);
        rc.writeSharedArray(BOT_ID_INDEX, id + 1);
        return id;
    }

    private static int symmetryIndex(Symmetry sym) {
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

    // whether the map might have a certain type of symmetry
    public static boolean getSymmetry(RobotController rc, Symmetry sym) throws GameActionException {
        int index = symmetryIndex(sym);
        return rc.readSharedArray(SYM_INDICES[index]) != 0;
    }

    public static void breakSymmetry(RobotController rc, Symmetry sym) throws GameActionException {
        int index = symmetryIndex(sym);
        rc.writeSharedArray(SYM_INDICES[index], 0);
    }

    public static Symmetry[] possibleSymmetries(RobotController rc) throws GameActionException {
        ArrayList<Symmetry> result = new ArrayList<>();
        for (Symmetry sym : Symmetry.values()) {
            if (getSymmetry(rc, sym)) {
                result.add(sym);
            }
        }
        return (Symmetry[]) result.toArray();
    }

    // whether a given flag is under attack
    // flag is either 0, 1, or 2
    public static boolean getFlagDanger(RobotController rc, int flag) throws GameActionException {
        return rc.readSharedArray(FLAG_DANGER_INDICES[flag]) != 0;
    }

    public static void setFlagDanger(RobotController rc, int flag, boolean value) throws GameActionException {
        rc.writeSharedArray(FLAG_DANGER_INDICES[flag], value ? 1 : 0);
    }

    public static MapLocation getFlagLocation(RobotController rc, Team team, int flag) throws GameActionException {
        int[] flagLocIndices = rc.getTeam() == team ? ALLY_FLAG_LOC_INDICES : ENEMY_FLAG_LOC_INDICES;
        int index = flagLocIndices[flag];
        int x = rc.readSharedArray(index);
        int y = rc.readSharedArray(index + 1);
        return new MapLocation(x, y);
    }

    public static void setFlagLocation(RobotController rc, Team team, int flag, MapLocation loc) throws GameActionException {
        int[] flagLocIndices = rc.getTeam() == team ? ALLY_FLAG_LOC_INDICES : ENEMY_FLAG_LOC_INDICES;
        int index = flagLocIndices[flag];
        rc.writeSharedArray(index, loc.x);
        rc.writeSharedArray(index + 1, loc.y);
    }

}
