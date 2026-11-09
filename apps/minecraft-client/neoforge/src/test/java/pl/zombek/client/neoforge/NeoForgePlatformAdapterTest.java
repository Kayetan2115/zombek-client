package pl.zombek.client.neoforge;

import org.junit.jupiter.api.Test;
import pl.zombek.client.neoforge.integration.NeoForgePlatformAdapter;

import static org.junit.jupiter.api.Assertions.*;

public class NeoForgePlatformAdapterTest {

    @Test
    void testPlatformAdapterProperties() {
        NeoForgePlatformAdapter adapter = new NeoForgePlatformAdapter();
        assertEquals("neoforge", adapter.getLoaderName());
        assertEquals("1.20.4", adapter.getMinecraftVersion());
        assertNotNull(adapter.getConfigDirectory());
        assertTrue(adapter.getConfigDirectory().toString().contains("zombekclient"));
    }
}
