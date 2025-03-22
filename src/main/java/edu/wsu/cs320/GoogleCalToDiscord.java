package edu.wsu.cs320;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.config.ConfigValues;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import edu.wsu.cs320.googleapi.GoogleOAuthManager;
import edu.wsu.cs320.gui.control.GuiController;
import edu.wsu.cs320.gui.control.GuiResponse;
import java.io.IOException;

public class GoogleCalToDiscord {

    public static GoogleOAuthManager googleOAuthManager;
    public static ConfigManager config;

    public static void main(String[] args) throws IOException {
        config = new ConfigManager(ConfigValues.CONFIG_FILENAME);
        GuiController controller = new GuiController();
        String googleClientID = config.get(ConfigValues.GOOGLE_CLIENT_ID);
        String googleClientSecret = config.get(ConfigValues.GOOGLE_CLIENT_SECRET);
        String googleRefreshToken = config.get(ConfigValues.GOOGLE_REFRESH_TOKEN);
        String discordClientID = config.get(ConfigValues.DISCORD_CLIENT_ID);
        String discordBotToken = config.get(ConfigValues.DISCORD_BOT_TOKEN);

        googleOAuthManager = new GoogleOAuthManager(googleClientID, googleClientSecret, googleRefreshToken, ConfigValues.CONFIG_FILENAME);

//        if (discordClientID != null && discordBotToken != null) {
//            DiscordInterface discordInterface = new DiscordInterface(discordClientID, discordBotToken);
//            discordInterface.start();
//        }

        if (googleOAuthManager.isAuthenticated()) {
            System.out.println("GOOGLE AUTHENTICATION SUCCESSFUL");
        }
        if (!googleOAuthManager.isAuthenticated() || discordClientID == null || discordBotToken == null) {
            GuiResponse<String[]> resp;
            while (true) {
                resp = controller.getAuthData();
                if (resp.status == GuiResponse.ResponseCode.INCOMPLETE_DATA) {
                    System.out.println("Data incomplete");
                    continue;
                }
                if (resp.status == GuiResponse.ResponseCode.WINDOW_CLOSED) {
                    controller.destroy();
                    return;
                }
                if (resp.status == GuiResponse.ResponseCode.CANCELLED) {
                    return;
                } else { // Response OK
                    break;
                }
            }
            config.put(ConfigValues.GOOGLE_CLIENT_ID, resp.data[0]);
            config.put(ConfigValues.GOOGLE_CLIENT_SECRET, resp.data[1]);
            config.put(ConfigValues.DISCORD_CLIENT_ID, resp.data[2]);
            config.put(ConfigValues.DISCORD_BOT_TOKEN, resp.data[3]);
            try {
                googleOAuthManager = new GoogleOAuthManager(resp.data[0], resp.data[1], "", ConfigValues.CONFIG_FILENAME);
                googleOAuthManager.invokeFlow();
                if (googleOAuthManager.isAuthenticated()) {
                    config.put(ConfigValues.GOOGLE_REFRESH_TOKEN, googleOAuthManager.getCredentials().getRefreshToken());
                }
            } catch (Exception e) {
                // TODO: Handle errors with auth flow
                // invokeFlow() will return an error with good text in it to display.
                // the function also blocks until complete, so keep that in mind
            }
        }
        // Get calendar
        System.out.println("Getting calendar...");
        GoogleCalendarServiceHandler calendarServiceHandler = new GoogleCalendarServiceHandler(googleOAuthManager.getCredentials());
        CalendarList calendarList = new CalendarList();
        CalendarListEntry selectedCalendar;
        calendarList.setItems(calendarServiceHandler.getCalendarList());
        GuiResponse<CalendarListEntry> resp;
        while (true) {
            resp = controller.getCalendarFromList(calendarList);
            if (resp.status == GuiResponse.ResponseCode.INCOMPLETE_DATA) {
                System.out.println("User did not select a calendar. Retrying...");
                continue;
            }
            if (resp.status == GuiResponse.ResponseCode.WINDOW_CLOSED) {
                controller.destroy();
                return;
            }
            if (resp.status == GuiResponse.ResponseCode.CANCELLED) {
                return;
            } else { // Response OK
                selectedCalendar = resp.data;
                break;
            }
        }
        System.out.println("Selected calendar. (" + selectedCalendar.getSummary() + ": " + selectedCalendar.getDescription() + ")");
    }
}
