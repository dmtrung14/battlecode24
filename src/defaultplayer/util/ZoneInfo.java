package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Comms;
import defaultplayer.Constants;

import java.util.*;

public class ZoneInfo {
    private int address;
    private int flags;
    private int allies;
    private int enemies;
    private boolean traps;

    private double weight;
    public ZoneInfo(int add) {
        this.address = add;
        this.allies = 0;
        this.enemies = 0;
        this.traps = false;
        this.weight = 1;
        this.flags = 0;
    }

    public int getAddress() { return this.address;}

    public int getAllies() {
        return this.allies;
    }

    public int getEnemies() {
        return this.enemies;
    }

    public boolean hasTraps() {
        return this.traps;
    }

    public double getWeight() { return this.weight;}

    public int getFlags() {return this.flags;}

    public void setAddress(int address) {
        this.address = address;
    }

    public void setAllies(int allies) {
        this.allies = allies;
    }

    public void setEnemies(int enemies){
        this.enemies = enemies;
    }

    public void setTraps(boolean traps) {
        this.traps = traps;
    }

    public void addFlag() {this.flags += 1;}

    public void setZoneInfo(int allies, int enemies, boolean traps) {
        this.allies = allies;
        this.enemies = enemies;
        this.traps = traps;
    }
    public MapLocation getCenter(){
        int zoneX = address/10;
        int zoneY = address%10;
        int zoneWidth = (int) (Constants.mapWidth * 0.1);
        int zoneHeight = (int) (Constants.mapHeight * 0.1);
        return new MapLocation(zoneX + zoneWidth/2, zoneY + zoneHeight/2);
    }
    public static int getZoneId(MapLocation location) {
        double zoneWidth = Constants.mapWidth * 0.1;
        double zoneHeight = Constants.mapHeight * 0.1;
        int zoneX = (int) Math.floor(location.x / zoneWidth);
        int zoneY = (int) Math.floor(location.y / zoneHeight);
        return zoneX * 10 + zoneY;
    }
    public void setWeight(RobotController rc) {
        if (!rc.isSpawned()) return;
        int ZoneOfRobot = getZoneId(rc.getLocation());
        int ZoneOfRobot_x = ZoneOfRobot / 10;
        int ZoneOfRobot_y = ZoneOfRobot % 10;
        int Zonex = address/10;
        int Zoney = address%10;
        this.weight = Math.exp(getFlags()) / (Math.abs(ZoneOfRobot_x - Zonex) + Math.abs(ZoneOfRobot_y - Zoney));
    }
    public static Integer[] getNeighbors(int zoneID) {
        int row = zoneID / 10;
        int col = zoneID % 10;

        // Initialize empty list for neighbors
        List<Integer> neighbors = new ArrayList<>();

        // Check all surrounding cells within the table bounds
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                // Skip out-of-bounds cells and the cell itself
                if (0 <= i && i < 10 && 0 <= j && j < 10 && !(i == row && j == col)) {
                    // Calculate neighbor number and add it to the list
                    int neighborNum = i * 10 + j;
                    neighbors.add(neighborNum);
                }
            }
        }
        Integer[] result = new Integer[neighbors.size()];
        return neighbors.toArray(result);
    }
}
