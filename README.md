
# 🎮 Treasure Hunt – Grid-Based Algorithmic Game

This project is a coursework submission for **COMP2069: Algorithms, Data Structures and Efficiency**, focused on the practical implementation of algorithms through a custom-designed game called **Treasure Hunt**.

## 📌 Overview
**Treasure Hunt** is a 2D grid-based game where the player navigates a procedurally generated map to find three hidden treasures. The game is built in **Java** and designed to highlight how data structures and algorithms can be used to solve problems in real-time applications like game development.

## 🧠 Key Features
- **Random Map Generation**: Each 20×20 grid is unique, with strategic placement of walls and treasures.
- **Player Navigation**: Move in four directions with real-time score tracking and collision detection.
- **Hint System**: Implemented using both **BFS** and **A\*** pathfinding algorithms.
- **Treasure Collection**: The goal is to find all 3 treasures with minimal cost.
- **Scoring Mechanics**:
  - Every move costs 1 point
  - Using a hint costs 3 points
  - Hitting a wall costs 10 points

## ⚙️ How to Run
1. Navigate to the `code/src/main/java/com/treasurehunt/` directory
2. Run the `TreasureHunt.java` file
3. Use arrow keys to play

## 📚 Algorithms & Data Structures Used
- **Breadth-First Search (BFS)** – for map validation and basic pathfinding
- **A\* Search** – for efficient hint generation with Manhattan distance heuristic
- **2D Arrays, HashMaps, PriorityQueues, LinkedLists** – for representing game entities and enabling fast computation

## 📈 Efficiency Insights
- Time complexity of BFS: `O(V + E)`
- Time complexity of A\*: `O(E + V log V)`
- All treasures are guaranteed to be reachable using a multi-stage validation process

## 🧩 Challenges & Trade-Offs
- Balanced optimality and performance using heuristic-guided A\*
- Prioritized readability and gameplay experience over ultra-complex optimizations
- Improved frame performance with partial redrawing techniques

## 📋 Lecturer Feedback

**Code:**
- Core game logic: **Good**
- Algorithm usage: **Good**
- Efficiency optimization: **Good**
- Extra features (e.g. enhanced pathfinding): **Good**

**Report:**
- Pathfinding explanation: **Good**
- Complexity discussion: **Good**
- Data structures analysis: **Good, but map structure not deeply discussed**
- Structure and presentation: **Good**
