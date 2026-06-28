package su.nightexpress.excellentcrates.crate.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.api.crate.Reward;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.crate.impl.CrateSource;
import su.nightexpress.excellentcrates.crate.reward.AbstractReward;
import su.nightexpress.excellentcrates.crate.reward.RewardProgression;
import su.nightexpress.excellentcrates.user.CrateUser;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.excellentcrates.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardLevelsMenu extends LinkedMenu<CratesPlugin, CrateSource> implements Filled<Reward>, ConfigBased {

    private static final String PLACEHOLDER_WINS        = "%reward_wins%";
    private static final String PLACEHOLDER_LEVELS_LIST = "%levels_list%";

    private int[]        rewardSlots;
    private String       rewardName;
    private List<String> rewardLore;

    public RewardLevelsMenu(@NotNull CratesPlugin plugin, @NotNull FileConfig config) {
        super(plugin, MenuType.GENERIC_9X5, BLACK.wrap("Reward Levels » " + CRATE_NAME));
        this.setApplyPlaceholderAPI(true);
        this.load(config);
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        return this.getLink(viewer.getPlayer()).getCrate().replacePlaceholders().apply(this.title);
    }

    @Override
    @NotNull
    public MenuFiller<Reward> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Crate crate = this.getLink(player).getCrate();
        CrateUser user = this.plugin.getUserManager().getOrFetch(player);

        var autoFill = MenuFiller.builder(this);

        autoFill.setSlots(this.rewardSlots);
        autoFill.setItems(crate.getRewards().stream()
            .filter(reward -> reward instanceof AbstractReward ar && ar.hasProgression())
            .toList());
        autoFill.setItemCreator(reward -> {
            AbstractReward abstractReward = (AbstractReward) reward;
            int wins = user.getCrateData(crate).getRewardWins(reward.getId());

            List<String> levelLines = new ArrayList<>();
            List<RewardProgression> levels = abstractReward.getProgressionLevels();
            for (int i = 0; i < levels.size(); i++) {
                RewardProgression level = levels.get(i);
                String name = level.getName() != null ? level.getName() : ("Level " + (i + 1));
                boolean reached = wins >= level.getRequiredCount();
                String marker = reached ? GREEN.wrap("✔") : RED.wrap("✖");
                levelLines.add(GRAY.wrap(marker + " " + WHITE.wrap(name) + " " + DARK_GRAY.wrap("(" + level.getRequiredCount() + " wins)")));
            }

            return NightItem.fromItemStack(reward.getPreviewItem())
                .hideAllComponents()
                .setDisplayName(this.rewardName)
                .setLore(this.rewardLore)
                .replacement(replacer -> {
                    replacer
                        .replace(PLACEHOLDER_WINS, () -> String.valueOf(wins))
                        .replace(PLACEHOLDER_LEVELS_LIST, levelLines)
                        .replace(crate.replacePlaceholders())
                        .replace(reward.replacePlaceholders());
                    if (this.applyPlaceholderAPI) replacer.replacePlaceholderAPI(player);
                });
        });

        autoFill.setItemClick(reward -> (viewer1, event) -> {
            CrateSource source = this.getLink(viewer1.getPlayer());
            this.runNextTick(() -> plugin.getCrateManager().openRewardLevelViewer(viewer1.getPlayer(), source, (AbstractReward) reward, 0));
        });

        return autoFill.build();
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.rewardSlots = ConfigValue.create("Levels.Slots",
            new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34}
        ).read(config);

        this.rewardName = ConfigValue.create("Levels.Reward.Name",
            REWARD_NAME
        ).read(config);

        this.rewardLore = ConfigValue.create("Levels.Reward.Lore", Lists.newList(
            "",
            GRAY.wrap("Your wins: " + WHITE.wrap(PLACEHOLDER_WINS)),
            "",
            GRAY.wrap("Levels:"),
            PLACEHOLDER_LEVELS_LIST,
            "",
            YELLOW.wrap("→ Click to view levels")
        )).read(config);

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, (viewer, event) -> {
            CrateSource source = this.getLink(viewer.getPlayer());
            this.runNextTick(() -> plugin.getCrateManager().previewCrate(viewer.getPlayer(), source));
        }).setPriority(MenuItem.HIGH_PRIORITY));

        loader.addDefaultItem(MenuItem.buildNextPage(this, 44).setPriority(MenuItem.HIGH_PRIORITY));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 36).setPriority(MenuItem.HIGH_PRIORITY));

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem()
            .setSlots(0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,37,38,39,41,42,43));
    }
}
