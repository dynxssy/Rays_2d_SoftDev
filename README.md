# Rays 2D SoftDev - Java Raycasting Game Engine

A **3D-style first-person game** built entirely in Java using advanced **raycasting algorithms** to create immersive pseudo-3D environments. This competitive multiplayer experience features a built-in **level editor**, **texture rendering**, and **real-time performance optimization**.

---

## Project Overview

This project demonstrates a complete **raycasting engine** implementation from scratch, showcasing advanced computer graphics techniques typically found in classic games like Wolfenstein 3D. The game features:

- **Advanced Raycasting Algorithm**: Custom implementation using DDA (Digital Differential Analyzer) for efficient wall detection
- **Real-time 3D Rendering**: Pseudo-3D perspective rendering with textured walls, floors, and animated sky
- **Built-in Level Editor**: Interactive map creation tool allowing players to design custom levels
- **Competitive Multiplayer**: Turn-based gameplay where players create and compete on each other's levels
- **Performance Optimization**: Dynamic FOV adjustment, configurable ray resolution, and efficient texture scaling
- **Interactive Features**: Sprint mechanics with stamina system, screenshake effects, and responsive controls

### Key Features
- **Textured Environment Rendering** with floor, wall, and sky textures
- **Dynamic Lighting Effects** with distance-based darkness simulation
- **Mini-map System** with real-time player tracking and ray visualization
- **Custom Level Creation** with spawn points, special tiles, and objectives
- **Audio Integration** with background music and sound effects
- **Timer-based Competition** system for speedrun-style gameplay

---

## Collaboration & Development

This project was developed using **modern software engineering practices**:

- **Version Control**: Git-based development with feature branching and collaborative workflows
- **Modular Architecture**: Clean separation of concerns with dedicated classes for rendering, game logic, and user interface
- **Object-Oriented Design**: Proper encapsulation and inheritance patterns throughout the codebase
- **Cross-platform Compatibility**: Pure Java implementation ensuring portability across Windows, macOS, and Linux

### Development Methodology
- **Test-Driven Development**: JUnit integration for game logic validation
- **Agile Practices**: Iterative development with clear TODO tracking and feature prioritization
- **Code Quality**: Consistent naming conventions, comprehensive commenting, and maintainable structure

---

## Skills & Technologies Highlighted

### **Programming Languages & Frameworks**
- **Java** - Core application development with Swing GUI framework
- **Shell Scripting** - Build automation and deployment scripts

### **Computer Graphics & Game Development**
- **Raycasting Algorithms** - DDA implementation for 3D rendering simulation
- **Texture Mapping** - Bilinear filtering and texture scaling techniques
- **Real-time Rendering** - BufferStrategy implementation for smooth graphics
- **Game Physics** - Collision detection and movement mechanics

### **Software Engineering Practices**
- **Object-Oriented Programming** - Clean class design and inheritance
- **Design Patterns** - Observer pattern for event handling, Factory pattern for object creation
- **Performance Optimization** - Memory management and rendering efficiency
- **File I/O Operations** - Level serialization and texture loading

### **Development Tools & Technologies**
- **Java Swing** - GUI development and event handling
- **BufferedImage & Graphics2D** - Advanced image processing and rendering
- **Robot Class** - Mouse control for FPS-style camera movement
- **File System Operations** - Dynamic level loading and asset management

---

## Installation & Usage

### Prerequisites
- **Java Development Kit (JDK) 8 or higher**
- **Windows, macOS, or Linux** operating system
- **Minimum 512MB RAM** for optimal performance
- **Graphics support** for BufferedImage rendering

### Setup & Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd Rays_2d_SoftDev
   ```

2. **Compile the project**:
   ```bash
   cd src
   javac *.java
   ```

3. **Run the game**:
   ```bash
   java Main
   ```

   **Alternative**: Use the provided build script:
   ```bash
   chmod +x build-and-run.sh
   ./build-and-run.sh
   ```

### Game Controls
- **WASD** - Movement (W: Forward, S: Backward, A: Strafe Left, D: Strafe Right)
- **Mouse Movement** - Camera rotation and looking around
- **SHIFT** - Sprint (consumes stamina)
- **ESC** - Game settings and options

### How to Play

1. **Create Your Level**: Use the built-in level editor to design a custom map
2. **Set Objectives**: Place spawn points, end goals, and special tiles
3. **Compete**: Players take turns creating levels and competing for best completion times
4. **Win Condition**: Reach the red end tile ('E') in the shortest time possible

### Level Editor Features
- **Wall Placement**: Click to place/remove walls
- **Special Tiles**: 
  - `P` - Player spawn point
  - `E` - End/goal tile (red)
  - `T` - Speed boost tile (blue)
  - `V` - Void/restart tile (black)
- **Save/Load**: Custom level persistence system

---

## Project Structure

```
Rays_2d_SoftDev/
├── src/                          # Source code
│   ├── Main.java                 # Application entry point
│   ├── Game.java                 # Core game loop and logic
│   ├── Raycaster.java           # 3D rendering engine
│   ├── Renderer.java            # Graphics rendering manager
│   ├── Player.java              # Player movement and state
│   ├── Map.java                 # Level data structure
│   ├── MapEditor.java           # Level creation tool
│   ├── HUD.java                 # User interface elements
│   ├── SoundManager.java        # Audio system
│   └── TextureLoader.java       # Asset loading utilities
├── textures/                    # Game assets
│   ├── brick3.jpg              # Wall textures
│   ├── floor.jpg               # Floor textures
│   └── sky1.jpg                # Sky textures
├── sounds/                      # Audio files
├── levels/                      # Custom level storage
├── bin/                         # Compiled classes
└── build-and-run.sh            # Build automation script
```

---

## Technical Implementation Highlights

### **Advanced Raycasting Engine**
- **DDA Algorithm**: Efficient grid traversal for wall detection
- **Texture Mapping**: Real-time texture application with bilinear filtering
- **Distance-based Lighting**: Dynamic shading effects for depth perception

### **Performance Optimizations**
- **Configurable Ray Resolution**: Adjustable rendering quality for performance tuning
- **Buffer Strategy**: Triple buffering for smooth animation
- **Efficient Pixel Manipulation**: Direct pixel buffer access for maximum speed

### **Interactive Systems**
- **Real-time Mini-map**: Live visualization of player position and viewing rays
- **Dynamic FOV**: Smooth field-of-view transitions for special effects
- **Stamina System**: Resource management mechanics for sprint functionality

---

*This project demonstrates proficiency in **computer graphics programming**, **game engine development**, and **advanced Java programming techniques**, showcasing the ability to implement complex algorithms and create engaging interactive experiences.*
