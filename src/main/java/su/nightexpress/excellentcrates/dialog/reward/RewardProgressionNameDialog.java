package su.nightexpress.excellentcrates.dialog.reward;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.crate.reward.ProgressionRef;
import su.nightexpress.excellentcrates.dialog.Dialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardProgressionNameDialog extends Dialog<ProgressionRef> {

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Reward.Progression.Name.Title").text(title("Progression", "Level Name"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Reward.Progression.Name.Body").dialogElement(400,
        "Set an optional " + SOFT_YELLOW.wrap("display name") + " for this progression level. Leave empty to use a default name."
    );

    private static final TextLocale INPUT = LangEntry.builder("Dialog.Reward.Progression.Name.Input").text(SOFT_YELLOW.wrap("Level Name"));

    private static final String JSON_NAME = "name";

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull ProgressionRef ref) {
        String current = ref.level().getName() == null ? "" : ref.level().getName();

        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.text(JSON_NAME, INPUT).initial(current).maxLength(64).width(300).build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String name = nbtHolder.getText(JSON_NAME).orElse("");
                ref.level().setName(name);
                ref.reward().getCrate().markDirty();
                viewer.callback();
            });
        });
    }
}
