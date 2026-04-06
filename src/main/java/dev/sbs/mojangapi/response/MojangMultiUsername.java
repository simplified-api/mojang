package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class MojangMultiUsername {

    private final @NotNull ConcurrentList<MojangUsername> profiles;

    public @NotNull Optional<MojangUsername> getProfile(@NotNull String username) {
        return this.getProfiles()
            .stream()
            .filter(response -> response.getUsername().equalsIgnoreCase(username))
            .findFirst();
    }

    public @NotNull Optional<MojangUsername> getProfile(@NotNull UUID uniqueId) {
        return this.getProfiles()
            .stream()
            .filter(response -> response.getUniqueId().equals(uniqueId))
            .findFirst();
    }

    public @NotNull Optional<UUID> getUniqueId(@NotNull String username) {
        return this.getProfile(username).map(MojangUsername::getUniqueId);
    }

    public @NotNull Optional<String> getUsername(@NotNull UUID uniqueId) {
        return this.getProfile(uniqueId).map(MojangUsername::getUsername);
    }

    public static class Deserializer implements JsonDeserializer<MojangMultiUsername> {

        @Override
        public MojangMultiUsername deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            Gson gson = MinecraftApi.getGson();

            return new MojangMultiUsername(
                jsonElement.getAsJsonArray()
                    .asList()
                    .stream()
                    .map(profile -> gson.fromJson(profile, MojangUsername.class))
                    .collect(Concurrent.toUnmodifiableList())
            );
        }

    }

}
