package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.annotations.SerializedName;
import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.StringUtil;
import dev.sbs.api.io.gson.SerializedPath;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Getter
public class MojangPropertiesResponse {

    @SerializedName("id")
    private UUID uniqueId;
    @SerializedName("name")
    private String username;
    private ConcurrentList<Property> properties = Concurrent.newList();
    private ConcurrentList<String> profileActions = Concurrent.newList();

    public @NotNull Property getProperty() {
        return this.getProperties()
            .getFirst()
            .orElseThrow();
    }

    @Getter
    public static class Property {

        private String name;
        @SerializedName("value")
        private String raw;
        private Optional<String> signature = Optional.empty();

        public String getRawJson() {
            return new String(StringUtil.decodeBase64(this.getRaw()), StandardCharsets.UTF_8);
        }

        public Value getValue() {
            return SimplifiedApi.getGson().fromJson(this.getRawJson(), Value.class);
        }

        @Getter
        public static class Value {

            private Instant timestamp;
            @SerializedName("profileId")
            private UUID uniqueId;
            @SerializedName("profileName")
            private String username;
            private boolean signatureRequired;
            @SerializedPath("textures.SKIN.url")
            private Optional<String> skinUrl = Optional.empty();
            @SerializedPath("textures.CAPE.url")
            private Optional<String> capeUrl = Optional.empty();
            @SerializedPath("textures.SKIN.metadata.model")
            private Optional<String> capeModel = Optional.empty();

            public boolean isSlim() {
                return this.getCapeModel()
                    .map(model -> model.equals("slim"))
                    .orElse(this.isDefaultSlim());
            }

            public boolean isDefaultSlim() {
                return this.getUniqueId().hashCode() % 2 == 1;
            }

        }

    }

}
