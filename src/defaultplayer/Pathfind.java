package defaultplayer;

import battlecode.common.*;

public class Pathfind {
    public static void explore(RobotController rc) throws GameActionException {

    }

    // execute these algorithms within vision range

    public static void BFS(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {
        // Create a queue for BFS
        Queue<MapLocation> queue = new LinkedList<>();
        
        // Create a set to keep track of visited locations
        Set<MapLocation> visited = new HashSet<>();
        
        // Add the start location to the queue and mark it as visited
        queue.add(start);
        visited.add(start);
        
        // Define the directions to search towards the end point
        Direction[] directions = start.directionTo(end).getCardinalDirections();
        
        // Perform BFS
        while (!queue.isEmpty()) {
            MapLocation current = queue.poll();
            
            // Move towards the end point in the defined directions
            for (Direction direction : directions) {
                MapLocation next = current.add(direction);
                
                // Check if the next location is within vision range and passable
                if (rc.canSenseLocation(next) && rc.onTheMap(next) && rc.isPathable(rc.getType(), next)) {
                    // Move to the next location
                    rc.move(direction);
                    
                    // Check if the end point is reached
                    if (next.equals(end)) {
                        return;
                    }
                    
                    // Add the next location to the queue and mark it as visited
                    queue.add(next);
                    visited.add(next);
                }
            }
        }
    }

    public static void DFS(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {

    }

    public static void AStar(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {

    }

    public static void Dijkstra(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {

    }

    public static void Bellman_Ford(RobotController rc, MapLocation start, MapLocation end) throws GameActionException {

    }
        

}