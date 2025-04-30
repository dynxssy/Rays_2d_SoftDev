// Player.java
public class Player {
    private double x, y, angle;
    private double moveSpeed = 0.02;        // base walk speed
    private boolean isSpeedBoosted = false;

    // Stamina fields
    private double stamina = 50.0;
    private static final double MAX_STAMINA = 50.0;
    private static final double DEPLETION_PER_SEC = 15.0;
    private static final double RECOVERY_PER_SEC  = 30.0;
    private static final double REGEN_COOLDOWN_TIME = 2.5;  // seconds
    private double regenCooldownTimer = 2.5;

    public Player(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    /** Called once per frame: handles stamina drain/recovery. */
    public void update(Map map) {
        double dt = 1.0 / 60.0; // or use a real delta if you pass one

        if (isSpeedBoosted) {
            stamina = Math.max(0, stamina - DEPLETION_PER_SEC * dt);
            regenCooldownTimer = REGEN_COOLDOWN_TIME;
            if (stamina == 0) setSpeedBoost(false);
        } else {
            if (regenCooldownTimer > 0) {
                regenCooldownTimer = Math.max(0, regenCooldownTimer - dt);
            } else {
                stamina = Math.min(MAX_STAMINA, stamina + RECOVERY_PER_SEC * dt);
            }
        }
    }

    /** Move forward in facing direction. */
    public void moveForward(Map map) {
        double nx = x + Math.cos(angle) * moveSpeed;
        double ny = y + Math.sin(angle) * moveSpeed;
        if (!map.isWall((int)nx, (int)y)) x = nx;
        if (!map.isWall((int)x, (int)ny)) y = ny;
    }

    /** Move backward (opposite facing). */
    public void moveBackward(Map map) {
        double nx = x - Math.cos(angle) * moveSpeed;
        double ny = y - Math.sin(angle) * moveSpeed;
        if (!map.isWall((int)nx, (int)y)) x = nx;
        if (!map.isWall((int)x, (int)ny)) y = ny;
    }

    /** Strafe directly left (angle – 90°). */
    public void strafeLeft(Map map) {
        double dx =  Math.sin(angle) * moveSpeed;
        double dy = -Math.cos(angle) * moveSpeed;
        tryMove(dx, dy, map);
    }

    /** Strafe directly right (angle + 90°). */
    public void strafeRight(Map map) {
        double dx = -Math.sin(angle) * moveSpeed;
        double dy =  Math.cos(angle) * moveSpeed;
        tryMove(dx, dy, map);
    }

    /** Collision-safe movement helper. */
    private void tryMove(double dx, double dy, Map map) {
        double nx = x + dx, ny = y + dy;
        if (!map.isWall((int)nx, (int)y)) x = nx;
        if (!map.isWall((int)x, (int)ny)) y = ny;
    }

    /** Rotate view by δ radians. */
    public void rotate(double delta) {
        angle += delta;
    }

    /** Toggle sprint; only works if you have stamina. */
    public void setSpeedBoost(boolean boost) {
        if (boost && !isSpeedBoosted && stamina > 0) {
            isSpeedBoosted = true;
            moveSpeed *= 4;
        } else if (!boost && isSpeedBoosted) {
            isSpeedBoosted = false;
            moveSpeed /= 4;
        }
    }

    /** For your HUD: returns 0.0–1.0 stamina ratio. */
    public double getStaminaRatio() {
        return stamina / MAX_STAMINA;
    }

    /** Accessors for Game/HUD. */
    public double getX()     { return x; }
    public double getY()     { return y; }
    public double getAngle() { return angle; }
}