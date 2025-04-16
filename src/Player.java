public class Player {
    private double x, y, angle;
    private double moveSpeed = 0.01; // Reduced from 0.05
    private final double rotateSpeed = 0.001; // Reduced from 0.02
    private boolean isSpeedBoosted = false;

    public Player(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public void update(Map map) {
        // Placeholder for additional updates if needed
    }

    public void moveForward(Map map) {
        double newX = x + Math.cos(angle) * moveSpeed;
        double newY = y + Math.sin(angle) * moveSpeed;
        if (!map.isWall((int) newX, (int) y)) x = newX;
        if (!map.isWall((int) x, (int) newY)) y = newY;
    }

    public void moveBackward(Map map) {
        double newX = x - Math.cos(angle) * moveSpeed;
        double newY = y - Math.sin(angle) * moveSpeed;
        if (!map.isWall((int) newX, (int) y)) x = newX;
        if (!map.isWall((int) x, (int) newY)) y = newY;
    }

    public void strafeLeft(Map map) {
        double newX = x + Math.sin(angle) * moveSpeed;
        double newY = y - Math.cos(angle) * moveSpeed;
        if (!map.isWall((int) newX, (int) y)) x = newX;
        if (!map.isWall((int) x, (int) newY)) y = newY;
    }

    public void strafeRight(Map map) {
        double newX = x - Math.sin(angle) * moveSpeed;
        double newY = y + Math.cos(angle) * moveSpeed;
        if (!map.isWall((int) newX, (int) y)) x = newX;
        if (!map.isWall((int) x, (int) newY)) y = newY;
    }

    public void rotateLeft() {
        angle -= rotateSpeed;
    }

    public void rotateRight() {
        angle += rotateSpeed;
    }

    public void rotate(double deltaAngle) {
        angle += deltaAngle;
    }

    public void setSpeedBoost(boolean boost) {
        if (boost && !isSpeedBoosted) {
            isSpeedBoosted = true;
            moveSpeed *= 4;
        } else if (!boost && isSpeedBoosted) {
            isSpeedBoosted = false;
            moveSpeed /= 4;
        }
    }

    public void adjustMoveSpeed(double delta) {
        moveSpeed = Math.max(0.001, moveSpeed + delta); // Ensure moveSpeed does not go below a minimum value
    }

    public double getX() {
        return Math.round(x * 100.0) / 100.0;
    }

    public double getY() {
        return Math.round(y * 100.0) / 100.0;
    }

    public double getAngle() {
        return angle;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    /**
     * Returns the player's current position as a formatted string (x, y).
     */
    public String getPosition() {
        return String.format("(%.2f, %.2f)", x, y);
    }

    /**
     * Calculates the distance from the player to a given point (targetX, targetY).
     */
    public double calculateDistance(double targetX, double targetY) {
        return Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
    }

    /**
     * Checks if the player is within a certain distance of a target point.
     * 
     * @param targetX The x-coordinate of the target point.
     * @param targetY The y-coordinate of the target point.
     * @param distance The maximum distance to check.
     * @return True if the player is within the specified distance, false otherwise.
     */
    public boolean isWithinDistance(double targetX, double targetY, double distance) {
        return calculateDistance(targetX, targetY) <= distance;
    }

    /**
     * Resets the player's position and angle to the specified starting values.
     * This is useful for respawning or resetting the game state.
     */
    public void resetPosition(double startX, double startY, double startAngle) {
        this.x = startX;
        this.y = startY;
        this.angle = startAngle;
    }
}