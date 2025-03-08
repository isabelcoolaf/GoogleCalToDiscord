package edu.wsu.cs320.config;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {

    private final Properties properties;
    private final String filePath;

    public ConfigManager(String filePath) {
        this.properties = new Properties();
        this.filePath = filePath;

        try {
            FileReader reader = new FileReader(filePath);
            this.properties.load(reader);
            reader.close();
        } catch (IOException ignored) {}
    }

    public String get(String key) {
        return this.properties.getProperty(key);
    }

    public void put(String key, String value) throws IOException {
        this.properties.setProperty(key, value);
        properties.store(new FileWriter(this.filePath), "GoogleCalToDiscord Configuration File");
    }

}
