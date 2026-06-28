package su.nightexpress.excellentcrates.crate.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.api.crate.Reward;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.crate.impl.CrateSource;
import su.nightexpress.excellentcrates.data.crate.UserCrateData;
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

public class BlacklistMenu extends LinkedMenu<CratesPlugin, CrateSource> implements Filled<Reward>, ConfigBased {

    private int[]        rewardSlots;
    private String       allowedName;
    private List<String> allowedLore;
    private String       blacklistedName;
    private List<String> blacklistedLore;

    public BlacklistMenu(@NotNull CratesPlugin plugin, @NotNull FileConfig config) {
        super(plugin, MenuType.GENERIC_9X5, BLACK.wrap(CRATE_NAME));
        this.setApplyPlaceholderAPI(true);
        this.load(config);
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        CrateSource source = this.getLink(viewer.getPlayer());

        return source.getCrate().replacePlaceholders().apply(this.title);
    }

    @Override
    @NotNull
    public MenuFiller<Reward> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Crate crate = this.getLink(player).getCrate();
        CrateUser user = this.plugin.getUserManager().getOrFetch(player);
        UserCrateData data = user.getCrateData(crate);

        var autoFill = MenuFiller.builder(this);

        autoFill.setSlots(this.rewardSlots);
        autoFill.setItems(crate.getRewards().stream().filter(Reward::isRollable).toList());
        autoFill.setItemCreator(reward -> {
            boolean blacklisted = data.isBlacklisted(reward.getId());

            return NightItem.fromItemStack(reward.getPreviewItem())
                .ignoreNameAndLore()
                .setDisplayName(blacklisted ? this.blacklistedName : this.allowedName)
                .setLore(blacklisted ? this.blacklistedLore : this.allowedLore)
                .replacement(replacer -> {
                    replacer.replace(crate.replacePlaceholders()).replace(reward.replacePlaceholders());
                    if (this.applyPlaceholderAPI) replacer.replacePlaceholderAPI(player);
                });
        });

        autoFill.setItemClick(reward -> (viewer1, event) -> {
            boolean blacklisted = data.isBlacklisted(reward.getId());

            if (!blacklisted) {
                long allowed = crate.getRewards().stream()
                    .filter(Reward::isRollable)
                    .filter(other -> !data.isBlacklisted(other.getId()))
                    .count();
                if (allowed <= 1) {
                    Lang.CRATE_BLACKLIST_ERROR_LAST.message().send(player);
                    return;
                }
            }

            data.toggleBlacklist(reward.getId());
            this.runNextTick(() -> this.flush(player));
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
        this.rewardSlots = ConfigValue.create("Blacklist.Slots",
            new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34}
        ).read(config);

        this.allowedName = ConfigValue.create("Blacklist.Allowed.Name",
            GREEN.wrap("✔ ") + WHITE.wrap(REWARD_NAME)
        ).read(config);

        this.allowedLore = ConfigValue.create("Blacklist.Allowed.Lore", Lists.newList(
            "",
            GRAY.wrap("Status: " + GREEN.wrap("Allowed")),
            "",
            YELLOW.wrap("→ Click to blacklist this reward")
        )).read(config);

        this.blacklistedName = ConfigValue.create("Blacklist.Blacklisted.Name",
            RED.wrap("✖ ") + GRAY.wrap(REWARD_NAME)
        ).read(config);

        this.blacklistedLore = ConfigValue.create("Blacklist.Blacklisted.Lore", Lists.newList(
            "",
            GRAY.wrap("Status: " + RED.wrap("Blacklisted")),
            GRAY.wrap("You will " + RED.wrap("not") + " receive this reward."),
            "",
            YELLOW.wrap("→ Click to allow this reward")
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
