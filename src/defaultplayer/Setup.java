package defaultplayer;

import battlecode.common.*;
import scala.collection.Map;

import java.lang.Math;
import java.util.*;
public class Setup {

    private static final int EXPLORE_ROUNDS = 150;
    private static final int CORNER;
    private final RobotController rc;

    public Setup(RobotController rc) {
        this.rc = rc;
    }

    public static MapLocation runFindDam() throws GameActionException {
        // get the robot id to see if it should be finding dam
        int id = rc.getID();
        return new MapLocation(0, 0);
    }

    public static MapLocation getMainFlag() throws GameActionException {
        return new MapLocation(0, 0);
    }

    public static MapLocation spawnNearMain() throws GameActionException {
        
    }
    
    public MapLocation findCorner(MapLocation dam){
        MapLocation secondCoordinateDam = new MapLocation(RobotPlayer.mapWidth - dam.x, RobotPlayer.mapHeight - dam.y);
        Team ourTeam = rc.getTeam();
        MapLocation corner1 = new MapLocation(0,0);
        MapLocation corner2 = new MapLocation(0, RobotPlayer.mapHeight - 1);
        MapLocation corner3 = new MapLocation(RobotPlayer.mapWidth - 1, 0);
        MapLocation corner4 = new MapLocation(RobotPlayer.mapWidth - 1, RobotPlayer.mapHeight - 1);
        MapLocation currentCoorRobot = rc.getLocation();
        // Process to find corner
        Map<MapLocation, Integer> mp = new HashMap<MapLocation, Integer>();
        mp.push(corner1, distanceSquaredTo(corner1, currentCoorRobot));
        mp.push(corner2, distanceSquaredTo(corner2, currentCoorRobot));
        mp.push(corner3, distanceSquaredTo(corner3, currentCoorRobot));
        mp.push(corner4, distanceSquaredTo(corner4, currentCoorRobot));
        Set<MapLocation> key = map.keySet();
        for (MapLocation key : set){
            Pathfind.BFS(rc, currentCoorRobot, key);
            if(rc.getLocation().equals(key)){
                return key;
            }
            else{
                mp.remove(key);
            }
        }
    }

    
}