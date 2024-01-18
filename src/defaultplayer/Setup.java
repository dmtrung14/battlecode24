package defaultplayer;

import battlecode.common.*;

import java.lang.Math;
import java.util.*;

import com.sun.tools.javac.comp.Check;
import defaultplayer.util.CheckWrapper.*;
import scala.Int;

import static defaultplayer.util.CheckWrapper.contains;

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
        while (!rc.isSpawned()) {
            if (Constants.myID == 0) {
                Constants.myID = Comms.incrementAndGetId(rc);
            } else if (contains(Constants.BUILDERS, Constants.myID)) {
                rc.spawn(Constants.SPAWN_ZONES[9 * (Constants.myID - 1) + 4]); //
            } else {
                int randomZone = rand.nextInt(3);
                for (int i = 27; i >= 1; i--) {
                    if (rc.canSpawn(Constants.SPAWN_ZONES[(randomZone + i) % 27])) {
                        rc.spawn(Constants.SPAWN_ZONES[(randomZone + i) % 27]);
                    }
                }
            }

        }
    }

    //    public void pickupFlag(MapLocation flag) throws GameActionException {
//        // TODO: calculate location of main flag
//        // right now it's hard coded for the default small map
//        builder.moveTo(flag);
//        rc.pickupFlag(rc.getLocation());
//    }
    // Move the flag to a reasonable position
    public void moveToGoal() throws GameActionException {
        MapLocation flag = rc.senseNearbyFlags(-1, rc.getTeam())[0].getLocation();
        if (Constants.HAS_MOVED_FLAG) return;
        else Constants.HAS_MOVED_FLAG = true;
        if (rc.canPickupFlag(flag)) {
            rc.pickupFlag(flag);
        } else if (!rc.hasFlag()) {
            Clock.yield();
        }
        MapLocation center = new MapLocation(Constants.mapWidth / 2, Constants.mapHeight / 2);
        Queue<Integer> pastDistance = new LinkedList<Integer>();
        pastDistance.add(rc.getLocation().distanceSquaredTo(center));
        MapLocation[] nextLocation = Pathfind.avoid(rc, center, pastDistance.remove());
        while (nextLocation.length > 0) {
            pastDistance.add(rc.getLocation().distanceSquaredTo(center));
            for (int i = 0; i < nextLocation.length; i++){
                Direction dir = rc.getLocation().directionTo(nextLocation[i]);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                } else if (rc.canFill(nextLocation[i])){
                    rc.dropFlag(rc.getLocation());
                    rc.fill(nextLocation[i]);
                    rc.pickupFlag(rc.getLocation());
                    rc.move(dir);
                }
            }
            nextLocation = Pathfind.avoid(rc, center, pastDistance.remove());
        }
        if (rc.hasFlag()) {
            rc.dropFlag(rc.getLocation());
        }
    }

    public void buildAroundFlags() {

    }

//    Direction next = rc.getLocation().directionTo(center).opposite();
//    int pastDistance = center.distanceSquaredTo(flag);
//        while (rc.canMove(next) || rc.canMove(next.rotateLeft()) || rc.canMove(next.rotateRight())
//            || rc.canFill(rc.getLocation().add(next))) {
//        if (rc.canFill(rc.getLocation().add(next))) {
//            try {
//                rc.dropFlag(rc.getLocation());
//                MapLocation Flag_location = rc.getLocation();
//                rc.fill(rc.getLocation().add(next));
//                rc.pickupFlag(Flag_location);
//            } catch (GameActionException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        if (rc.canMove(next)) {
//            rc.move(next);
//        } else if (rc.canMove(next.rotateRight())) {
//            rc.move(next.rotateRight());
//        } else {
//            rc.move(next.rotateLeft());
//        }
//        next = rc.getLocation().directionTo(center).opposite();
//    }

    public boolean isBuilder() {
        return contains(Constants.BUILDERS, Constants.myID);
    }
    public boolean isExplorer() {
        return 4 <= Constants.myID && Constants.myID <= 4 + (Constants.mapHeight + Constants.mapWidth)/2;
    }

    public void run() {
        try {
            if (!rc.isSpawned()) {
                spawn();
            }
            if (isBuilder()) {
                if (rc.getRoundNum() <= Constants.EXPLORE_ROUNDS) {
                    moveToGoal();
                }
            } else if (isExplorer()) {
                Pathfind.explore(rc);
            }
            // TODO: do something after the setup phase

        } catch (GameActionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}