package su.nightexpress.excellentcrates.crate.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.crate.impl.CrateSource;
import su.nightexpress.excellentcrates.crate.reward.AbstractReward;
import su.nightexpress.excellentcrates.crate.reward.RewardProgression;
import su.nightexpress.excellentcrates.crate.reward.impl.ItemReward;
import su.nightexpress.excellentcrates.user.CrateUser;
import su.nightexpress.excellentcrates.util.ItemHelper;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.excellentcrates.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardLevelViewerMenu extends LinkedMenu<CratesPlugin, RewardLevelViewerMenu.Data> {

    public record Data(@NotNull CrateSource source, @NotNull AbstractReward reward, int index) {}

    private static final int[] ITEM_SLOTS = {10,11,12,13,14,15,16,19,20,21,22,23,24,25};

    public RewardLevelViewerMenu(@NotNull CratesPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X4, BLACK.wrap(REWARD_NAME));

        this.addItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true)
            .toMenuItem().setSlots(0,1,2,3,5,6,7,8,9,17,18,26).setPriority(-1));
        this.addItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true)
            .toMenuItem().setSlots(IntStream.range(27, 36).toArray()).setPriority(-1));

        this.addItem(MenuItem.buildReturn(this, 31, (viewer, event) -> {
            Data data = this.getLink(viewer.getPlayer());
            this.runNextTick(() -> plugin.getCrateManager().openRewardLevels(viewer.getPlayer(), data.source));
        }).setPriority(MenuItem.HIGH_PRIORITY));
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        Data data = this.getLink(viewer.getPlayer());
        return data.reward.replacePlaceholders().apply(this.title);
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        AbstractReward reward = data.reward;

        List<RewardProgression> levels = reward.getProgressionLevels();
        int total = 1 + levels.size();
        int index = Math.max(0, Math.min(data.index, total - 1));

        CrateUser user = this.plugin.getUserManager().getOrFetch(player);
        int wins = user.getCrateData(reward.getCrate()).getRewardWins(reward.getId());

        String name;
        int requiredWins;
        boolean reached;
        if (index == 0) {
            name = "Base Reward";
            requiredWins = 0;
            reached = true;
        }
        else {
            RewardProgression level = levels.get(index - 1);
            name = level.getName() != null ? level.getName() : ("Level " + index);
            requiredWins = level.getRequiredCount();
            reached = wins >= requiredWins;
        }

        viewer.addItem(NightItem.fromType(Material.PAPER)
            .setDisplayName(WHITE.wrap(BOLD.wrap(name)))
            .setLore(Lists.newList(
                GRAY.wrap("Level: " + WHITE.wrap((index + 1) + "/" + total)),
                GRAY.wrap("Required wins: " + WHITE.wrap(String.valueOf(requiredWins))),
                GRAY.wrap("Your wins: " + WHITE.wrap(String.valueOf(wins))),
                "",
                reached ? GREEN.wrap("✔ Unlocked") : RED.wrap("✖ Locked")
            ))
            .toMenuItem().setSlots(4).build()
        );

        if (index > 0) {
            int target = index - 1;
            viewer.addItem(NightItem.fromType(Material.ARROW)
                .setDisplayName(YELLOW.wrap(BOLD.wrap("Previous Level")))
                .toMenuItem().setSlots(28).setHandler((viewer1, event) ->
                    this.runNextTick(() -> plugin.getCrateManager().openRewardLevelViewer(player, data.source, reward, target))
                ).build()
            );
        }

        if (index < total - 1) {
            int target = index + 1;
            viewer.addItem(NightItem.fromType(Material.ARROW)
                .setDisplayName(YELLOW.wrap(BOLD.wrap("Next Level")))
                .toMenuItem().setSlots(34).setHandler((viewer1, event) ->
                    this.runNextTick(() -> plugin.getCrateManager().openRewardLevelViewer(player, data.source, reward, target))
                ).build()
            );
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        Data data = this.getLink(viewer.getPlayer());
        AbstractReward reward = data.reward;

        List<RewardProgression> levels = reward.getProgressionLevels();
        int total = 1 + levels.size();
        int index = Math.max(0, Math.min(data.index, total - 1));

        List<AdaptedItem> items;
        if (index == 0) {
            items = reward instanceof ItemReward itemReward ? itemReward.getItems() : List.of();
        }
        else {
            items = levels.get(index - 1).getItems();
        }

        for (int i = 0; i < items.size() && i < ITEM_SLOTS.length; i++) {
            inventory.setItem(ITEM_SLOTS[i], ItemHelper.toItemStack(items.get(i)));
        }
    }
}
