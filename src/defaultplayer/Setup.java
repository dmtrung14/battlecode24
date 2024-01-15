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
            if(rc.senseMapInfo.getTeamTerritory.equals(ourTeam)){
                return corner1;
            }
        }
        
    }

    public static void runSetup() throws GameActionException {

        if(rc.getRoundNum() < EXPLORE_ROUNDS) {
            //pickup flag if possible, explore randomly
            FlagInfo[] flags = rc.senseNearbyFlags(-1);
            for(FlagInfo flag : flags) {
                MapLocation flagLoc = flag.getLocation();
                if(rc.senseMapInfo(flagLoc).isSpawnZone() && rc.canPickupFlag(flagLoc)) {
                    rc.pickupFlag(flag.getLocation());
                }
            }
            Pathfind.explore(rc);
        }
        else {
            //try to place flag if it is far enough away from other flags
            if(rc.senseLegalStartingFlagPlacement(rc.getLocation())) {
                if(rc.canDropFlag(rc.getLocation())) rc.dropFlag(rc.getLocation());
            }
            //move towards flags and place defenses around them
            FlagInfo[] flags = rc.senseNearbyFlags(-1);

            FlagInfo targetFlag = null;
            for(FlagInfo flag : flags) {
                if(!flag.isPickedUp()) {
                    targetFlag = flag;
                    break;
                }
            }

            if(targetFlag != null) {
                Pathfind.moveTowards(rc, targetFlag.getLocation(), false);
                if(rc.getLocation().distanceSquaredTo(flags[0].getLocation()) < 9) {
                    if(rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation())) {
                        rc.build(TrapType.EXPLOSIVE, rc.getLocation());
                    }
                    else {
                        MapLocation waterLoc = rc.getLocation().add(RobotPlayer.directions[RobotPlayer.random.nextInt(8)]);
                        if(rc.canDig(waterLoc)) rc.dig(waterLoc);
                    }
                }
            } 
            else Pathfind.explore(rc);
        }
    }
}