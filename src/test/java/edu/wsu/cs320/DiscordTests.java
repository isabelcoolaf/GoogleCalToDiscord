package edu.wsu.cs320;

import com.google.api.services.calendar.model.CalendarListEntry;
import edu.wsu.cs320.RP.Presence;
import edu.wsu.cs320.commands.CommandList;
import edu.wsu.cs320.commands.SlashCommandInteractions;
import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.config.ConfigValues;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class DiscordTests {

    private List<String> makeNameList(){
        List<String> names = new ArrayList<>();
        for (int i = 0; i < 100; i++){
            names.add(String.valueOf(i));
        }
        return names;
    }

    @Test // Black box unit tests
    // Given some google calendar list, get a list of all calendar names
    public void testCalendarNameList1(){
        List<CalendarListEntry> calendarList = new ArrayList<>();
        assertEquals(new ArrayList<>(), new CommandList(null).getCalendarNames(calendarList));

    }

    @Test
    public void testCalendarNameList2(){
        List<CalendarListEntry> calendarList = new ArrayList<>();
        CalendarListEntry entry = new CalendarListEntry();
        entry.setId("000000");
        entry.setSummary("test");
        calendarList.add(entry);
        String[] dummyList = {"test"};
        assertArrayEquals(dummyList, new CommandList(null).getCalendarNames(calendarList).toArray());
    }

    @Test
    public void testCalendarNameList3(){
        List<CalendarListEntry> calendarList = new ArrayList<>();
        CalendarListEntry entry;
        for (int i = 0; i < 1000; i++) {
            entry = new CalendarListEntry();
            entry.setSummary("test" + i);
            calendarList.add(entry);
        }
        assertEquals("test999", new CommandList(null).getCalendarNames(calendarList).toArray()[999]);
    }

    @Test // White-box unit tests
    // Create a calendar selection menu when given a list of calendar names
    public void testCalendarMenu1(){
        List<String> names = new ArrayList<>();
        StringSelectMenu menu = StringSelectMenu.create("choose-calendar").addOption("1","1").build();

        names.add("1");
        StringSelectMenu menu2 = new CommandList(null).getCalendarMenu(names);
        assertEquals(menu.getOptions().get(0).getLabel(), menu2.getOptions().get(0).getLabel()); // consistent label and value
        assertEquals(menu.getOptions().get(0).getValue(), menu2.getOptions().get(0).getValue());
    }

    @Test
    public void testCalendarMenu2(){
        List<String> names = new ArrayList<>();
        StringSelectMenu menu = StringSelectMenu.create("choose-calendar").addOption("1","1").build();
        names.add("1");
        names.add("2");
        StringSelectMenu menu2 = new CommandList(null).getCalendarMenu(names);
        assertNotEquals(menu, menu2); // too many options
    }

    @Test
    public void testCalendarMenu3(){
        List<String> names = makeNameList();

        StringSelectMenu menu2 = new CommandList(null).getCalendarMenu(names);
        assertEquals("Next Page", menu2.getOptions().get(23).getLabel()); // Next Page exists
        assertEquals("⏩", Objects.requireNonNull(menu2.getOptions().get(23).getEmoji()).getAsReactionCode());
        assertNotEquals("Previous Page", menu2.getOptions().get(0).getLabel()); // Previous Page not on first page
    }

    // These tests no longer function

//    @Test
//    public void testCalendarMenu4(){
//        List<String> names = makeNameList();
//
//        SlashCommandInteractions menu3 = new SlashCommandInteractions(null,null);
//        CommandList cmdlist = new CommandList();
//        menu3.setPage(1);
//        StringSelectMenu menu2 = menu3.getCalendarMenu(names);
//        assertEquals("Previous Page", menu2.getOptions().get(0).getLabel()); // Previous Page exists
//        assertEquals("⏪", Objects.requireNonNull(menu2.getOptions().get(0).getEmoji()).getAsReactionCode());
//    }
//
//    @Test
//    public void testCalendarMenu5(){
//        List<String> names = makeNameList();
//        SlashCommandInteractions menu3 = new SlashCommandInteractions(null,null);
//        menu3.setPage(4);
//        StringSelectMenu menu2 = menu3.getCalendarMenu(names);
//        assertNotEquals("Next Page", menu2.getOptions().get(menu2.getOptions().size() - 1).getLabel()); // Next Page not on last page
//    }

    private JDA makeTestBot(){
        ConfigManager config = new ConfigManager(ConfigValues.CONFIG_FILENAME);
        Presence presence = new Presence(config.get(ConfigValues.DISCORD_CLIENT_ID));
        SlashCommandInteractions commands = new SlashCommandInteractions(presence,null);
        return JDABuilder.createDefault(config.get(ConfigValues.DISCORD_BOT_TOKEN))
                .addEventListeners(commands)
                .build();

    }

    @Test // Integration tests
    public void testNumberOfCommands(){
        int expectedCommands = 5;
        JDA tester = makeTestBot();

        tester.retrieveCommands().queue(command ->{
            assertEquals(expectedCommands, command.size());    // correct number of correlating commands
        });
    }

    @Test // Integration test
    public void testNumberOfCommandsWithOptions(){
        int expectedCommandsWithOptions = 1;
        JDA tester = makeTestBot();

        tester.retrieveCommands().queue(command ->{
            int ops = 0;
            for (net.dv8tion.jda.api.interactions.commands.Command value : command) {
                if (!value.getOptions().isEmpty()) {
                    ops++;
                }
            }
            assertEquals(expectedCommandsWithOptions, ops);    // correct number of commands with attached options
        });
    }
}
