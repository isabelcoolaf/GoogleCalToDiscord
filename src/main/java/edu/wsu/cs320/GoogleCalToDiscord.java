package edu.wsu.cs320;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.config.ConfigValues;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import edu.wsu.cs320.googleapi.GoogleOAuthManager;
import edu.wsu.cs320.gui.Customizer.Customizer;
import edu.wsu.cs320.gui.control.GuiController;
import edu.wsu.cs320.gui.control.GuiResponse;
import edu.wsu.cs320.RP.DiscordInterface;

import java.io.IOException;
import java.net.URL;

public class GoogleCalToDiscord {

    public static GoogleOAuthManager googleOAuthManager;
    public static DiscordInterface discordInterface;
    public static ConfigManager config;

    private static void makeInterfaceInstance(String discordClientID, String discordBotToken) {
        if (discordClientID != null && discordBotToken != null && discordInterface == null) {
            discordInterface = new DiscordInterface(discordClientID, discordBotToken);
            discordInterface.start();
        }
    }

    public static void main(String[] args) throws IOException {
        config = new ConfigManager(ConfigValues.CONFIG_FILENAME);
        GuiController controller = new GuiController();
        String googleClientID = config.get(ConfigValues.GOOGLE_CLIENT_ID);
        String googleClientSecret = config.get(ConfigValues.GOOGLE_CLIENT_SECRET);
        String googleRefreshToken = config.get(ConfigValues.GOOGLE_REFRESH_TOKEN);
        String discordClientID = config.get(ConfigValues.DISCORD_CLIENT_ID);
        String discordBotToken = config.get(ConfigValues.DISCORD_BOT_TOKEN);

        googleOAuthManager = new GoogleOAuthManager(googleClientID, googleClientSecret, googleRefreshToken, ConfigValues.CONFIG_FILENAME);

        if (config.get(ConfigValues.GOOGLE_CALENDAR_ID) != null) {
            makeInterfaceInstance(discordClientID, discordBotToken);
        }

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
                System.out.println("Created OAuth Manager!");
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
        calendarList.setItems(calendarServiceHandler.getCalendarList());
        GuiResponse<CalendarListEntry> selectorResponse;
        GuiResponse<Customizer.CustomizerCode> customizerResponse;
        while (true) {
            boolean shouldExitUILoop = false;
            while (true) {
                selectorResponse = controller.getCalendarFromList(calendarList);
                switch (selectorResponse.status) {
                    case INCOMPLETE_DATA:
                        System.out.println("User did not select a calendar. Retrying...");
                        continue;
                    case OK:
                        config.put(ConfigValues.GOOGLE_CALENDAR_ID, selectorResponse.data.getId());
                        makeInterfaceInstance(discordClientID, discordBotToken);
                        discordInterface.getRichPresence().setGoogleCalendar();
                        break;
                    case WINDOW_CLOSED:
                        shouldExitUILoop = true;
                        controller.destroy();
                        break;
                    case CANCELLED:
                        controller.destroy();
                        shouldExitUILoop = true;
                        break;
                }
                break;
            }
            if (shouldExitUILoop) break;

            while (true) {
                customizerResponse = controller.accessCustomizer(discordInterface);
                switch (customizerResponse.status) {
                    case OK:
                        if (customizerResponse.data == Customizer.CustomizerCode.CHANGE_IMAGE) {
                            System.out.println("Image changed");
                            URL imageURL = controller.getCustomizerImage();
                            if (imageURL != null) {
                                discordInterface.getRichPresence().updateActivityWithImages(imageURL.toString(), null);
                                controller.setCustomizerImage(imageURL);
                            }
                            continue;
                        }
                        break;
                    case INCOMPLETE_DATA:
                    case CANCELLED:
                    case WINDOW_CLOSED:
                        shouldExitUILoop = true;
                        break;
                }
                break;
            }
            if (shouldExitUILoop) break;
        }
        controller.destroy();
        discordInterface.getRichPresence().stopDiscordActivity();
        discordInterface.killBot();
    }

}
