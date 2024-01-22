package defaultplayer21.util;

import battlecode.common.*;
import defaultplayer21.Constants;

import java.util.*;

public class ZoneInfo {
    private int ADDRESS;
    private int ALLIES;

    private int ENEMIES;

    private int TRAPS;

    public ZoneInfo() {
        this.ALLIES = 0;
        this.ENEMIES = 0;
        this.TRAPS = 0;
    }

    public int getEnemies() {
        return this.ENEMIES;
    }
    public int getAllies() {
        return this.ALLIES;
    }

    public int getTraps() {
        return this.TRAPS;
    }

    public void setAddress(int address) {
        this.ADDRESS = address;
    }

    public void setAllies(int allies) {
        this.ALLIES = allies;
    }

    public void setEnemies(int enemies){
        this.ENEMIES = enemies;
    }

    public void setTraps(int traps) {
        this.TRAPS = traps;
    }
    public void setZoneInfo(int allies, int enemies, int traps) {
        this.ALLIES = allies;
        this.ENEMIES = enemies;
        this.TRAPS = traps;
    }

    public static int getZoneId(MapLocation location) {
        double zoneWidth = Constants.mapWidth * 0.1;
        double zoneHeight = Constants.mapHeight * 0.1;
        int zoneX = (int) Math.floor(location.x / zoneWidth);
        int zoneY = (int) Math.floor(location.y / zoneHeight);
        return zoneX * 10 + zoneY;
    }

    public static Integer[] getNeighbors(int zoneID) {
        int row = zoneID / 10;
        int col = zoneID % 10;

        // Initialize empty list for neighbors
        List<Integer> neighbors = new ArrayList<Integer>();

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
