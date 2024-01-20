package defaultplayer;

import java.util.*;
import battlecode.common.*;
import defaultplayer.util.ZoneInfo;

public class Constants {
    public static int mapWidth;
    public static int mapHeight;

    public static Integer[] BUILDERS = {1, 2, 3};

    public static int NULL_COOR = 61;
    public static MapLocation[] ALLY_FLAGS = new MapLocation[3];

    public static MapLocation[] SPAWN_ZONES;
    public static final int EXPLORE_ROUNDS = 150;

    public static final int FLAG_RUSH_ROUNDS = 60;
    public static int myID = 0;

    public static MapLocation[] ENEMY_FLAGS = new MapLocation[3];
    public static MapLocation[] ENEMY_FLAGS_PING = new MapLocation[3];

    public static Integer[] ENEMY_FLAGS_ID = new Integer[3];

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
    public static boolean HAS_MOVED_FLAG = false;
    public static boolean IS_MY_FLAG_DANGER = false;

    public static int KNOWN_ENEMY_FLAGS = -1;
    public static ZoneInfo[] ZONE_INFO = new ZoneInfo[100];

}
