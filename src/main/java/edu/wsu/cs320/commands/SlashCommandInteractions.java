package edu.wsu.cs320.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class SlashCommandInteractions extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        System.out.println("Command used \"" + event.getName() + "\"");
        switch (event.getName()) {
            case "command_example":
                OptionMapping options = event.getOption("insert_option_name");
                String response = options.getAsString();

                event.reply("reply to command with option: " + response).setEphemeral(true).queue();
                break;
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        List<CommandData> commands = new ArrayList<>();

        // Slash command template with option
        OptionData option_example = new OptionData(OptionType.STRING, "insert_option_name", "insert_description", true);
        commands.add(Commands.slash("command_example", "command_example").addOptions(option_example));

        event.getJDA().updateCommands().addCommands(commands).queue();
    }
}
