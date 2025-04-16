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
     * Resets the player's position and angle to the specified starting values.
     */
    public void resetPosition(double startX, double startY, double startAngle) {
        this.x = startX;
        this.y = startY;
        this.angle = startAngle;
    }
}