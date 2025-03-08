package edu.wsu.cs320;

import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.config.ConfigValues;
import edu.wsu.cs320.googleapi.GoogleOAuthManager;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class GoogleOAuthManagerTests {

    // Integration test between ConfigManager and GoogleOAuthManager
    @Test
    public void testStoringCredentials() throws IOException {
        GoogleOAuthManager oauth = new GoogleOAuthManager("a", "b", "c", ConfigValues.CONFIG_TEST_FILENAME);
        oauth.storeCredentials();
        ConfigManager config = new ConfigManager(ConfigValues.CONFIG_TEST_FILENAME);
        assertEquals("a", config.get(ConfigValues.GOOGLE_CLIENT_ID));
        assertEquals("b", config.get(ConfigValues.GOOGLE_CLIENT_SECRET));
        assertEquals("c", config.get(ConfigValues.GOOGLE_REFRESH_TOKEN));
    }

    private CompletableFuture<Void> invokeFlowTest(GoogleOAuthManager oauth) {
        return CompletableFuture.runAsync(() -> {
            try {
                oauth.invokeFlow(8001);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // White-box with branch coverage test of invokeFlow().
    // (OPENS 10 BROWSER TABS)
    @Test
    public void testOAuthResponses() {
        GoogleOAuthManager oauth = new GoogleOAuthManager("a", "b", "c", ConfigValues.CONFIG_TEST_FILENAME);
        assertFalse(oauth.isAuthenticated());

        // Testing receiving oauth error
        String errorText = "error";
        CompletableFuture<Void> future = invokeFlowTest(oauth);
        assertDoesNotThrow(() -> Desktop.getDesktop().browse(new URI("http://localhost:8001/?error=" + errorText)));
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals(errorText, exception.getCause().getMessage());

        // Testing missing scope
        future = invokeFlowTest(oauth);
        assertDoesNotThrow(() -> Desktop.getDesktop().browse(new URI("http://localhost:8001/?scope=openid")));
        exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause().getMessage().contains("Missing scope"));

        // Testing incorrect scope
        future = invokeFlowTest(oauth);
        assertDoesNotThrow(() -> Desktop.getDesktop().browse(new URI("http://localhost:8001/?scope=openid")));
        exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause().getMessage().contains("Missing scope"));

        // Testing missing code
        future = invokeFlowTest(oauth);
        assertDoesNotThrow(() -> Desktop.getDesktop().browse(new URI("http://localhost:8001/?scope=https://www.googleapis.com/auth/calendar")));
        exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause().getMessage().contains("Didn't receive code"));

        // Testing well-formed request (should still throw because code is invalid)
        future = invokeFlowTest(oauth);
        assertDoesNotThrow(() -> Desktop.getDesktop().browse(new URI("http://localhost:8001/?scope=https://www.googleapis.com/auth/calendar&code=a")));
        // Below line is because IntelliJ was whining without it
        exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause().getMessage().contains("401 Unauthorized"));
    }

}
