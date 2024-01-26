package defaultplayer;

import java.util.*;
import battlecode.common.*;
import defaultplayer.util.FastIterableLocSet;
import defaultplayer.util.ZoneInfo;

public class Constants {
    public static int myID = 0;

    public static int mapWidth;
    public static int mapHeight;

    public static int[] TURN_QUEUE;

    public static final Integer[] BUILDERS = {1, 2, 3};

    public static MapLocation[] SPAWN_ZONES;
    public static MapLocation[] SPAWN_ZONE_CENTERS;

    public static final int FLAG_RUSH_ROUNDS = 100;
    public static final int EXPLORE_ROUNDS = 180;

    public static MapLocation[] ALLY_FLAGS = new MapLocation[3];
    public static MapLocation[] ENEMY_FLAGS_PING;
    public static MapLocation[] ENEMY_FLAGS_COMMS;

    public static final Direction[] DIRECTIONS = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static Random RANDOM;

    public static Team ALLY;
    public static Team OPPONENT;

    public static ZoneInfo[] ZONE_INFO = new ZoneInfo[100];

    public static FastIterableLocSet EXPLORED = new FastIterableLocSet(200);

    public static ArrayList<MapLocation> ENEMY_BORDER_LINE = new ArrayList<MapLocation>();
    public static ArrayList<MapLocation> NEUTRAL_BORDERLINE = new ArrayList<MapLocation>();

    public static int[] BUFFER = new int[64];
    public static int[] BUFFER_GIT = new int[64];

    public static Integer[] FLAG_ZONES;

    public static Integer[] NEIGHBORING_ZONES;
}
