package su.nightexpress.excellentcrates.editor.crate;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.crate.reward.AbstractReward;
import su.nightexpress.excellentcrates.crate.reward.RewardProgression;
import su.nightexpress.excellentcrates.util.ItemHelper;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardProgressionListMenu extends LinkedMenu<CratesPlugin, AbstractReward> implements Filled<RewardProgression> {

    public RewardProgressionListMenu(@NotNull CratesPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X5, Lang.EDITOR_TITLE_REWARD_PROGRESSION.text());

        this.addItem(MenuItem.buildReturn(this, 40, (viewer, event) -> {
            this.runNextTick(() -> plugin.getEditorManager().openRewardOptions(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addItem(MenuItem.buildNextPage(this, 41));
        this.addItem(MenuItem.buildPreviousPage(this, 39));

        this.addItem(NightItem.fromType(Material.LIME_DYE)
            .setDisplayName(GREEN.wrap(BOLD.wrap("Add Level")))
            .setLore(Lists.newList(
                GRAY.wrap("Adds a new progression level"),
                GRAY.wrap("to this reward."),
                "",
                YELLOW.wrap("→ Click to add")
            ))
            .toMenuItem().setSlots(42).setHandler((viewer, event) -> {
                AbstractReward reward = this.getLink(viewer);
                reward.addProgressionLevel(new RewardProgression());
                reward.getCrate().markDirty();
                this.runNextTick(() -> this.flush(viewer));
            })
        );

        this.addItem(NightItem.fromType(Material.GRAY_STAINED_GLASS_PANE).setHideTooltip(true)
            .toMenuItem().setSlots(IntStream.range(36, 45).toArray()).setPriority(-1)
        );
    }

    @Override
    @NotNull
    public MenuFiller<RewardProgression> createFiller(@NotNull MenuViewer viewer) {
        AbstractReward reward = this.getLink(viewer);
        List<RewardProgression> levels = reward.getProgressionLevels();

        var autoFill = MenuFiller.builder(this);

        autoFill.setSlots(IntStream.range(0, 36).toArray());
        autoFill.setItems(levels);
        autoFill.setItemCreator(level -> {
            int index = levels.indexOf(level);
            String name = level.getName() != null ? level.getName() : ("Level " + (index + 1));

            return NightItem.fromItemStack(this.getDisplayStack(level))
                .hideAllComponents()
                .setDisplayName(WHITE.wrap(BOLD.wrap(name)))
                .setLore(Lists.newList(
                    GRAY.wrap("Required Wins: " + WHITE.wrap(String.valueOf(level.getRequiredCount()))),
                    GRAY.wrap("Items: " + WHITE.wrap(String.valueOf(level.getItems().size()))),
                    GRAY.wrap("Commands: " + WHITE.wrap(String.valueOf(level.getCommands().size()))),
                    "",
                    YELLOW.wrap("→ Click to edit")
                ));
        });

        autoFill.setItemClick(level -> (viewer1, event) -> {
            this.runNextTick(() -> plugin.getEditorManager().openRewardProgressionContent(viewer1.getPlayer(), reward, level));
        });

        return autoFill.build();
    }

    @NotNull
    private org.bukkit.inventory.ItemStack getDisplayStack(@NotNull RewardProgression level) {
        AdaptedItem display = level.getDisplayItem();
        if (display != null && display.isValid()) {
            return ItemHelper.toItemStack(display);
        }
        if (level.hasItems()) {
            return ItemHelper.toItemStack(level.getItems().getFirst());
        }
        return new org.bukkit.inventory.ItemStack(Material.EXPERIENCE_BOTTLE);
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
