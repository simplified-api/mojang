package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.annotations.SerializedName;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.simplified.gson.SerializedPath;
import dev.simplified.persistence.type.GsonType;
import dev.simplified.util.StringUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Getter
@GsonType
@NoArgsConstructor(access = AccessLevel.NONE)
public class MojangProperty {

    private String name = "textures";
    @SerializedName("value")
    private String raw = "";
    private @NotNull Optional<String> signature = Optional.empty();

    public @NotNull String getRawJson() {
        return new String(StringUtil.decodeBase64(this.getRaw()), StandardCharsets.UTF_8);
    }

    public @NotNull Value getValue() {
        return MinecraftApi.getGson().fromJson(this.getRawJson(), Value.class);
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