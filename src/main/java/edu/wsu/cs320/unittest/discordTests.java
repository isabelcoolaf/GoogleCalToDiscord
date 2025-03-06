package edu.wsu.cs320.unittest;

import edu.wsu.cs320.GoogleCalToDiscord;
import edu.wsu.cs320.commands.SlashCommandInteractions;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void test3(){

    }
}
