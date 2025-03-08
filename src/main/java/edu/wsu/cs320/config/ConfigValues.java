package edu.wsu.cs320.config;

import java.io.File;

public final class ConfigValues {
    public static final String CONFIG_FILENAME = System.getProperty("user.home") + File.separator + ".gc2d";
    public static final String CONFIG_TEST_FILENAME = CONFIG_FILENAME + "test";

    public static final String GOOGLE_CLIENT_SECRET = "google_client_secret";
    public static final String GOOGLE_CLIENT_ID = "google_client_id";
    public static final String GOOGLE_REFRESH_TOKEN = "google_refresh_token";
    public static final String GOOGLE_CALENDAR_ID = "google_calendar_id";
    public static final String DISCORD_CLIENT_ID = "discord_client_id";
    public static final String DISCORD_BOT_TOKEN = "discord_bot_token";

    private ConfigValues() {}
}
