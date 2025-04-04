package edu.wsu.cs320.RP;

import edu.wsu.cs320.GoogleCalToDiscord;
import edu.wsu.cs320.commands.SlashCommandInteractions;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;

public class DiscordInterface extends Thread{
    private final String id, token;
    private JDA bot;
    public DiscordInterface(String id, String token){
        this.id = id;
        this.token = token;
    }

    public void killBot(){
        bot.shutdown();
    }

    @Override
    public void run(){
        if (this.token == null || this.id == null) {
            System.out.println("Bot Token or Application Id was not supplied: will not run interface!");
            return;
        }

        GoogleCalendarServiceHandler calHandler = new GoogleCalendarServiceHandler(GoogleCalToDiscord.googleOAuthManager.getCredentials());

        RichPresence presence = new RichPresence();
        SlashCommandInteractions commands = new SlashCommandInteractions(presence, this);
        commands.setGoogleCalendarHandler(calHandler);

        this.bot = JDABuilder.createDefault(this.token)
                .addEventListeners(commands)
                .build();

        presence.startDiscordActivity(this.id);
    }

    public void sendMessageToUser(long userID, String message) {
        User user = this.bot.retrieveUserById(userID).complete();
        user.openPrivateChannel().map((chan) -> chan.sendMessage(message)).complete().queue();
    }
}
