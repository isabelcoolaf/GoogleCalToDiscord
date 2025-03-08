package edu.wsu.cs320;

import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.config.ConfigValues;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigManagerTests {

    // Black-box. Making sure empty key returns null.
    @Test
    @Order(1)
    public void getEmptyKey() {
        ConfigManager manager = new ConfigManager(ConfigValues.CONFIG_TEST_FILENAME);
        assertNull(manager.get("empty_key"));
    }

    // Black-box. Making sure putting then retrieving the same key works.
    @Test
    @Order(2)
    public void putKey() throws IOException {
        ConfigManager manager = new ConfigManager(ConfigValues.CONFIG_TEST_FILENAME);
        manager.put("guaranteed_key", "value");
        assertEquals("value", manager.get("guaranteed_key"));
    }

    // Black-box. Making sure the previously put key persists to a new ConfigManager instance
    @Test
    @Order(3)
    public void getExistingKeyFromNewManager() {
        ConfigManager manager = new ConfigManager(ConfigValues.CONFIG_TEST_FILENAME);
        assertEquals("value", manager.get("guaranteed_key"));
    }

    // Black-box. Making sure overwriting keys works.
    @Test
    @Order(4)
    public void updateExistingKey() throws IOException {
        ConfigManager manager = new ConfigManager(ConfigValues.CONFIG_TEST_FILENAME);
        manager.put("guaranteed_key", "value2");
        assertEquals("value2", manager.get("guaranteed_key"));
    }

    // Black-box. Making sure an overwritten key persists to a new ConfigManager instance
    @Test
    @Order(5)
    public void getExistingExistingKeyFromNewManager() {
        ConfigManager manager = new ConfigManager(ConfigValues.CONFIG_TEST_FILENAME);
        assertEquals("value2", manager.get("guaranteed_key"));
    }

}
