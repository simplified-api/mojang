package dev.sbs.minecraftapi.client.mojang.request;

import dev.sbs.minecraftapi.client.mojang.exception.MojangApiException;
import dev.sbs.minecraftapi.client.mojang.response.MojangPropertiesResponse;
import feign.Param;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface MojangSessionRequest extends IMojangRequest {

    @RequestLine("GET /session/minecraft/profile/{uniqueId}?unsigned=false")
    @NotNull MojangPropertiesResponse getProperties(@NotNull @Param("uniqueId") UUID uniqueId) throws MojangApiException;

}
