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
        // dam, secondCoordinateDam;
        // a, b, c, d = 4 corners -> getTeamTerritory
        // distA = min(A -> dam, A->secondDam)
        // distB = min(B -> dam ...)
        Team ourTeam = rc.getTeam();
        MapLocation corner1 = new MapLocation(0,0);
        MapLocation corner2 = new MapLocation(0, RobotPlayer.mapHeight - 1);
        MapLocation corner3 = new MapLocation(RobotPlayer.mapWidth - 1, 0);
        MapLocation corner4 = new MapLocation(RobotPlayer.mapWidth - 1, RobotPlayer.mapHeight - 1);
        MapLocation currentCoorRobot = rc.getLocation();
        int[] distances = {currentCoorRobot.distanceSquaredTo(corner1), currentCoorRobot.distanceSquaredTo(corner2),
                currentCoorRobot.distanceSquaredTo(corner3), currentCoorRobot.distanceSquaredTo(corner4)};

        int minDistance = Collections.min(Arrays.asList(distances));
        if(distanceSquaredTo(corner1, currentCoorRobot) == minDistance){
            Pathfind.BFS(rc, currentCoorRobot, corner1);
            if(rc.getLocation.equals(corner1)){
                return rc.getLocation;
            }
        }

    }

    
}