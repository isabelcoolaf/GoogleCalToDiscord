package edu.wsu.cs320.RP;

import com.google.api.services.calendar.model.Calendar;
import edu.wsu.cs320.GoogleCalToDiscord;
import edu.wsu.cs320.commands.SlashCommandInteractions;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.IOException;

public class DiscordInterface extends Thread{
    private final String ID, Token;
    private SlashCommandInteractions commands;
    public DiscordInterface(String id, String token){
        ID = id;
        Token = token;
    }

    public void setCurCalendar(String googleCalID){
        if (commands != null){
            commands.setCurrentCalendar(googleCalID);
        }
    }

    @Override
    public void run(){
        if (Token == null || ID == null) {
            System.out.println("Bot Token or Application Id was not supplied: will not run interface!");
            return;
        }

        GoogleCalendarServiceHandler calHandler = new GoogleCalendarServiceHandler(GoogleCalToDiscord.googleOAuthManager.getCredentials());

        Presence presence = new Presence(ID);
        commands = new SlashCommandInteractions(presence);
        commands.setGoogleCalendarHandler(calHandler);

        // Bot token for using slash commands
        JDA bot = JDABuilder.createDefault(Token)
                .addEventListeners(commands)
                .build();

        try {
            presence.Activity();
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }
}
