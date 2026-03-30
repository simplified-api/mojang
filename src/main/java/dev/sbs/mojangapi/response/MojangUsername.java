package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class MojangUsername {

    @SerializedName("name")
    private @NotNull String username;
    @SerializedName("id")
    private @NotNull UUID uniqueId;
    private boolean legacy;
    private boolean demo;

}
