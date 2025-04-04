public class Player {
    private double x, y, angle;
    private final double moveSpeed = 0.0025; // Reduced from 0.05
    private final double rotateSpeed = 0.001; // Reduced from 0.02

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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAngle() {
        return angle;
    }
}