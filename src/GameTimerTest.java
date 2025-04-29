import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.awt.event.KeyEvent;

class GameTimerTest {
    private Game game;
    private Field timerStartedField;
    private Field timerStartField;
    private Field keysField;

    @BeforeEach
    void setUp() throws Exception {
        game = new Game();
        // access private fields
        timerStartedField = Game.class.getDeclaredField("timerStarted");
        timerStartedField.setAccessible(true);
        timerStartField = Game.class.getDeclaredField("timerStart");
        timerStartField.setAccessible(true);
        keysField = Game.class.getDeclaredField("keys");
        keysField.setAccessible(true);
        // set a simple map so processInput can run without null pointer
        Field mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        char[][] layout = new char[3][3];
        for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++) layout[y][x] = '0';
        mapField.set(game, new Map(layout));
    }

    @Test
    void testTimerNotStartedInitially() throws Exception {
        boolean started = timerStartedField.getBoolean(game);
        assertFalse(started, "Timer should not start before any movement");
    }

    @Test
    void testTimerStartsOnFirstMovement() throws Exception {
        // simulate pressing W
        boolean[] keys = (boolean[]) keysField.get(game);
        keys[KeyEvent.VK_W] = true;

        // invoke processInput()
        Method processInput = Game.class.getDeclaredMethod("processInput");
        processInput.setAccessible(true);
        processInput.invoke(game);

        assertTrue(timerStartedField.getBoolean(game), "Timer should start on first movement");
        long startTime = timerStartField.getLong(game);
        assertTrue(startTime > 0, "timerStart should be set to a positive value");

        // subsequent movement should not reset timerStart
        Thread.sleep(10); // wait briefly
        processInput.invoke(game);
        long newStart = timerStartField.getLong(game);
        assertEquals(startTime, newStart, "Timer start time should not change on subsequent movements");
    }
}