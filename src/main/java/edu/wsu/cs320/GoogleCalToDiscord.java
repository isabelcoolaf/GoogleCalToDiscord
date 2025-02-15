package edu.wsu.cs320;

import edu.wsu.cs320.googleapi.GoogleOAuthManager;
import edu.wsu.cs320.gui.control.GuiController;
import edu.wsu.cs320.gui.control.GuiResponse;
import edu.wsu.cs320.RP.DiscordInterface;

import javax.swing.*;
import java.util.Arrays;

public class GoogleCalToDiscord {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Google Auth Window");
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
        try {
            GoogleOAuthManager manager = new GoogleOAuthManager(resp.data[0], resp.data[1], "");
            manager.invokeFlow();
        } catch (Exception e) {
            // TODO: Handle errors with auth flow
            // invokeFlow() will return an error with good text in it to display.
            // the function also blocks until complete, so keep that in mind
        }

        DiscordInterface discordInterface = new DiscordInterface("Application ID","Bot Token");
        discordInterface.start();
    }

}
