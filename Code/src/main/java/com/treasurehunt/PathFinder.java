package com.treasurehunt;

import java.awt.Point;
import java.util.*;

/**
 * Handles pathfinding algorithms for the Treasure Hunt game.
 * Implements both BFS and A* algorithms to find paths between points on the game grid.
 */
public class PathFinder {
    private final TreasureHunt game;

    public PathFinder(TreasureHunt game) {
        this.game = game;
    }

    // BFS Pathfinding Algorithm
    public List<Point> findBFSPath(Point start, Point end) {
        Queue<Point> queue = new LinkedList<>();
        Map<Point, Point> cameFrom = new HashMap<>();
        Set<Point> visited = new HashSet<>();

       // Initialize with starting position
        queue.add(start);
        visited.add(start);
        cameFrom.put(start, null);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            // Path found
            if (current.equals(end)) {
                return reconstructPath(cameFrom, current);
            }

            // Explore all neighbors
            for (Point neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return null;
    }

    // A* Pathfinding Algorithm
    public List<Point> findAStarPath(Point start, Point end) {
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Integer> gScore = new HashMap<>();
        Map<Point, Integer> fScore = new HashMap<>();

        // Initialize starting node
        gScore.put(start, 0);
        fScore.put(start, heuristicEstimate(start, end));
        openSet.add(new AStarNode(start, fScore.get(start)));

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();

            // Path found
            if (current.point.equals(end)) {
                return reconstructPath(cameFrom, current.point);
            }

            // Evaluate all neighbors
            for (Point neighbor : getNeighbors(current.point)) {
                int tentativeGScore = gScore.get(current.point) + 1;

                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current.point);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristicEstimate(neighbor, end));

                    if (!openSet.contains(new AStarNode(neighbor, 0))) {
                        openSet.add(new AStarNode(neighbor, fScore.get(neighbor)));
                    }
                }
            }
        }
        return null;
    }

    // Manhattan distance heuristic
    private int heuristicEstimate(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    // Get walkable neighboring points
    public List<Point> getNeighbors(Point p) {
        List<Point> neighbors = new ArrayList<>();
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};

        for (int[] dir : directions) {
            int x = p.x + dir[0];
            int y = p.y + dir[1];

            if (x >= 0 && x < TreasureHunt.SIZE && y >= 0 && y < TreasureHunt.SIZE
                    && game.grid[x][y] != TreasureHunt.CellType.WALL) {
                neighbors.add(new Point(x, y));
            }
        }
        return neighbors;
    }

    // Reconstruct the path from cameFrom map
    private List<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
        List<Point> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    // Node class for A* algorithm
    private static class AStarNode implements Comparable<AStarNode> {
        final Point point;
        final int fScore;

        AStarNode(Point point, int fScore) {
            this.point = point;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(AStarNode other) {
            return Integer.compare(this.fScore, other.fScore);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AStarNode node = (AStarNode) obj;
            return point.equals(node.point);
        }

        @Override
        public int hashCode() {
            return point.hashCode();
        }
    }
}