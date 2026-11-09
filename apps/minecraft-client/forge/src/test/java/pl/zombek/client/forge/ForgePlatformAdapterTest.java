package pl.zombek.client.forge;

import org.junit.jupiter.api.Test;
import pl.zombek.client.forge.integration.ForgePlatformAdapter;

import static org.junit.jupiter.api.Assertions.*;

public class ForgePlatformAdapterTest {

    @Test
    void testPlatformAdapterProperties() {
        ForgePlatformAdapter adapter = new ForgePlatformAdapter();
        assertEquals("forge", adapter.getLoaderName());
        assertEquals("1.20.4", adapter.getMinecraftVersion());
        assertNotNull(adapter.getConfigDirectory());
        assertTrue(adapter.getConfigDirectory().toString().contains("zombekclient"));
    }
}
