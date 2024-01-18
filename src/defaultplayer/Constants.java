package defaultplayer;

import java.util.*;
import battlecode.common.*;
public class Constants {
    public static Integer[] BUILDERS = {1, 2, 3};

    public static int mapWidth;
    public static int mapHeight;
    public static MapLocation[] FLAGS = new MapLocation[3];

    public static MapLocation[] SPAWN_ZONES;
    public static final int EXPLORE_ROUNDS = 150;
    public static int myID = 0;

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

    public static boolean HAS_MOVED_FLAG = false;
}
