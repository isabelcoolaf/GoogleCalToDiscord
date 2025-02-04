package edu.wsu.cs320.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {

    private final Properties properties;

    public ConfigManager() {
        this.properties = new Properties();

        try {
            FileReader reader = new FileReader(System.getProperty("user.home") + File.separator + ".gc2d");
            this.properties.load(reader);
            reader.close();
        } catch (IOException ignored) {}
    }

    public String get(String key) {
        return this.properties.getProperty(key);
    }

    public void put(String key, String value) throws IOException {
        this.properties.setProperty(key, value);
        properties.store(new FileWriter(System.getProperty("user.home") + File.separator + ".gc2d"), "GoogleCalToDiscord Configuration File");
    }

}
