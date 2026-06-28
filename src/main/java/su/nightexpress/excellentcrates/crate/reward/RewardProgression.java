package su.nightexpress.excellentcrates.crate.reward;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.util.CrateUtils;
import su.nightexpress.excellentcrates.util.ItemHelper;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class RewardProgression {

    private int               requiredCount;
    private String            name;
    private List<AdaptedItem> items;
    private List<String>      commands;
    private AdaptedItem       displayItem;

    public RewardProgression() {
        this(0, null, new ArrayList<>(), new ArrayList<>(), null);
    }

    public RewardProgression(int requiredCount,
                             @Nullable String name,
                             @NotNull List<AdaptedItem> items,
                             @NotNull List<String> commands,
                             @Nullable AdaptedItem displayItem) {
        this.setRequiredCount(requiredCount);
        this.setName(name);
        this.setItems(items);
        this.setCommands(commands);
        this.setDisplayItem(displayItem);
    }

    @NotNull
    public static RewardProgression read(@NotNull FileConfig config, @NotNull String path) {
        RewardProgression level = new RewardProgression();

        level.setRequiredCount(config.getInt(path + ".Required_Count", 0));
        level.setName(config.getString(path + ".Name"));
        level.setCommands(config.getStringList(path + ".Commands"));

        List<AdaptedItem> items = new ArrayList<>();
        config.getSection(path + ".ItemsData").forEach(sId -> {
            ItemHelper.read(config, path + ".ItemsData." + sId).ifPresent(items::add);
        });
        level.setItems(items);

        if (config.contains(path + ".DisplayData")) {
            ItemHelper.read(config, path + ".DisplayData").ifPresent(level::setDisplayItem);
        }

        return level;
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Required_Count", this.requiredCount);
        config.set(path + ".Name", this.name);
        config.set(path + ".Commands", this.commands);

        config.remove(path + ".ItemsData");
        int count = 0;
        for (AdaptedItem item : this.items) {
            config.set(path + ".ItemsData." + count++, item);
        }

        config.remove(path + ".DisplayData");
        if (this.displayItem != null && this.displayItem.isValid()) {
            config.set(path + ".DisplayData", this.displayItem);
        }
    }

    public boolean hasItems() {
        return !this.items.isEmpty();
    }

    public boolean hasCommands() {
        return !this.commands.isEmpty();
    }

    public int getRequiredCount() {
        return this.requiredCount;
    }

    public void setRequiredCount(int requiredCount) {
        this.requiredCount = Math.max(0, requiredCount);
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    public void setName(@Nullable String name) {
        this.name = (name == null || name.isBlank()) ? null : name;
    }

    @NotNull
    public List<AdaptedItem> getItems() {
        return this.items;
    }

    public void setItems(@NotNull List<AdaptedItem> items) {
        this.items = new ArrayList<>(items.stream().filter(AdaptedItem::isValid).limit(CrateUtils.REWARD_ITEMS_LIMIT).toList());
    }

    public void addItem(@NotNull AdaptedItem item) {
        if (this.items.size() >= CrateUtils.REWARD_ITEMS_LIMIT) return;
        if (!item.isValid()) return;

        this.items.add(item);
    }

    @NotNull
    public List<String> getCommands() {
        return this.commands;
    }

    public void setCommands(@NotNull List<String> commands) {
        this.commands = new ArrayList<>(commands.stream().filter(Predicate.not(String::isBlank)).toList());
    }

    @Nullable
    public AdaptedItem getDisplayItem() {
        return this.displayItem;
    }

    public void setDisplayItem(@Nullable AdaptedItem displayItem) {
        this.displayItem = displayItem;
    }
}
