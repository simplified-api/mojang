package dev.sbs.minecraftapi.client.mojang.exception;

import dev.sbs.minecraftapi.MinecraftApi;
import dev.simplified.client.exception.ApiException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public final class MojangApiException extends ApiException {

    private final @NotNull MojangErrorResponse response;

    public MojangApiException(@NotNull String methodKey, @NotNull feign.Response response) {
        super(methodKey, response, "Mojang");
        this.response = this.getBody()
            .map(json -> super.fromJson(MinecraftApi.getGson(), json, MojangErrorResponse.class))
            .orElse(new MojangErrorResponse.Unknown());
    }

}
