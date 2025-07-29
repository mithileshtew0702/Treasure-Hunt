package com.treasurehunt;

import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;

/**
 * Handles player movement and collision detection for the Treasure Hunt game.
 * This class processes keyboard input and updates the player's position accordingly,
 * while checking for walls, boundaries, and treasure collection.
 */
public class PlayerMovement {
    private TreasureHunt game;

    public PlayerMovement(TreasureHunt game) {
        this.game = game;
    }

    // Calculate potential new position based on key press
    public void handleKeyPress(KeyEvent e) {
        int newX = game.playerPos.x;
        int newY = game.playerPos.y;

        // Determine direction from key input
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: newY--; break;
            case KeyEvent.VK_DOWN: newY++; break;
            case KeyEvent.VK_LEFT: newX--; break;
            case KeyEvent.VK_RIGHT: newX++; break;
            default: return;
        }

        // Check for map boundary collision
        if (newX < 0 || newX >= TreasureHunt.SIZE || newY < 0 || newY >= TreasureHunt.SIZE) {
            JOptionPane.showMessageDialog(game, "Map boundary!");
            return;
        }

        // Handle wall collision
        if (game.grid[newX][newY] == TreasureHunt.CellType.WALL) {
            game.visibility[newX][newY] = TreasureHunt.Visibility.VISIBLE;
            game.score -= 10;
            game.updateScore();
            JOptionPane.showMessageDialog(game, "Wall hit! -10 points");
            game.repaint();
            return;
        }

        // Clear current player position
        game.grid[game.playerPos.x][game.playerPos.y] = TreasureHunt.CellType.EMPTY;

        // Check for treasure collection
        if (game.grid[newX][newY] == TreasureHunt.CellType.TREASURE) {
            game.visibility[newX][newY] = TreasureHunt.Visibility.VISIBLE;
            game.treasuresFound++;

            // Check for game completion
            JOptionPane.showMessageDialog(game, "Treasure found! " + game.treasuresFound + "/" + TreasureHunt.TREASURES);
            if (game.treasuresFound == TreasureHunt.TREASURES) {
                JOptionPane.showMessageDialog(game, "You won! Final score: " + game.score);
                System.exit(0);
            }
        }

        // Update player position and visibility
        game.playerPos.setLocation(newX, newY);
        game.grid[newX][newY] = TreasureHunt.CellType.PLAYER;
        game.visibility[newX][newY] = TreasureHunt.Visibility.VISIBLE;

        // Clear any path hints and update score for the move
        game.clearPathMarkers();
        game.score -= 1;
        game.updateScore();
        game.repaint();
    }
}