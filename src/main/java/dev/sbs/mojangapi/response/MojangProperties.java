package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.annotations.SerializedName;
import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class MojangProperties {

    @SerializedName("id")
    private UUID uniqueId;
    @SerializedName("name")
    private String username;
    private ConcurrentList<MojangProperty> properties = Concurrent.newList();
    private ConcurrentList<String> profileActions = Concurrent.newList();

    public @NotNull MojangProperty getProperty() {
        return this.getProperties()
            .findFirst()
            .orElseThrow();
    }

}
