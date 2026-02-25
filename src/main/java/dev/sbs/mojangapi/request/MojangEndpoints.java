package dev.sbs.minecraftapi.client.mojang.request;

import dev.sbs.api.client.request.Endpoints;
import dev.sbs.api.client.request.expander.StringArrayQuoteExpander;
import dev.sbs.api.client.route.Route;
import dev.sbs.minecraftapi.client.mojang.MojangClient;
import dev.sbs.minecraftapi.client.mojang.exception.MojangApiException;
import dev.sbs.minecraftapi.client.mojang.response.MojangMultiUsernameResponse;
import dev.sbs.minecraftapi.client.mojang.response.MojangPropertiesResponse;
import dev.sbs.minecraftapi.client.mojang.response.MojangUsernameResponse;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

@Route("api.minecraftservices.com")
public interface MojangEndpoints extends Endpoints {

    default @NotNull MojangMultiUsernameResponse getMultipleUniqueIds(@NotNull Collection<String> usernames) throws MojangApiException {
        return this.getMultipleUniqueIds(usernames.toArray(new String[] { }));
    }

    @Body("[{usernames}]")
    @Headers("Content-Type: application/json")
    @RequestLine("POST /minecraft/profile/lookup/bulk/byname")
    @NotNull MojangMultiUsernameResponse getMultipleUniqueIds(@NotNull @Param(value = "usernames", expander = StringArrayQuoteExpander.class) String... usernames) throws MojangApiException;

    @RequestLine("GET /minecraft/profile/lookup/name/{username}")
    @NotNull MojangUsernameResponse getPlayer(@NotNull @Param("username") String username) throws MojangApiException;

    @RequestLine("GET /minecraft/profile/lookup/{username}")
    @NotNull MojangUsernameResponse getPlayer(@NotNull @Param("uuid") UUID uniqueId) throws MojangApiException;

    @MojangDomain(MojangClient.Domain.MOJANG_SESSIONSERVER)
    @RequestLine("GET /session/minecraft/profile/{uniqueId}?unsigned=false")
    @NotNull MojangPropertiesResponse getProperties(@NotNull @Param("uniqueId") UUID uniqueId) throws MojangApiException;


}
