package edu.wsu.cs320.RP;

import com.google.api.services.calendar.model.Calendar;
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

    public void setCurCalendar(Calendar googleCal){
        if (commands != null){
            commands.setCurrentCalendar(googleCal);
        }
    }

    public Calendar getCurCalendar(){
        if (commands != null){
            return commands.getCurCalendar();
        }
        return null;
    }


    @Override
    public void run(){
        if (Token == null || ID == null) {
            System.out.println("Bot Token or Application Id was not supplied: will not run interface!");
            return;
        }

        Presence presence = new Presence(ID);
        commands = new SlashCommandInteractions(presence);
        setGoogleCalCredentials(manager, calHandler);

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
