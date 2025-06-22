package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class MojangMultiUsernameResponse {

    private final @NotNull ConcurrentList<MojangUsernameResponse> profiles;

    public @NotNull Optional<MojangUsernameResponse> getProfile(@NotNull String username) {
        return this.getProfiles()
            .stream()
            .filter(response -> response.getUsername().equalsIgnoreCase(username))
            .findFirst();
    }

    public @NotNull Optional<MojangUsernameResponse> getProfile(@NotNull UUID uniqueId) {
        return this.getProfiles()
            .stream()
            .filter(response -> response.getUniqueId().equals(uniqueId))
            .findFirst();
    }

    public @NotNull Optional<UUID> getUniqueId(@NotNull String username) {
        return this.getProfile(username).map(MojangUsernameResponse::getUniqueId);
    }

    public @NotNull Optional<String> getUsername(@NotNull UUID uniqueId) {
        return this.getProfile(uniqueId).map(MojangUsernameResponse::getUsername);
    }

    public static class Deserializer implements JsonDeserializer<MojangMultiUsernameResponse> {

        @Override
        public MojangMultiUsernameResponse deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            Gson gson = SimplifiedApi.getGson();

            return new MojangMultiUsernameResponse(
                jsonElement.getAsJsonArray()
                    .asList()
                    .stream()
                    .map(profile -> gson.fromJson(profile, MojangUsernameResponse.class))
                    .collect(Concurrent.toUnmodifiableList())
            );
        }

    }

}
