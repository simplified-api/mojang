package dev.sbs.minecraftapi.client.mojang.exception;

import com.google.gson.Gson;
import dev.simplified.client.exception.JsonApiException;
import org.jetbrains.annotations.NotNull;

public final class MojangApiException extends JsonApiException {

    public MojangApiException(@NotNull Gson gson, @NotNull String methodKey, @NotNull feign.Response response) {
        super(methodKey, response, "Mojang", gson, MojangErrorResponse.class, MojangErrorResponse.Unknown::new);
    }

    @Override
    public @NotNull MojangErrorResponse getResponse() {
        return (MojangErrorResponse) super.getResponse();
    }

}
