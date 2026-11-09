package pl.zombek.client.quilt;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.quilt.platform.QuiltPlatformAdapter;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class QuiltPlatformAdapterTest {

    @Test
    public void testPlatformMetadata() {
        IPlatformAdapter adapter = new QuiltPlatformAdapter();

        assertEquals("quilt", adapter.getLoaderName(), "Loader name must be 'quilt'");
        assertNotNull(adapter.getLoaderVersion(), "Loader version should not be null");
        assertEquals("1.20.4", adapter.getMinecraftVersion(), "Minecraft version must be 1.20.4");
        assertTrue(adapter.isClient(), "Quilt client adapter must report isClient() == true");

        Path configDir = adapter.getConfigDirectory();
        assertNotNull(configDir);
        assertTrue(configDir.toString().contains("zombekclient"), "Config dir must contain 'zombekclient'");
    }

    @Test
    public void testExecuteOnRenderThread() {
        QuiltPlatformAdapter adapter = new QuiltPlatformAdapter();
        AtomicBoolean executed = new AtomicBoolean(false);

        adapter.executeOnRenderThread(() -> executed.set(true));

        assertTrue(executed.get(), "Action should be executed when Minecraft client is headless/null");
    }
}
