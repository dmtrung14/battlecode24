package defaultplayer;

import battlecode.common.*;
import java.lang.Math;
public class Setup {

    private static final int EXPLORE_ROUNDS = 150;

    public Setup(RobotController rc) {
        this.rc = rc;
    }

    public static MapLocation runFindDam() throws GameActionException {
        // get the robot id to see if it should be finding dam
        int id = rc.getID();
    }
    
    public static MapLocation corner(MapLocation dam){
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
        int minDistance = min(distanceSquaredTo(corner1, currentCoorRobot), distanceSquaredTo(corner2, currentCoorRobot)
                                distanceSquaredTo(corner3, currentCoorRobot), distanceSquaredTo(corner4, currentCoorRobot));
        if(distanceSquaredTo(corner1, currentCoorRobot) == minDistance){
            Pathfind.BFS(rc, currentCoorRobot, corner1);
            if(rc.getLocation.equals(corner1)){
                return rc.getLocation;
            }
        }
        
    }

    
}