package su.nightexpress.excellentcrates.dialog.reward;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.crate.reward.ProgressionRef;
import su.nightexpress.excellentcrates.dialog.Dialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.Plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static su.nightexpress.excellentcrates.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardProgressionCommandsDialog extends Dialog<ProgressionRef> {

    private static final int COMMANDS_AMOUNT = 5;

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Reward.Progression.Commands.Title").text(title("Progression", "Level Commands"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Reward.Progression.Commands.Body").dialogElement(400,
        "Enter up to " + SOFT_YELLOW.wrap(COMMANDS_AMOUNT + " commands") + " that will be executed when a player reaches this progression level.",
        "",
        SOFT_YELLOW.wrap("→") + " Use the " + SOFT_YELLOW.wrap(PLAYER_NAME) + " placeholder to insert the player's name.",
        "",
        SOFT_YELLOW.wrap("→") + " You can use any placeholders from the " + SOFT_YELLOW.wrap(Plugins.PLACEHOLDER_API) + " plugin."
    );

    private static final TextLocale INPUT_COMMAND = LangEntry.builder("Dialog.Reward.Progression.Commands.Input.Command").text("Command " + SOFT_YELLOW.wrap("#%s"));

    private static final Function<Integer, String> JSON_COMMAND = index -> "command_" + index;

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull ProgressionRef ref) {
        List<WrappedDialogInput> inputs = new ArrayList<>();
        List<String> currentCommands = ref.level().getCommands();
        int size = Math.max(COMMANDS_AMOUNT, currentCommands.size());

        for (int index = 0; index < size; index++) {
            inputs.add(DialogInputs.text(JSON_COMMAND.apply(index), INPUT_COMMAND.text().formatted(String.valueOf(index + 1)))
                .initial(currentCommands.size() > index ? currentCommands.get(index) : "")
                .maxLength(400)
                .width(300)
                .build());
        }

        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(inputs)
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                List<String> commands = new ArrayList<>();

                for (int index = 0; index < size; index++) {
                    nbtHolder.getText(JSON_COMMAND.apply(index)).filter(Predicate.not(String::isBlank)).ifPresent(command -> {
                        if (command.startsWith("/")) command = command.substring(1);
                        commands.add(command);
                    });
                }

                ref.level().setCommands(commands);
                ref.reward().getCrate().markDirty();
                viewer.callback();
            });
        });
    }
}
