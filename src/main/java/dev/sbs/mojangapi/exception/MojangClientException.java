package dev.sbs.minecraftapi.client.mojang.exception;

import dev.sbs.api.client.exception.ClientException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public final class MojangClientException extends ClientException {

    private final @NotNull MojangErrorResponse response;

    public MojangClientException(@NotNull String methodKey, @NotNull feign.Response response) {
        super(methodKey, response, "Mojang");
        this.response = this.getBody()
            .map(json -> super.fromJson(json, MojangErrorResponse.class))
            .orElse(new MojangErrorResponse.Unknown());
    }

}
