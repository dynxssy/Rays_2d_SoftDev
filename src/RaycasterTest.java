import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

public class RaycasterTest {

    @Test
    void testUpdateSettingsCreatesCorrectOffsets() throws Exception {
        char[][] map = new char[1][1];
        int width = 200, height = 100;
        Raycaster rc = new Raycaster(map, 1, 1, 0.5, 0.5, 0, width, height, 90, 5);

        Field offsetCosField = Raycaster.class.getDeclaredField("offsetCos");
        offsetCosField.setAccessible(true);
        assertNull(offsetCosField.get(rc));

        rc.updateSettings(90, 5);
        double[] offsetCos = (double[]) offsetCosField.get(rc);
        assertNotNull(offsetCos);

        int expectedCount = (width + 5 - 1) / 5;
        assertEquals(expectedCount, offsetCos.length);

        double radFov = Math.toRadians(90);
        double half = radFov / 2;
        // First offset
        assertEquals(Math.cos(-half), offsetCos[0], 1e-6);
        // Last offset
        int lastIndex = offsetCos.length - 1;
        double expectedLast = -half + radFov * (lastIndex * 5.0) / width;
        assertEquals(Math.cos(expectedLast), offsetCos[lastIndex], 1e-6);
    }

    @Test
    void testUpdatePlayerUpdatesFields() throws Exception {
        char[][] map = new char[1][1];
        Raycaster rc = new Raycaster(map, 1, 1, 0, 0, 0, 100, 100, 60, 1);

        Field xField = Raycaster.class.getDeclaredField("playerX");
        xField.setAccessible(true);
        Field yField = Raycaster.class.getDeclaredField("playerY");
        yField.setAccessible(true);
        Field angleField = Raycaster.class.getDeclaredField("playerAngle");
        angleField.setAccessible(true);

        rc.updatePlayer(2.5, 3.5, Math.PI / 4);
        assertEquals(2.5, xField.getDouble(rc), 1e-9);
        assertEquals(3.5, yField.getDouble(rc), 1e-9);
        assertEquals(Math.PI / 4, angleField.getDouble(rc), 1e-9);
    }
}