package com.treasurehunt;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Main game class for the Treasure Hunt game.
 * This class handles the window, game loop, map loading, UI, and game logic.
 */
public class TreasureHunt extends JFrame {
    // Grid size (20x20) and cell dimensions
    static final int SIZE = 20;
    private static final int CELL_SIZE = 30;
    static final int TREASURES = 3;

    // Each cell can have a type
    protected enum CellType {
        EMPTY, // Walkable space
        WALL, // Obstacle
        TREASURE, // Collectible target
        PLAYER, // Current Player position
        PATH // Hint path marker
    }

    // Used to track whether a cell is visible to the player or hidden
    protected enum Visibility {
        HIDDEN, // Cell is not seen yet
        VISIBLE // Cell is revealed
    }

    // Game state variables
    protected CellType[][] grid;
    protected Visibility[][] visibility;
    protected Point playerPos;
    protected int score = 100;
    protected int treasuresFound = 0;
    protected JLabel scoreLabel;
    protected JLabel timeLabel;
    protected JPanel gamePanel;
    private PlayerMovement playerMovement;
    private PathFinder pathFinder;
    private long startTime;
    private Timer gameTimer;
    private boolean isGameComplete = false;
    private JLabel instructionsLabel;

    public TreasureHunt() {
        setTitle("Treasure Hunt");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(SIZE * CELL_SIZE + 50, SIZE * CELL_SIZE + 130);
        setLayout(new BorderLayout());

        // Try loading a random map from JSON files
        int[][] generatedMap = loadRandomMap();
        if (generatedMap == null) {
            JOptionPane.showMessageDialog(this, "Failed to load map file. Exiting...");
            System.exit(1);
        }

        // Set up the game grid based on the loaded map
        initializeGame(generatedMap);

        // Create a custom JPanel to draw the game board
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };
        gamePanel.setPreferredSize(new Dimension(SIZE * CELL_SIZE, SIZE * CELL_SIZE));
        gamePanel.setFocusable(true);

        // Set up input and pathfinding handlers
        playerMovement = new PlayerMovement(this);
        pathFinder = new PathFinder(this);

        // Listener for arrow key presses to move the player
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                playerMovement.handleKeyPress(e);
            }
        });

        scoreLabel = new JLabel("Score: " + score + " | Treasures: " + treasuresFound + "/" + TREASURES);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

        timeLabel = new JLabel("Time: 00:00");
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Hint buttons
        JButton bfsHintButton = new JButton("BFS Hint (-3)");
        bfsHintButton.addActionListener(e -> {
            showBFSHint();
            gamePanel.requestFocusInWindow();
        });

        JButton aStarHintButton = new JButton("A* Hint (-3)");
        aStarHintButton.addActionListener(e -> {
            showAStarHint();
            gamePanel.requestFocusInWindow();
        });

        // UI layout setup
        JPanel controlPanel = new JPanel(new GridLayout(2, 1));
        JPanel scorePanel = new JPanel();
        scorePanel.add(scoreLabel);
        scorePanel.add(timeLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(bfsHintButton);
        buttonPanel.add(aStarHintButton);

        instructionsLabel = new JLabel("Use Arrow Keys: ↑ → ↓ ← to move");
        instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionsLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(scorePanel, BorderLayout.CENTER);
        topPanel.add(instructionsLabel, BorderLayout.SOUTH);

        controlPanel.add(topPanel);
        controlPanel.add(buttonPanel);

        add(gamePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
        gamePanel.requestFocusInWindow();

        // Start the game timer
        startTime = System.currentTimeMillis();
        gameTimer = new Timer(1000, e -> updateTimer());
        gameTimer.start();
    }

    // Updates the timer display every second
    private void updateTimer() {
        if (!isGameComplete) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long seconds = (elapsedTime / 1000) % 60;
            long minutes = (elapsedTime / (1000 * 60)) % 60;
            timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
        }
    }

    // Loads a random map file from local JSON files
    protected int[][] loadRandomMap() {
        File dir = new File(".");
        File[] files = dir.listFiles((d, name) -> name.startsWith("map") && name.endsWith(".json"));

        if (files == null || files.length == 0) {
            MapGenerator.generateNextAvailableMap();
            files = dir.listFiles((d, name) -> name.startsWith("map") && name.endsWith(".json"));
            if (files == null || files.length == 0) {
                return null;
            }
        }

        // Pick a random file and parse it into a 2D map array
        Random rand = new Random();
        File mapFile = files[rand.nextInt(files.length)];

        try {
            JSONParser parser = new JSONParser();
            JSONObject mapData = (JSONObject) parser.parse(new FileReader(mapFile));
            JSONArray gridData = (JSONArray) mapData.get("grid");

            int[][] map = new int[SIZE][SIZE];
            for (int y = 0; y < SIZE; y++) {
                String row = (String) gridData.get(y);
                for (int x = 0; x < SIZE; x++) {
                    map[x][y] = Character.getNumericValue(row.charAt(x));
                }
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Converts the loaded integer map into cell types, and sets the player's starting position
    protected void initializeGame(int[][] generatedMap) {
        grid = new CellType[SIZE][SIZE];
        visibility = new Visibility[SIZE][SIZE];
        playerPos = new Point(0, 0);

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                switch (generatedMap[i][j]) {
                    case 0: grid[i][j] = CellType.EMPTY; break;
                    case 1: grid[i][j] = CellType.WALL; break;
                    case 2: grid[i][j] = CellType.TREASURE; break;
                    case 3:
                        grid[i][j] = CellType.PLAYER;
                        playerPos = new Point(i, j);
                        break;
                    default: grid[i][j] = CellType.EMPTY;
                }
                visibility[i][j] = Visibility.HIDDEN;
            }
        }
        visibility[playerPos.x][playerPos.y] = Visibility.VISIBLE;
    }

    // Paints the entire game board based on grid and visibility
    protected void drawGrid(Graphics g) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                int pixelX = x * CELL_SIZE;
                int pixelY = y * CELL_SIZE;

                if (visibility[x][y] == Visibility.VISIBLE) {
                    switch (grid[x][y]) {
                        case WALL:
                            g.setColor(Color.BLACK);
                            break;
                        case TREASURE:
                            g.setColor(Color.YELLOW);
                            break;
                        case PATH:
                            g.setColor(Color.GREEN);
                            break;
                        case EMPTY:
                        case PLAYER:
                            g.setColor(Color.WHITE);
                            break;
                    }
                    g.fillRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                }
                g.setColor(Color.GRAY);
                g.drawRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
            }
        }

        // Draw player on top
        int px = playerPos.x * CELL_SIZE;
        int py = playerPos.y * CELL_SIZE;
        g.setColor(Color.BLUE);
        g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.GRAY);
        g.drawRect(px, py, CELL_SIZE, CELL_SIZE);
    }

    // Resets all PATH cells back to EMPTY
    protected void clearPathMarkers() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == CellType.PATH) {
                    grid[i][j] = CellType.EMPTY;
                }
            }
        }
    }

    // Uses BFS to give the next step towards the nearest treasure
    protected void showBFSHint() {
        if (score < 3) {
            JOptionPane.showMessageDialog(this, "Not enough points!");
            return;
        }

        Point nearestTreasure = findNearestTreasure();
        if (nearestTreasure == null) {
            JOptionPane.showMessageDialog(this, "No treasures left!");
            return;
        }

        clearPathMarkers();
        repaint();

        List<Point> path = pathFinder.findBFSPath(playerPos, nearestTreasure);
        if (path != null && path.size() > 1) {
            Point nextStep = path.get(1);

            if (grid[nextStep.x][nextStep.y] != CellType.WALL) {
                grid[nextStep.x][nextStep.y] = CellType.PATH;
                visibility[nextStep.x][nextStep.y] = Visibility.VISIBLE;

                gamePanel.repaint(nextStep.x * CELL_SIZE, nextStep.y * CELL_SIZE,
                        CELL_SIZE, CELL_SIZE);

                score -= 3;
                updateScore();
            }
        }
    }

    // Uses A* for pathfinding
    protected void showAStarHint() {
        if (score < 3) {
            JOptionPane.showMessageDialog(this, "Not enough points!");
            return;
        }

        Point nearestTreasure = findNearestTreasure();
        if (nearestTreasure == null) {
            JOptionPane.showMessageDialog(this, "No treasures left!");
            return;
        }

        clearPathMarkers();
        repaint();

        List<Point> path = pathFinder.findAStarPath(playerPos, nearestTreasure);
        if (path != null && path.size() > 1) {
            Point nextStep = path.get(1);

            if (grid[nextStep.x][nextStep.y] != CellType.WALL) {
                grid[nextStep.x][nextStep.y] = CellType.PATH;
                visibility[nextStep.x][nextStep.y] = Visibility.VISIBLE;

                gamePanel.repaint(nextStep.x * CELL_SIZE, nextStep.y * CELL_SIZE,
                        CELL_SIZE, CELL_SIZE);

                score -= 3;
                updateScore();
            }
        }
    }

    // Finds the treasure nearest to the player's current position using Manhattan distance
    protected Point findNearestTreasure() {
        List<Point> treasures = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == CellType.TREASURE) {
                    treasures.add(new Point(i, j));
                }
            }
        }

        if (treasures.isEmpty()) return null;

        Point nearest = treasures.get(0);
        int minDistance = Math.abs(playerPos.x - nearest.x) + Math.abs(playerPos.y - nearest.y);

        for (Point treasure : treasures) {
            int distance = Math.abs(playerPos.x - treasure.x) + Math.abs(playerPos.y - treasure.y);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = treasure;
            }
        }

        return nearest;
    }

    // Refreshes the score and treasure count display
    protected void updateScore() {
        scoreLabel.setText("Score: " + score + " | Treasures: " + treasuresFound + "/" + TREASURES);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TreasureHunt());
    }
}