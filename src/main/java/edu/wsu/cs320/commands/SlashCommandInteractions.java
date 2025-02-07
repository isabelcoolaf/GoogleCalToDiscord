package edu.wsu.cs320.commands;

import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import edu.wsu.cs320.RP.Presence;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlashCommandInteractions extends ListenerAdapter {
    private Presence RichPresence;

    // Presence required so that commands can alter the data of the
    public SlashCommandInteractions(Presence RP) {
        RichPresence = RP;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        System.out.println("Command used \"" + event.getName() + "\"");
        switch (event.getName()) {
            case "command_example":
                OptionMapping options = event.getOption("insert_option_name");
                String response = options.getAsString();

                event.reply("reply to command with option: " + response).setEphemeral(true).queue();
                break;
            case "presence_type":
                OptionMapping presenceOptions = event.getOption("presence_type");
                String presenceResponse = presenceOptions.getAsString();

                Activity activity = RichPresence.getActivityState();

                Map<String, ActivityType> activityTypes = new HashMap<>();
                activityTypes.put("Playing", ActivityType.PLAYING);
                activityTypes.put("Watching", ActivityType.WATCHING);
                activityTypes.put("Listening", ActivityType.LISTENING);
                activityTypes.put("Streaming", ActivityType.STREAMING);
                activityTypes.put("Competing", ActivityType.COMPETING);

                ActivityType type = activityTypes.get(presenceResponse);
                activity.setType(type);

                RichPresence.setActivityState(activity);

                event.reply("Changed presence type to: " + presenceResponse).setEphemeral(true).queue();
                break;
        }
    }


    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        List<CommandData> commands = new ArrayList<>();

        // names must be lowercase
        // command names must match command functionality options
        OptionData option_example = new OptionData(OptionType.STRING, "insert_option_name", "insert_description", true);

        OptionData PresenceType = new OptionData(OptionType.STRING, "presence_type", "Select the presence type", true);
        String[] choices = {"Playing", "Watching", "Listening", "Streaming", "Competing"};
        for (String choice : choices) {
            PresenceType.addChoice(choice, choice);
        }


        String[] commandList = {"command_example", "presence_type"};
        String[] commandDescriptions = {"command_example", "Changes Presence Type"};
        OptionData[] options = {option_example, PresenceType};
        for (int i = 0; i < commandList.length; i++) {
            commands.add(Commands.slash(commandList[i], commandDescriptions[i])
                    .setContexts(InteractionContextType.ALL)
                    .setIntegrationTypes(IntegrationType.USER_INSTALL)
                    .addOptions(options[i])
            );
        }


        event.getJDA().updateCommands().addCommands(commands).queue();

    }
}