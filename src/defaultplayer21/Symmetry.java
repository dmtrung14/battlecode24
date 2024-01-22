package defaultplayer21;

import battlecode.common.MapLocation;

public enum Symmetry {
    HORIZONTAL(true, false),
    VERTICAL(false, true),
    ROTATIONAL(true, true);

    public final boolean reflectX;
    public final boolean reflectY;

    Symmetry(boolean reflectX, boolean reflectY) {
        this.reflectX = reflectX;
        this.reflectY = reflectY;
    }

    public MapLocation opposite(MapLocation loc, int width, int height) {
        int x = reflectX ? width - loc.x - 1 : loc.x;
        int y = reflectY ? height - loc.y - 1 : loc.y;
        return new MapLocation(x, y);
    }
}
