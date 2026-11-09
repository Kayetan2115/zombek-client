package pl.zombek.client;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.ClientConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClientArchitectureTest {

    @Test
    void testClientConstants() {
        assertEquals("zombekclient", ClientConstants.MOD_ID);
        assertEquals("ZombekClient", ClientConstants.CLIENT_NAME);
        assertEquals("fabric", ClientConstants.LOADER_TYPE);
        assertNotNull(ClientConstants.CLIENT_VERSION);
    }
}
