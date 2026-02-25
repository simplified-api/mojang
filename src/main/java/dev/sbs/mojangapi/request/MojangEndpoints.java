package dev.sbs.minecraftapi.client.mojang.request;

import dev.sbs.api.client.request.Endpoints;
import dev.sbs.api.client.request.expander.StringArrayQuoteExpander;
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

@MojangDomain(MojangClient.Domain.MINECRAFT_SERVICES)
public interface MojangEndpoints extends Endpoints {

    /**
     * Requests player information by username in bulk.
     *
     * @param usernames The case-insensitive names of the players.
     * @throws MojangApiException Thrown when the Mojang Api encounters an error in the HTTP
     * status range of 400+.
     * @apiNote The maximum number of usernames per request is 10.
     */
    default @NotNull MojangMultiUsernameResponse getMultipleUniqueIds(@NotNull Collection<String> usernames) throws MojangApiException {
        return this.getMultipleUniqueIds(usernames.toArray(new String[] { }));
    }

    /**
     * Requests player information by username in bulk.
     *
     * @param usernames The case-insensitive names of the players.
     * @throws MojangApiException Thrown when the Mojang Api encounters an error in the HTTP
     * status range of 400+.
     * @apiNote The maximum number of usernames per request is 10.
     */
    @Body("[{usernames}]")
    @Headers("Content-Type: application/json")
    @RequestLine("POST /minecraft/profile/lookup/bulk/byname")
    @NotNull MojangMultiUsernameResponse getMultipleUniqueIds(@NotNull @Param(value = "usernames", expander = StringArrayQuoteExpander.class) String... usernames) throws MojangApiException;

    /**
     * Requests player information by username.
     *
     * @param username The case-insensitive name of the player.
     * @throws MojangApiException Thrown when the Mojang Api encounters an error in the HTTP
     * status range of 400+.
     */
    @RequestLine("GET /minecraft/profile/lookup/name/{username}")
    @NotNull MojangUsernameResponse getPlayer(@NotNull @Param("username") String username) throws MojangApiException;

    /**
     * Retrieves player information by unique id.
     *
     * @param uniqueId The unique id of the player.
     * @throws MojangApiException Thrown when the Mojang Api encounters an error in the HTTP
     * status range of 400+.
     */
    @RequestLine("GET /minecraft/profile/lookup/{username}")
    @NotNull MojangUsernameResponse getPlayer(@NotNull @Param("uuid") UUID uniqueId) throws MojangApiException;

    /**
     * Requests player properties by unique id.
     *
     * @param uniqueId The unique id of the player.
     * @throws MojangApiException Thrown when the Mojang Api encounters an error in the HTTP
     * status range of 400+.
     */
    @MojangDomain(MojangClient.Domain.MOJANG_SESSIONSERVER)
    @RequestLine("GET /session/minecraft/profile/{uniqueId}?unsigned=false")
    @NotNull MojangPropertiesResponse getProperties(@NotNull @Param("uniqueId") UUID uniqueId) throws MojangApiException;


}
