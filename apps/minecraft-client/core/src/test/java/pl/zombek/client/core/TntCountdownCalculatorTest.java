package pl.zombek.client.core;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.pvp.tnt.TntCountdownCalculator;
import pl.zombek.client.core.modules.pvp.tnt.TntDisplayFormat;

import static org.junit.jupiter.api.Assertions.*;

public class TntCountdownCalculatorTest {

    @Test
    public void testCalculateSeconds() {
        assertEquals(4.0f, TntCountdownCalculator.calculateSeconds(80, 0), 0.001f);
        assertEquals(3.8f, TntCountdownCalculator.calculateSeconds(76, 0), 0.001f);
        assertEquals(0.0f, TntCountdownCalculator.calculateSeconds(0, 0), 0.001f);
        assertEquals(1.0f, TntCountdownCalculator.calculateSeconds(10, 10), 0.001f);
    }

    @Test
    public void testFormatLabelWithPrefix() {
        String label = TntCountdownCalculator.formatLabel(76, 0, true, true, 1, TntDisplayFormat.PREFIX_AND_TIME);
        assertEquals("TNT 3.8s", label);

        String label2 = TntCountdownCalculator.formatLabel(76, 0, false, true, 1, TntDisplayFormat.PREFIX_AND_TIME);
        assertEquals("3.8s", label2);

        String timeOnly = TntCountdownCalculator.formatLabel(76, 0, true, true, 1, TntDisplayFormat.TIME_ONLY);
        assertEquals("3.8s", timeOnly);
    }

    @Test
    public void testResolveColorDynamic() {
        // Safe (> 50 ticks)
        assertEquals(TntCountdownCalculator.COLOR_GREEN, TntCountdownCalculator.resolveColor(70, true, 0xFFFFFFFF));

        // Warning (20 - 50 ticks)
        assertEquals(TntCountdownCalculator.COLOR_YELLOW, TntCountdownCalculator.resolveColor(35, true, 0xFFFFFFFF));

        // Critical (< 20 ticks)
        assertEquals(TntCountdownCalculator.COLOR_RED, TntCountdownCalculator.resolveColor(10, true, 0xFFFFFFFF));

        // Static mode
        assertEquals(0xFF00FF00, TntCountdownCalculator.resolveColor(10, false, 0xFF00FF00));
    }
}
