package su.nightexpress.excellentcrates.dialog.crate;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.config.Config;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.dialog.Dialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class CrateBlacklistDialog extends Dialog<Crate> {

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Crate.Blacklist.Title").text(title("Crate", "Blacklist Menu"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Crate.Blacklist.Body").dialogElement(400,
        "Select a blacklist GUI template for the crate.",
        "You can create and edit blacklist menus in the " + SOFT_YELLOW.wrap(Config.DIR_BLACKLIST) + " directory."
    );

    private static final TextLocale INPUT_ID = LangEntry.builder("Dialog.Crate.Blacklist.Input.Id").text(SOFT_YELLOW.wrap("Blacklist Menu"));

    private static final String JSON_ID = "id";

    private final CratesPlugin plugin;

    public CrateBlacklistDialog(@NotNull CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull Crate crate) {
        List<WrappedSingleOptionEntry> entries = new ArrayList<>();

        this.plugin.getCrateManager().getBlacklistNames().stream().sorted(String::compareTo).forEach(id -> {
            entries.add(new WrappedSingleOptionEntry(id, FileConfig.withExtension(id), crate.getBlacklistId().equalsIgnoreCase(id)));
        });

        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.singleOption(JSON_ID, INPUT_ID, entries).build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String id = nbtHolder.getText(JSON_ID, crate.getBlacklistId());
                crate.setBlacklistId(id);
                crate.markDirty();
                viewer.callback();
            });
        });
    }
}
