package defaultplayer.util;

import battlecode.common.*;
import defaultplayer.Constants;

import java.util.*;

import static defaultplayer.Constants.ALLY;

public class ZoneInfo {
    private final int address;
    private final int zoneX;
    private final int zoneY;
    private int allies;
    private int enemies;
    private boolean traps;
    private int allyFlags;
    private int enemyFlags;

    private double weight;

    public ZoneInfo(int add) {
        address = add;
        zoneX = add / 10;
        zoneY = add % 10;
        allies = 0;
        enemies = 0;
        traps = false;
        allyFlags = 0;
        enemyFlags = 0;
        weight = 1;
    }

    public int getAddress() { return this.address; }

    public int getAllies() {
        return this.allies;
    }

    public void setAllies(int allies) {
        this.allies = allies;
    }

    public int getEnemies() {
        return this.enemies;
    }

    public void setEnemies(int enemies) {
        this.enemies = enemies;
    }

    public boolean hasTraps() {
        return this.traps;
    }

    public void setTraps(boolean traps) {
        this.traps = traps;
    }

    public void setZoneInfo(int allies, int enemies, boolean traps) {
        this.allies = allies;
        this.enemies = enemies;
        this.traps = traps;
    }

    public int getFlags(Team team) { return team == ALLY ? allyFlags : enemyFlags; }

    public void resetFlags(Team team) {
        if (team == ALLY) allyFlags = 0;
        else enemyFlags = 0;
    }

    public void addFlag(Team team) {
        if (team == ALLY) allyFlags++;
        else enemyFlags++;
    }

    public MapLocation getCenter() {
        int zoneWidth = (int) (Constants.mapWidth * 0.1);
        int zoneHeight = (int) (Constants.mapHeight * 0.1);
        return new MapLocation(zoneX * zoneWidth + zoneWidth / 2, zoneY * zoneHeight + zoneHeight / 2);
    }

    public double getWeight() { return this.weight; }

    public void updateWeight(RobotController rc) {
        if (!rc.isSpawned()) return;
        int zoneOfRobot = getZoneId(rc.getLocation());
        int zoneOfRobotX = zoneOfRobot / 10;
        int zoneOfRobotY = zoneOfRobot % 10;
        int dist = Math.abs(zoneOfRobotX - zoneX) + Math.abs(zoneOfRobotY - zoneY);
        this.weight = (double) (Math.sqrt(allyFlags) + Math.pow(enemyFlags, 2) + 1)/(dist + 1);
    }

    public double getScore() {
        return enemies != 0 ? heuristic((double) allies / (enemies + 1), weight) : 0;
    }

    private static double heuristic(double ratio, double weight) {
        double log2ratio = Math.abs(Math.log(ratio) / Math.log(3));
        return 1 - Math.pow(log2ratio, 1.5) + weight;
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
                if (0 <= i && i < 10 && 0 <= j && j < 10) {
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
