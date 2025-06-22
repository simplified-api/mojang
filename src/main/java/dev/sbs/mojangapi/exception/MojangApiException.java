package dev.sbs.minecraftapi.client.mojang.exception;

import dev.sbs.api.client.exception.ApiException;
import feign.FeignException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public final class MojangApiException extends ApiException {

    private final @NotNull MojangErrorResponse response;

    public MojangApiException(@NotNull FeignException exception) {
        super(exception, "Mojang");
        this.response = this.getBody()
            .map(json -> super.fromJson(json, MojangErrorResponse.class))
            .orElse(new MojangErrorResponse.Unknown());
    }

}
