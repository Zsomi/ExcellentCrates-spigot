package su.nightexpress.excellentcrates.data.crate;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.TimeUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserCrateData {

    private long cooldownTimestamp;
    private int openingStreak;

    private int  openings;
    private int milestone;

    private final Set<String> blacklistedRewards;
    private final Map<String, Integer> rewardWins;

    public UserCrateData() {
        this(0L, 0, 0, 0);
    }

    public UserCrateData(long cooldownTimestamp, int openingStreak, int openings, int milestone) {
        this(cooldownTimestamp, openingStreak, openings, milestone, new HashSet<>(), new HashMap<>());
    }

    public UserCrateData(long cooldownTimestamp, int openingStreak, int openings, int milestone,
                         @NotNull Set<String> blacklistedRewards, @NotNull Map<String, Integer> rewardWins) {
        this.cooldownTimestamp = cooldownTimestamp;
        this.setOpeningStreak(openingStreak);

        this.openings = openings;
        this.milestone = milestone;

        this.blacklistedRewards = new HashSet<>(blacklistedRewards);
        this.rewardWins = new HashMap<>(rewardWins);
    }

    public void resetCooldownAndStreak() {
        this.setCooldownTimestamp(0L);
        this.setOpeningStreak(0);
    }

    private void queryStreak() {
        if (this.cooldownTimestamp > 0 && TimeUtil.isPassed(this.cooldownTimestamp)) {
            this.resetCooldownAndStreak();
        }
    }

    public int queryOpeningStreak() {
        this.queryStreak();

        return this.getOpeningStreak();
    }

    public long queryCooldownTimestamp() {
        this.queryStreak();

        return this.getCooldownTimestamp();
    }

    public void setCooldown(long seconds) {
        this.setCooldownTimestamp(TimeUtil.createFutureTimestamp(seconds));
    }

    public boolean isOnCooldown() {
        return this.queryCooldownTimestamp() != 0;
    }

    public boolean isCooldownPermanent() {
        return this.cooldownTimestamp < 0;
    }

    public void addOpeningStreak(int amount) {
        this.setOpeningStreak(this.openingStreak + Math.abs(amount));
    }

    public void addOpenings(int amount) {
        this.setOpenings(this.openings + Math.abs(amount));
    }

    public void addMilestones(int amount) {
        this.setMilestone(this.milestone + Math.abs(amount));
    }


    public long getCooldownTimestamp() {
        return this.cooldownTimestamp;
    }

    public void setCooldownTimestamp(long cooldownTimestamp) {
        this.cooldownTimestamp = cooldownTimestamp;
    }

    public int getOpeningStreak() {
        return this.openingStreak;
    }

    public void setOpeningStreak(int openingStreak) {
        this.openingStreak = Math.max(0, openingStreak);
    }

    public int getOpenings() {
        return openings;
    }

    public void setOpenings(int openings) {
        this.openings = Math.max(0, openings);
    }

    public int getMilestone() {
        return milestone;
    }

    public void setMilestone(int milestone) {
        this.milestone = Math.max(0, milestone);
    }

    @NotNull
    public Set<String> getBlacklistedRewards() {
        return this.blacklistedRewards;
    }

    public boolean isBlacklisted(@NotNull String rewardId) {
        return this.blacklistedRewards.contains(rewardId.toLowerCase());
    }

    public void addBlacklist(@NotNull String rewardId) {
        this.blacklistedRewards.add(rewardId.toLowerCase());
    }

    public void removeBlacklist(@NotNull String rewardId) {
        this.blacklistedRewards.remove(rewardId.toLowerCase());
    }

    public boolean toggleBlacklist(@NotNull String rewardId) {
        if (this.isBlacklisted(rewardId)) {
            this.removeBlacklist(rewardId);
            return false;
        }
        this.addBlacklist(rewardId);
        return true;
    }

    @NotNull
    public Map<String, Integer> getRewardWins() {
        return this.rewardWins;
    }

    public int getRewardWins(@NotNull String rewardId) {
        return this.rewardWins.getOrDefault(rewardId.toLowerCase(), 0);
    }

    public void addRewardWin(@NotNull String rewardId) {
        this.addRewardWins(rewardId, 1);
    }

    public void addRewardWins(@NotNull String rewardId, int amount) {
        this.rewardWins.merge(rewardId.toLowerCase(), Math.max(0, amount), Integer::sum);
    }

    public void setRewardWins(@NotNull String rewardId, int amount) {
        this.rewardWins.put(rewardId.toLowerCase(), Math.max(0, amount));
    }
}
