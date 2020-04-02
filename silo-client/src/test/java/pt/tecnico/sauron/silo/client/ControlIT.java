package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;

public class ControlIT extends BaseIT {
    @Test
    public void testPing() {
        Assertions.assertDoesNotThrow(() -> {
            client.ping();
        });
    }
}