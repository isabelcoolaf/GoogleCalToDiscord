package edu.wsu.cs320.RP;

import edu.wsu.cs320.commands.SlashCommandInteractions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.IOException;

public class DiscordInterface extends Thread{
    private String ID, Token;
    public DiscordInterface(String id, String token){
        ID = id;
        Token = token;
    }

    @Override
    public void run(){
        Presence presence = new Presence(ID);

        // Bot token for using slash commands
        String token = Token;
        JDA bot = JDABuilder.createDefault(token)
                .addEventListeners(new SlashCommandInteractions(presence))
                .build();

        try {
            presence.Activity();
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }
}
