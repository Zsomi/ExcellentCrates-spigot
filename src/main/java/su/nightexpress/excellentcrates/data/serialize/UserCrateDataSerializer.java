package su.nightexpress.excellentcrates.data.serialize;

import com.google.gson.*;
import su.nightexpress.excellentcrates.data.crate.UserCrateData;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UserCrateDataSerializer implements JsonSerializer<UserCrateData>, JsonDeserializer<UserCrateData> {

    @Override
    public UserCrateData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        long openCooldown = object.get("openCooldown").getAsLong();
        int openingStreak = Optional.ofNullable(object.get("openingStreak")).map(JsonElement::getAsInt).orElse(openCooldown != 0 ? 1 : 0);
        int openings = object.get("openings").getAsInt();
        int milestones = object.get("milestones").getAsInt();

        Set<String> blacklist = new HashSet<>();
        if (object.has("blacklist") && object.get("blacklist").isJsonArray()) {
            object.getAsJsonArray("blacklist").forEach(element -> blacklist.add(element.getAsString()));
        }

        Map<String, Integer> rewardWins = new HashMap<>();
        if (object.has("rewardWins") && object.get("rewardWins").isJsonObject()) {
            object.getAsJsonObject("rewardWins").entrySet().forEach(entry -> rewardWins.put(entry.getKey(), entry.getValue().getAsInt()));
        }

        return new UserCrateData(openCooldown, openingStreak, openings, milestones, blacklist, rewardWins);
    }

    @Override
    public JsonElement serialize(UserCrateData data, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.addProperty("openCooldown", data.getCooldownTimestamp());
        object.addProperty("openingStreak", data.getOpeningStreak());
        object.addProperty("openings", data.getOpenings());
        object.addProperty("milestones", data.getMilestone());

        if (!data.getBlacklistedRewards().isEmpty()) {
            JsonArray blacklist = new JsonArray();
            data.getBlacklistedRewards().forEach(blacklist::add);
            object.add("blacklist", blacklist);
        }

        if (!data.getRewardWins().isEmpty()) {
            JsonObject rewardWins = new JsonObject();
            data.getRewardWins().forEach(rewardWins::addProperty);
            object.add("rewardWins", rewardWins);
        }

        return object;
    }
}
