package su.nightexpress.excellentcrates.crate.reward;

import org.jetbrains.annotations.NotNull;

public record ProgressionRef(@NotNull AbstractReward reward, @NotNull RewardProgression level) {
}
