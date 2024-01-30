package defaultplayer_intlqualifier;

import battlecode.common.*;

import java.util.Random;

// the guard unit sits on a flag and reports enemy attacks
public class Guard {
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    private final RobotController rc;
    private final int assignedFlag;
    private final Random rng = new Random(0);

    public Guard(RobotController rc, int assignedFlag) {
        this.rc = rc;
        this.assignedFlag = assignedFlag;
    }

    // returns whether it succeeds
    private boolean spawn() throws GameActionException {
        // TODO: spawn closer to the flag
        MapLocation[] locs = rc.getAllySpawnLocations();
        for (MapLocation loc : locs) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                return true;
            }
        }
        return false;
    }

    private void setupPhaseOneTurn() throws GameActionException {
        // TODO: explore map and collect crumbs
        // right now we move randomly
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    private void moveTowardFlag() throws GameActionException {
        // TODO: pathfind towards assigned flag
        MapLocation flag = Comms.getFlagLocation(rc, rc.getTeam(), assignedFlag);
        Direction dir = rc.getLocation().directionTo(flag);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    private void reportAndAttackEnemies(RobotInfo[] enemies) throws GameActionException {
        Comms.setFlagDanger(rc, assignedFlag, true);
        for (RobotInfo enemy : enemies) {
            if (rc.canAttack(enemy.location)) {
                rc.attack(enemy.location);
                return;
            }
        }
    }

    private void runOneTurn() throws GameActionException {
        if (!rc.isSpawned()) {
            if (!spawn()) return;
        }
        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
            setupPhaseOneTurn();
        } else if (!rc.getLocation().equals(Comms.getFlagLocation(rc, rc.getTeam(), assignedFlag))) {
            moveTowardFlag();
        } else {
            RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (enemies.length > 0) {
                reportAndAttackEnemies(enemies);
            }
        }
        // later: attempt to heal teammates
        // later: handle case when flag is stolen
    }

    public void run() {
        try {
            Comms.init(rc);
        } catch (GameActionException e) {
        }
        while (true) {
            try {
                runOneTurn();
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }
}
