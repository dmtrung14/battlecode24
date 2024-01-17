package defaultplayer;

import battlecode.common.*;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class Map {
    public static HashSet<MapLocation> wall_locs = new HashSet<>();

    public static HashSet<MapLocation> crumbs_locs = new HashSet<>();
    public static HashSet<MapLocation> dam_locs = new HashSet<>();
    private static RobotController rc = null;

    public void Deployment(int myID) {
        if (myID <= 5) {
            return;
        } else {
            int index = myID - 4;
        }
    }

    public void move(MapLocation coor_current_robot, MapLocation goal) throws GameActionException {
        int x_diff = goal.x - coor_current_robot.x;
        int y_diff = goal.y - coor_current_robot.y;
        // in case a robot go diagonally
        if (x_diff > 0 && y_diff > 0) { // go up and right
            while (x_diff != 0 || y_diff != 0) {
                MapLocation next = rc.getLocation().add(Direction.NORTHEAST);
                if (rc.canMove(Direction.NORTHEAST)) {
                    rc.move(Direction.NORTHEAST);
                    x_diff = goal.x - rc.getLocation().x;
                    y_diff = goal.y - rc.getLocation().y;
                } else if (rc.canFill(next)) { // there is a water area in front
                    rc.fill(next);
                } else if (rc.senseMapInfo(next).isDam()) { // dam
                    dam_locs.add(next);
                    break;
                }
                //TODO : how to move when there is a wall in front
            }
        } else if (x_diff < 0 && y_diff > 0) { // go up and left
            while (x_diff != 0 || y_diff != 0) {
                MapLocation next = rc.getLocation().add(Direction.NORTHWEST);
                if (rc.canMove(Direction.NORTHWEST)) {
                    rc.move(Direction.NORTHWEST);
                    x_diff = goal.x - rc.getLocation().x;
                    y_diff = goal.y - rc.getLocation().y;
                } else if (rc.canFill(next)) {
                    rc.fill(next);
                } else if (rc.senseMapInfo(next).isDam()) { // dam
                    dam_locs.add(next);
                    break;
                }
            }
        } else if (x_diff < 0 && y_diff < 0) {
            while (x_diff != 0 || y_diff != 0) {
                MapLocation next = rc.getLocation().add(Direction.SOUTHWEST);
                if (rc.canMove(Direction.SOUTHWEST)) {
                    rc.move(Direction.SOUTHWEST);
                    x_diff = goal.x - rc.getLocation().x;
                    y_diff = goal.y - rc.getLocation().y;
                } else if (rc.canFill(next)) {
                    rc.fill(next);
                } else if (rc.senseMapInfo(next).isDam()) { // dam
                    dam_locs.add(next);
                    break;
                }
            }
        } else {
            while (x_diff != 0 || y_diff != 0) {
                MapLocation next = rc.getLocation().add(Direction.SOUTHEAST);
                if (rc.canMove(Direction.SOUTHEAST)) {
                    rc.move(Direction.SOUTHEAST);
                    x_diff = goal.x - rc.getLocation().x;
                    y_diff = goal.y - rc.getLocation().y;
                } else if (rc.canFill(next)) {
                    rc.fill(next);
                } else if (rc.senseMapInfo(next).isDam()) { // dam
                    dam_locs.add(next);
                    break;
                }
            }
        }
        // in case a robot go straight
        if (x_diff == 0 && y_diff > 0) {
            while (y_diff > 0) {
                MapLocation next = rc.getLocation().add(Direction.NORTH);
                if (rc.canMove(Direction.NORTH)) {
                    rc.move(Direction.NORTH);
                    y_diff = goal.y - rc.getLocation().y;
                } else if (rc.canFill(next)) {
                    rc.fill(next);
                } else if (rc.senseMapInfo(next).isDam()) { // dam
                    dam_locs.add(next);
                    break;
                }
            }
        } else if (x_diff == 0 && y_diff < 0) {
            while (y_diff < 0) {
                MapLocation next = rc.getLocation().add(Direction.SOUTH);
                if (rc.canMove(Direction.SOUTH)) {
                    rc.move(Direction.SOUTH);
                    y_diff = goal.y - rc.getLocation().y;
                } else if (rc.canFill(next)) {
                    rc.fill(next);
                } else if (rc.senseMapInfo(next).isDam()) { // dam
                    dam_locs.add(next);
                    break;
                }
            }
        } else if (x_diff > 0 && y_diff == 0) {
            while (x_diff > 0) {
                MapLocation next = rc.getLocation().add(Direction.EAST);
                if (rc.canMove(Direction.EAST)) {
                    rc.move(Direction.EAST);
                    x_diff = goal.x - rc.getLocation().x;
                } else if (rc.canFill(next)) {
                    rc.fill(next);
                } else if (rc.senseMapInfo(next).isDam()) { // dam
                    dam_locs.add(next);
                    break;
                }
            }
        } else if (x_diff < 0 && y_diff == 0) {
            while (x_diff < 0) {
                MapLocation next = rc.getLocation().add(Direction.WEST);
                if (rc.canMove(Direction.WEST)) {
                    rc.move(Direction.WEST);
                    x_diff = goal.x - rc.getLocation().x;
                } else if (rc.canFill(next)) {
                    rc.fill(next);
                } else if (rc.senseMapInfo(next).isDam()) { // dam
                    dam_locs.add(next);
                    break;
                }
            }
        }
    }

}