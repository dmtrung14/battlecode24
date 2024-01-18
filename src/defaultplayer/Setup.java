package defaultplayer;

import battlecode.common.*;

import java.lang.Math;
import java.util.*;

import com.sun.tools.internal.jxc.ap.Const;
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
                Constants.RANDOM = new Random(Constants.myID);
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
        if (rc.canPickupFlag(flag)) {
            rc.pickupFlag(flag);
            Constants.HAS_MOVED_FLAG = true;
        }
        MapLocation center = new MapLocation(Constants.mapWidth / 2, Constants.mapHeight / 2);
        Queue<Integer> pastDistance = new LinkedList<Integer>();
        pastDistance.add(rc.getLocation().distanceSquaredTo(center));
        MapLocation[] nextLocation = Pathfind.avoid(rc, center, pastDistance.remove());
        while (nextLocation.length > 0 && rc.getRoundNum() <= Constants.FLAG_RUSH_ROUNDS) {
            pastDistance.add(rc.getLocation().distanceSquaredTo(center));
            for (int i = 0; i < nextLocation.length; i++){
                Direction dir = rc.getLocation().directionTo(nextLocation[i]);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    break;
                } else if (rc.canFill(nextLocation[i])){
                    rc.dropFlag(rc.getLocation());
                    rc.fill(nextLocation[i]);
                    rc.pickupFlag(rc.getLocation());
                    rc.move(dir);
                    break;
                }
                if (i == (nextLocation.length - 1) && rc.getRoundNum() < Constants.FLAG_RUSH_ROUNDS) {
                    i = -1;
                    Clock.yield();
                } else {
                    break;
                }
            }
            Comms.setFlagLocation(rc, rc.getTeam(), Constants.myID - 1, rc.getLocation());
            nextLocation = Pathfind.avoid(rc, center, pastDistance.remove());
        }
        while (rc.hasFlag()){
            if (rc.canDropFlag(rc.getLocation())) {
                rc.dropFlag(rc.getLocation());
            }
            Clock.yield();
        }
    }

    public void buildAroundFlags() {

    }

    public boolean isBuilder() {
        return contains(Constants.BUILDERS, Constants.myID);
    }
    public boolean isExplorer() {
        return 4 <= Constants.myID;
    }

    public void run() {
        try {
            if (!rc.isSpawned()) {
                spawn();
            }
            if (isBuilder()) {
                if (rc.getRoundNum() <= Constants.FLAG_RUSH_ROUNDS) {
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