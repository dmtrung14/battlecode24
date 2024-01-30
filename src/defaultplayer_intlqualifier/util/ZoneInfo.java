package defaultplayer_intlqualifier.util;

import battlecode.common.*;
import defaultplayer_intlqualifier.Constants;

import java.util.*;

public class ZoneInfo {
    private int address;
    private int allies;
    private int enemies;
    private boolean traps;

    public ZoneInfo() {
        this.allies = 0;
        this.enemies = 0;
        this.traps = false;
    }

    public int getAllies() {
        return this.allies;
    }

    public int getEnemies() {
        return this.enemies;
    }

    public boolean hasTraps() {
        return this.traps;
    }

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

    public void setZoneInfo(int allies, int enemies, boolean traps) {
        this.allies = allies;
        this.enemies = enemies;
        this.traps = traps;
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
