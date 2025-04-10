package edu.wsu.cs320.RP;

import edu.wsu.cs320.GoogleCalToDiscord;
import edu.wsu.cs320.commands.SlashCommandInteractions;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;

public class DiscordInterface extends Thread{
    private final String id, token;
    private DiscordRichPresence richPresence;
    private JDA bot;
    public DiscordInterface(String id, String token){
        this.id = id;
        this.token = token;
    }

    public DiscordRichPresence getRichPresence(){
        return this.richPresence;
    }

    public void killBot(){
        bot.shutdown();
    }

    @Override
    public void run(){
        GoogleCalendarServiceHandler calHandler = new GoogleCalendarServiceHandler(GoogleCalToDiscord.googleOAuthManager.getCredentials());

        richPresence = new DiscordRichPresence();
        SlashCommandInteractions commands = new SlashCommandInteractions(richPresence, this);
        commands.setGoogleCalendarHandler(calHandler);

        this.bot = JDABuilder.createDefault(this.token)
                .addEventListeners(commands)
                .build();

        richPresence.startDiscordActivity(this.id);
    }

    public void sendMessageToUser(long userID, String message) {
        User user = this.bot.retrieveUserById(userID).complete();
        user.openPrivateChannel().map((chan) -> chan.sendMessage(message)).complete().queue();
    }
}
