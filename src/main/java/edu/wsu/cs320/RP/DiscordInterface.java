package edu.wsu.cs320.RP;

import edu.wsu.cs320.commands.SlashCommandInteractions;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import edu.wsu.cs320.googleapi.GoogleOAuthManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.IOException;

public class DiscordInterface extends Thread{
    private final String ID, Token;
    private GoogleOAuthManager manager;
    private GoogleCalendarServiceHandler calHandler;
    private SlashCommandInteractions commands;
    public DiscordInterface(String id, String token){
        ID = id;
        Token = token;
    }

    public void setGoogleCalCredentials(GoogleOAuthManager m, GoogleCalendarServiceHandler h){
        manager = m;
        calHandler = h;
        if (commands != null && manager != null && manager.isAuthenticated()){
            commands.setGoogleCalendarHandler(calHandler);
        }
    }

    public void setCurCalendar(com.google.api.services.calendar.model.Calendar googleCal){
        if (commands != null){
            commands.setCurrentCalendar(googleCal);
        }
    }

    public com.google.api.services.calendar.model.Calendar getCurCalendar(){
        if (commands != null){
            return commands.getCurCalendar();
        }
        return null;
    }


    @Override
    public void run(){
        Presence presence = new Presence(ID);
        commands = new SlashCommandInteractions(presence);
        setGoogleCalCredentials(manager, calHandler);

        // Bot token for using slash commands
        String token = Token;
        JDA bot = JDABuilder.createDefault(token)
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
