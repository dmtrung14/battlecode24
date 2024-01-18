package defaultplayer;

import battlecode.common.*;

public class MainPhase {
    private final RobotController rc;

    public MainPhase(RobotController rc) {
        this.rc = rc;
    }
    public void run() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length > 0 && rc.canAttack(enemies[0].getLocation())) {
            rc.attack(enemies[0].getLocation());
        }
    }
}
