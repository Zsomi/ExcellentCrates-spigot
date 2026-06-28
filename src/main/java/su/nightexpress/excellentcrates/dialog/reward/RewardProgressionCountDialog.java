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

public class RewardProgressionCountDialog extends Dialog<ProgressionRef> {

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Reward.Progression.Count.Title").text(title("Progression", "Required Wins"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Reward.Progression.Count.Body").dialogElement(400,
        "Set how many times a player must " + SOFT_YELLOW.wrap("win this reward") + " before this progression level becomes active.",
        "",
        SOFT_YELLOW.wrap("→") + " The highest level whose required wins is reached will be granted instead of the base reward."
    );

    private static final TextLocale INPUT = LangEntry.builder("Dialog.Reward.Progression.Count.Input").text(SOFT_YELLOW.wrap("Required Wins"));

    private static final String JSON_COUNT = "count";

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull ProgressionRef ref) {
        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.text(JSON_COUNT, INPUT).initial(String.valueOf(ref.level().getRequiredCount())).maxLength(10).build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                int count = (int) nbtHolder.getDouble(JSON_COUNT, ref.level().getRequiredCount());
                ref.level().setRequiredCount(count);
                ref.reward().getCrate().markDirty();
                viewer.callback();
            });
        });
    }
}
