package su.nightexpress.excellentcrates.editor.crate;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.crate.reward.AbstractReward;
import su.nightexpress.excellentcrates.crate.reward.ProgressionRef;
import su.nightexpress.excellentcrates.crate.reward.RewardDialogs;
import su.nightexpress.excellentcrates.crate.reward.RewardProgression;
import su.nightexpress.excellentcrates.dialog.DialogRegistry;
import su.nightexpress.excellentcrates.util.CrateUtils;
import su.nightexpress.excellentcrates.util.ItemHelper;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardProgressionContentMenu extends LinkedMenu<CratesPlugin, ProgressionRef> {

    private final DialogRegistry dialogs;

    public RewardProgressionContentMenu(@NotNull CratesPlugin plugin, @NotNull DialogRegistry dialogs) {
        super(plugin, MenuType.GENERIC_9X5, Lang.EDITOR_TITLE_REWARD_PROGRESSION_CONTENT.text());
        this.dialogs = dialogs;

        this.addItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true)
            .toMenuItem().setSlots(0,1,3,5,7).setPriority(-1));
        this.addItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true)
            .toMenuItem().setSlots(IntStream.range(36, 45).toArray()).setPriority(-1));

        this.addItem(MenuItem.buildReturn(this, 40, (viewer, event) -> {
            this.runNextTick(() -> plugin.getEditorManager().openRewardProgression(viewer.getPlayer(), this.getLink(viewer).reward()));
        }));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        ProgressionRef ref = this.getLink(player);
        RewardProgression level = ref.level();
        Crate crate = ref.reward().getCrate();
        Runnable flush = () -> this.flush(player);

        viewer.addItem(NightItem.fromType(Material.COMPARATOR)
            .setDisplayName(YELLOW.wrap(BOLD.wrap("Required Wins")))
            .setLore(Lists.newList(
                GRAY.wrap("Current: " + WHITE.wrap(String.valueOf(level.getRequiredCount()))),
                "",
                YELLOW.wrap("→ Click to change")
            ))
            .toMenuItem().setSlots(2).setHandler((viewer1, event) -> {
                this.dialogs.show(player, RewardDialogs.PROGRESSION_COUNT, ref, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.NAME_TAG)
            .setDisplayName(YELLOW.wrap(BOLD.wrap("Level Name")))
            .setLore(Lists.newList(
                GRAY.wrap("Current: " + WHITE.wrap(level.getName() != null ? level.getName() : "Default")),
                "",
                YELLOW.wrap("→ Click to change")
            ))
            .toMenuItem().setSlots(4).setHandler((viewer1, event) -> {
                this.dialogs.show(player, RewardDialogs.PROGRESSION_NAME, ref, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.COMMAND_BLOCK)
            .setDisplayName(YELLOW.wrap(BOLD.wrap("Commands")))
            .setLore(Lists.newList(
                GRAY.wrap("Current: " + WHITE.wrap(String.valueOf(level.getCommands().size()))),
                GRAY.wrap("Commands run when this level is reached."),
                "",
                YELLOW.wrap("→ Click to edit")
            ))
            .toMenuItem().setSlots(6).setHandler((viewer1, event) -> {
                this.dialogs.show(player, RewardDialogs.PROGRESSION_COMMANDS, ref, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.BARRIER)
            .setDisplayName(RED.wrap(BOLD.wrap("Delete Level")))
            .setLore(Lists.newList(
                GRAY.wrap("Permanently deletes this level."),
                "",
                RED.wrap("→ Press [Q]/Drop to delete")
            ))
            .toMenuItem().setSlots(8).setHandler((viewer1, event) -> {
                if (event.getClick() != ClickType.DROP) return;

                AbstractReward reward = ref.reward();
                reward.removeProgressionLevel(level);
                crate.markDirty();
                this.runNextTick(() -> plugin.getEditorManager().openRewardProgression(player, reward));
            }).build()
        );
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        List<AdaptedItem> items = this.getLink(viewer).level().getItems();

        for (int index = 0; index < CrateUtils.REWARD_ITEMS_LIMIT; index++) {
            if (index >= items.size()) break;

            int slot = index + 9;
            inventory.setItem(slot, ItemHelper.toItemStack(items.get(index)));
        }
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);

        ItemStack clicked = result.getItemStack();
        if (clicked == null || clicked.getType().isAir()) return;

        Player player = viewer.getPlayer();
        ItemStack copy = new ItemStack(clicked);
        ProgressionRef ref = this.getLink(viewer);
        RewardProgression level = ref.level();
        Crate crate = ref.reward().getCrate();
        Runnable flush = () -> this.flush(player);

        if (result.isInventory()) {
            level.addItem(ItemHelper.adapt(copy, true));
            crate.markDirty();
            this.runNextTick(flush);
        }
        else {
            int slot = result.getSlot();
            if (slot < 9 || slot >= 9 + CrateUtils.REWARD_ITEMS_LIMIT) return;

            Players.addItem(player, copy);

            if (event.isRightClick()) return;

            int index = slot - 9;
            if (index < level.getItems().size()) {
                level.getItems().remove(index);
                crate.markDirty();
                clicked.setAmount(0);
                this.runNextTick(flush);
            }
        }
    }
}
