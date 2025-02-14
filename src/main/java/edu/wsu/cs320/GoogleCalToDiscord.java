package edu.wsu.cs320;

import edu.wsu.cs320.RP.DiscordInterface;
import edu.wsu.cs320.gui.GoogleAuthWindow.GoogleAuthWindow;

import javax.swing.*;

public class GoogleCalToDiscord {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Google Auth Window");
        GoogleAuthWindow authWindow = new GoogleAuthWindow();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(authWindow.mainPanel);
        frame.setSize(650, 150);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        DiscordInterface discordInterface = new DiscordInterface("Application ID","Bot Token");
        discordInterface.start();

    }

}
