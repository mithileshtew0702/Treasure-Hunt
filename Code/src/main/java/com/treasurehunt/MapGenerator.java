package com.treasurehunt;

import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Generates random maps for the Treasure Hunt game.
 * This class creates maze-like maps with walls, treasures, and ensures all treasures are reachable.
 * Saves maps as JSON files for later use in the game.
 */
public class MapGenerator {
    private static final int SIZE = 20;
    private static final int MIN_OBSTACLES = 20;
    private static final int MAX_OBSTACLES = 30;
    private static final int TREASURES = 3;
    private static final String MAP_PREFIX = "map";
    private static final String MAP_EXTENSION = ".json";
    private static final int MAX_MAPS = 1000;

    public static void main(String[] args) {
        generateNextAvailableMap();
    }

    //Generates a new map with the next available filename.
    public static void generateNextAvailableMap() {
        int mapNumber = 1;
        while (mapNumber <= MAX_MAPS) {
            String filename = MAP_PREFIX + mapNumber + MAP_EXTENSION;
            if (!new File(filename).exists()) {
                generateAndSaveMap(filename);
                System.out.println("Successfully created " + filename);
                return;
            }
            mapNumber++;
        }
        System.out.println("Maximum map count (" + MAX_MAPS + ") reached");
    }

    // Generates and saves a new map to the specified filename.
    public static void generateAndSaveMap(String filename) {
        int[][] grid = generateMap();
        saveMapToJson(grid, filename);
    }

    // Generates a new random map with walls, treasures, and guaranteed paths.
    public static int[][] generateMap() {
        int[][] grid = new int[SIZE][SIZE];
        Random rand = new Random();

        // Initialize empty grid
        for (int[] row : grid) {
            Arrays.fill(row, 0);
        }

        // Place player at start position (0,0)
        grid[0][0] = 3;

        // Place walls and ensure path complexity
        placeSmartWalls(grid, rand);

        // Place treasures with guaranteed paths
        placeAccessibleTreasures(grid, rand);

        return grid;
    }

    // Distributes walls across the map using different placement strategies.
    private static void placeSmartWalls(int[][] grid, Random rand) {
        int obstacleCount = MIN_OBSTACLES + rand.nextInt(MAX_OBSTACLES - MIN_OBSTACLES + 1);

        // Place maze-like walls (10%)
        placeMazeWalls(grid, rand, (int)(obstacleCount * 0.1));

        // Place scattered walls (80%)
        placeScatteredWalls(grid, rand, (int)(obstacleCount * 0.8));

        // Place small clusters (10%)
        placeWallClusters(grid, rand, (int)(obstacleCount * 0.1));
    }

    private static void placeMazeWalls(int[][] grid, Random rand, int count) {
        // Create maze-like patterns that create interesting paths
        for (int i = 0; i < count; i++) {
            int x = rand.nextInt(SIZE-2) + 1;
            int y = rand.nextInt(SIZE-2) + 1;

            // Create L-shaped walls
            if (rand.nextBoolean()) {
                grid[x][y] = 1;
                grid[x+1][y] = 1;
                grid[x][y+1] = 1;
            } else {
                grid[x][y] = 1;
                grid[x-1][y] = 1;
                grid[x][y-1] = 1;
            }
        }
    }

    // Places individual walls at random positions.
    private static void placeScatteredWalls(int[][] grid, Random rand, int count) {
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = rand.nextInt(SIZE);
                y = rand.nextInt(SIZE);
            } while (grid[x][y] != 0 || (x == 0 && y == 0));
            grid[x][y] = 1;
        }
    }

    // Places small clusters of walls
    private static void placeWallClusters(int[][] grid, Random rand, int count) {
        for (int i = 0; i < count; i++) {
            int centerX = rand.nextInt(SIZE-3) + 1;
            int centerY = rand.nextInt(SIZE-3) + 1;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (rand.nextDouble() < 0.7) {
                        int x = centerX + dx;
                        int y = centerY + dy;
                        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE && grid[x][y] == 0) {
                            grid[x][y] = 1;
                        }
                    }
                }
            }
        }
    }

    // Places treasures in the map while ensuring they are reachable from the start.
    private static void placeAccessibleTreasures(int[][] grid, Random rand) {
        List<Point> potentialSpots = new ArrayList<>();

        // Find all empty spots in the farther half of the map
        for (int x = SIZE/2; x < SIZE; x++) {
            for (int y = SIZE/2; y < SIZE; y++) {
                if (grid[x][y] == 0) {
                    potentialSpots.add(new Point(x, y));
                }
            }
        }

        Collections.shuffle(potentialSpots, rand);

        // Place treasures and ensure paths
        int placed = 0;
        for (Point spot : potentialSpots) {
            if (placed >= TREASURES) break;

            // Temporarily mark as treasure to test reachability
            grid[spot.x][spot.y] = 2;

            if (isReachable(grid, new Point(0, 0), spot)) {
                placed++;
            } else {
                // If not reachable, clear a path
                clearPath(grid, new Point(0, 0), spot);
                placed++;
            }
        }
    }

    // Checks if a path exists between two points using BFS.
    private static boolean isReachable(int[][] grid, Point from, Point to) {
        boolean[][] visited = new boolean[SIZE][SIZE];
        Queue<Point> queue = new LinkedList<>();
        queue.add(from);
        visited[from.x][from.y] = true;

        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(to)) return true;

            for (int[] dir : directions) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                if (newX >= 0 && newX < SIZE && newY >= 0 && newY < SIZE
                        && !visited[newX][newY] && grid[newX][newY] != 1) {
                    visited[newX][newY] = true;
                    queue.add(new Point(newX, newY));
                }
            }
        }
        return false;
    }

    // Clears a path between two points by removing walls along the optimal path.
    private static void clearPath(int[][] grid, Point from, Point to) {
        // Use A* to find the best path to clear
        List<Point> path = findPath(grid, from, to);
        if (path != null) {
            for (Point p : path) {
                if (grid[p.x][p.y] == 1) {
                    grid[p.x][p.y] = 0;
                }
            }
        }
    }

    // Finds the optimal path between two points using A* algorithm.
    private static List<Point> findPath(int[][] grid, Point start, Point end) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Integer> gScore = new HashMap<>();
        Map<Point, Integer> fScore = new HashMap<>();

        gScore.put(start, 0);
        fScore.put(start, heuristic(start, end));
        openSet.add(new PathNode(start, fScore.get(start)));

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (current.point.equals(end)) {
                return reconstructPath(cameFrom, current.point);
            }

            for (Point neighbor : getNeighbors(grid, current.point)) {
                int tentativeGScore = gScore.get(current.point) + 1;

                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current.point);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristic(neighbor, end));
                    openSet.add(new PathNode(neighbor, fScore.get(neighbor)));
                }
            }
        }
        return null;
    }

    // Gets walkable neighboring points.
    private static List<Point> getNeighbors(int[][] grid, Point p) {
        List<Point> neighbors = new ArrayList<>();
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] dir : directions) {
            int newX = p.x + dir[0];
            int newY = p.y + dir[1];

            if (newX >= 0 && newX < SIZE && newY >= 0 && newY < SIZE
                    && grid[newX][newY] != 1) {
                neighbors.add(new Point(newX, newY));
            }
        }
        return neighbors;
    }

    // Reconstructs the path from the cameFrom map.
    private static List<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
        List<Point> path = new ArrayList<>();
        path.add(current);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }

        return path;
    }

    //Calculates Manhattan distance between two points for A* heuristic.
    private static int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    @SuppressWarnings("unchecked")
    //Saves the generated map to a JSON file.
    private static void saveMapToJson(int[][] grid, String filename) {
        JSONObject mapData = new JSONObject();
        JSONArray gridData = new JSONArray();

        // Create string rows
        for (int y = 0; y < SIZE; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < SIZE; x++) {
                row.append(grid[x][y]);
            }
            gridData.add(row.toString());
        }

        mapData.put("grid", gridData);
        mapData.put("size", SIZE);
        mapData.put("treasures", TREASURES);

        try (FileWriter file = new FileWriter(filename)) {
            file.write(mapData.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // tores a point and its f-score for priority queue comparison
    private static class PathNode implements Comparable<PathNode> {
        Point point;
        int fScore;

        PathNode(Point point, int fScore) {
            this.point = point;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(PathNode other) {
            return Integer.compare(this.fScore, other.fScore);
        }
    }
}