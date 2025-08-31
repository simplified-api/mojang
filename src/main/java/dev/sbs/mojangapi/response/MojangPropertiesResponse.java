package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.annotations.SerializedName;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.minecraftapi.client.mojang.profile.MojangProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class MojangPropertiesResponse {

    @SerializedName("id")
    private UUID uniqueId;
    @SerializedName("name")
    private String username;
    private ConcurrentList<MojangProperty> properties = Concurrent.newList();
    private ConcurrentList<String> profileActions = Concurrent.newList();

    public @NotNull MojangProperty getProperty() {
        return this.getProperties()
            .getFirst()
            .orElseThrow();
    }

}
