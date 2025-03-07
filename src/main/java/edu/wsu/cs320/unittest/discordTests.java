package edu.wsu.cs320.unittest;


import edu.wsu.cs320.RP.Presence;
import edu.wsu.cs320.commands.SlashCommandInteractions;
import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.config.ConfigValues;

import edu.wsu.cs320.googleapi.GoogleOAuthManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static edu.wsu.cs320.GoogleCalToDiscord.config;
import static org.junit.Assert.*;
public class discordTests {
    @Test // Black box unit test
    public void testCalendarMenu(){
        List<String> names = new ArrayList<>();
        StringSelectMenu menu = StringSelectMenu.create("choose-calendar").addOption("1","1").build();

        names.add("1");
        StringSelectMenu menu2 = new SlashCommandInteractions(null).getCalendarMenu(names);
        assertEquals(menu.getOptions().get(0).getLabel(), menu2.getOptions().get(0).getLabel()); // consistent label and value
        assertEquals(menu.getOptions().get(0).getValue(), menu2.getOptions().get(0).getValue());

        names.add("2");
        menu2 = new SlashCommandInteractions(null).getCalendarMenu(names);
        assertNotEquals(menu, menu2); // too many options

        for (int i = 3; i < 100; i++){
            names.add(String.valueOf(i));
        }

        menu2 = new SlashCommandInteractions(null).getCalendarMenu(names);
        assertEquals("Next Page", menu2.getOptions().get(23).getLabel()); // Next Page exists
        assertNotEquals("Previous Page", menu2.getOptions().get(0).getLabel()); // Previous Page not on first page

    }

    @Test // White-box unit test
    public void test2(){

    }

    @Test // Integration test
    public void testCommands(){
        int expectedCommands = 4;
        int expectedCommandsWithOptions = 2;

        ConfigManager config = new ConfigManager();
        Presence presence = new Presence(config.get(ConfigValues.DISCORD_CLIENT_ID));
        SlashCommandInteractions commands = new SlashCommandInteractions(presence);
        JDA tester = JDABuilder.createDefault(config.get(ConfigValues.DISCORD_BOT_TOKEN))
                .addEventListeners(commands)
                .build();

        tester.retrieveCommands().queue(command ->{
            assertEquals(expectedCommands, command.size());    // correct number of correlating commands
        });


        tester.retrieveCommands().queue(command ->{
            int ops = 0;
            for (int i = 0; i < command.size(); i++){
                if (!command.get(i).getOptions().isEmpty()){
                    ops++;
                }
            }
            assertEquals(expectedCommandsWithOptions, ops);    // correct number of commands with attached options
        });
    }
}
