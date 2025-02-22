package edu.wsu.cs320;

import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.googleapi.GoogleOAuthManager;
import edu.wsu.cs320.gui.control.GuiController;
import edu.wsu.cs320.gui.control.GuiResponse;
import edu.wsu.cs320.RP.DiscordInterface;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GoogleCalToDiscord {

    public static void main(String[] args) throws IOException {
        ConfigManager config = new ConfigManager();
        String googleClientID = config.get("google_client_id");
        String googleClientSecret = config.get("google_client_secret");
        String googleRefreshToken = config.get("google_refresh_token");
        String discordClientID = config.get("discord_client_id");
        String discordBotToken = config.get("discord_bot_token");

        GoogleOAuthManager googleOAuthManager = new GoogleOAuthManager(googleClientID, googleClientSecret, googleRefreshToken);

        if (discordClientID != null && discordBotToken != null) {
            DiscordInterface discordInterface = new DiscordInterface(discordClientID, discordBotToken);
            discordInterface.start();
        }

        if (googleOAuthManager.isAuthenticated()) {
            System.out.println("GOOGLE AUTHENTICATION SUCCESSFUL");
        }
        if (!googleOAuthManager.isAuthenticated() || discordClientID == null || discordBotToken == null) {
            Frame frame = new JFrame("Auth Window");
            GuiController controller = new GuiController();
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
            config.put("google_client_id", resp.data[0]);
            config.put("google_client_secret", resp.data[1]);
            config.put("discord_client_id", resp.data[2]);
            config.put("discord_bot_token", resp.data[3]);
            try {
                googleOAuthManager = new GoogleOAuthManager(resp.data[0], resp.data[1], "");
                googleOAuthManager.invokeFlow();
                if (googleOAuthManager.isAuthenticated()) {
                    config.put("google_refresh_token", googleOAuthManager.getCredentials().getRefreshToken());
                }
            } catch (Exception e) {
                // TODO: Handle errors with auth flow
                // invokeFlow() will return an error with good text in it to display.
                // the function also blocks until complete, so keep that in mind
            }
        }
    }
}
