# My Raycaster Game

## Overview
My Raycaster Game is a simple 3D first-person shooter inspired by classic games like Wolfenstein 3D. It features a raycasting engine that allows players to navigate through a tile-based map, avoiding walls and reaching an exit point.

## Features
- Functional 3D first-person view using raycasting.
- Smooth movement and rotation controlled by the keyboard (WASD for movement, arrow keys for rotation).
- A simple, tile-based map with walls rendered using raycasting.
- Basic textures and shading to differentiate walls and provide depth perception.
- A heads-up display (HUD) showing player position and direction.
- Collision detection to prevent walking through walls.
- Frame rate optimization for smooth performance.
- Simple game objective: reach the exit point.

## Project Structure
```
my-raycaster-game
├── src
│   ├── Main.java        # Entry point of the game
│   ├── Game.java        # Main game class managing game state
│   ├── Map.java         # Tile-based map structure
│   ├── Player.java      # Player character representation
│   ├── Raycaster.java    # Raycasting algorithm implementation
│   ├── Renderer.java     # Game scene rendering
│   └── HUD.java         # Heads-up display management
├── README.md            # Project documentation
└── build-and-run.sh     # Shell script to build and run the game
```

## How to Build and Run
1. Ensure you have the Java Development Kit (JDK) installed on your machine.
2. Open a terminal and navigate to the project directory.
3. Run the following command to build and execute the game:
   ```
   ./build-and-run.sh
   ```

## Controls
- **W**: Move forward
- **S**: Move backward
- **A**: Move left
- **D**: Move right
- **Arrow Keys**: Rotate the view
- **R**: Reset player position

## License
This project is open-source and available for modification and distribution. Enjoy exploring the world of raycasting!