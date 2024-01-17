package defaultplayer;

import battlecode.common.*;

import java.lang.Math;
import java.util.*;
public class Setup {

    private final RobotController rc;
    private final Builder builder;
    private final Random rand;
    public Setup(RobotController rc) {
        this.rc = rc;
        this.builder = new Builder(rc);
        this.rand = new Random(rc.getID());
    }
    public static MapLocation runFindDam() throws GameActionException {
        // get the robot id to see if it should be finding dam
        return new MapLocation(0, 0);
    }

    public void spawn() throws GameActionException {
        while (!rc.isSpawned()){
            if (Constants.myID == 0){
                Constants.myID = Comms.incrementAndGetId(rc);
            }
            else if (Arrays.asList(Constants.BUILDERS).contains(Constants.myID)){
                rc.spawn(Constants.SPAWN_ZONES[Constants.myID - 1]);
            }
            else{
                int randomZone = rand.nextInt(3);
                for (int i = 27; i >= 1; i --){
                    if (rc.canSpawn(Constants.SPAWN_ZONES[(randomZone + i)%27])) {
                        rc.spawn(Constants.SPAWN_ZONES[(randomZone + i)%27]);
                    }
                }
            }

        }
    }

    public void pickupFlag(MapLocation flag) throws GameActionException {
        // TODO: calculate location of main flag
        // right now it's hard coded for the default small map
        builder.moveTo(flag);
        rc.pickupFlag(rc.getLocation());
    }
    // Move the flag to a reasonable position
    public void MoveToGoal(){
        MapLocation center = new MapLocation(Constants.mapWidth/2, Constants.mapHeight/2);
        Direction next = rc.getLocation().directionTo(center).opposite();
        while(rc.canMove(next) || rc.canFill(rc.getLocation().add(next))){
            if(rc.canFill(rc.getLocation().add(next))){
                try {
                    rc.fill(rc.getLocation().add(next));
                } catch (GameActionException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                rc.move(next);
            } catch (GameActionException e) {
                throw new RuntimeException(e);
            }
            next = rc.getLocation().directionTo(center).opposite();
        }
    }
    public void run() {
        try {
            if (!rc.isSpawned()){
                spawn();
            }
            Pathfind.explore(rc);
            // TODO: do something after the setup phase

        } catch (GameActionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}