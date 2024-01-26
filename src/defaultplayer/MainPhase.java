package defaultplayer;

import battlecode.common.*;
import defaultplayer.util.ZoneInfo;

import static defaultplayer.Constants.*;
import static defaultplayer.util.CheckWrapper.*;
import static defaultplayer.util.Micro.*;
import static defaultplayer.util.Optimizer.nearestFlag;

public class MainPhase {
    private final RobotController rc;
    private final Setup setup;
    private final Builder builder;

    public MainPhase(RobotController rc) {
        this.rc = rc;
        this.setup = new Setup(rc);
        this.builder = new Builder(rc);
    }

    public void run() throws GameActionException {
        tryBuyGlobal(rc);
        tryUpdateInfo(rc);
        if (!rc.isSpawned()) {
            setup.trySpawn();
            if (!rc.isSpawned()) return;
        }
        if (isBuilder()) {
            Pathfind.moveToward(rc, ALLY_FLAGS[myID - 1], true);
            Comms.setFlagDanger(rc, myID - 1, isFlagInDanger(rc));
            if (isFlagInDanger(rc)) {
                tryAttack(rc);
            }
        } else if (isExplorer()) {
            // TODO : Configure the logic for the order of attack, movement when flag is in danger.
            if (rc.hasFlag()) tryReturnFlag(rc);
            else {
                // TODO: pass these variables as parameters to reduce bytecode use
                FlagInfo[] nearbyEnemyFlags = rc.senseNearbyFlags(-1, OPPONENT);
                MapInfo[] mapInfos = rc.senseNearbyMapInfos();
                RobotInfo[] allies = rc.senseNearbyRobots(-1, ALLY);
                RobotInfo[] enemies = rc.senseNearbyRobots(-1, OPPONENT);
                int actionZone = action(rc);
                if (actionZone != ZoneInfo.getZoneId(rc.getLocation())) {
                    Pathfind.moveToward(rc, ZONE_INFO[actionZone].getCenter(), true);
                }
                tryCaptureFlag(rc, nearbyEnemyFlags);
                tryAttack(rc);
                tryBuildTrap(rc, mapInfos, allies, enemies);
                moveTowardFlag(rc, builder, nearbyEnemyFlags);
                if (!rc.isSpawned()) return;
                tryHeal(rc);
                if (!rc.isSpawned()) return;
                Pathfind.exploreDVD(rc);
            }
            Comms.reportZoneInfo(rc);
        }
    }
}