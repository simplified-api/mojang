package dev.sbs.minecraftapi.client.mojang.request;

import dev.sbs.api.client.request.Endpoint;
import dev.sbs.api.client.request.expander.StringArrayQuoteExpander;
import dev.sbs.minecraftapi.client.mojang.MojangClient;
import dev.sbs.minecraftapi.client.mojang.exception.MojangApiException;
import dev.sbs.minecraftapi.client.mojang.response.MojangMultiUsername;
import dev.sbs.minecraftapi.client.mojang.response.MojangProperties;
import dev.sbs.minecraftapi.client.mojang.response.MojangUsername;
import dev.sbs.minecraftapi.client.mojang.response.PistonManifest;
import dev.sbs.minecraftapi.client.mojang.response.PistonMetadata;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

@MojangDomain(MojangClient.Domain.MINECRAFT_SERVICES)
public interface MojangEndpoint extends Endpoint {

    /**
     * Requests player information by username in bulk.
     *
     * @param usernames the case-insensitive names of the players
     * @return the bulk username lookup response
     * @throws MojangApiException when the Mojang API returns an HTTP status of 400 or higher
     * @apiNote the maximum number of usernames per request is 10
     */
    default @NotNull MojangMultiUsername getMultipleUniqueIds(@NotNull Collection<String> usernames) throws MojangApiException {
        return this.getMultipleUniqueIds(usernames.toArray(new String[] { }));
    }

    /**
     * Requests player information by username in bulk.
     *
     * @param usernames the case-insensitive names of the players
     * @return the bulk username lookup response
     * @throws MojangApiException when the Mojang API returns an HTTP status of 400 or higher
     * @apiNote the maximum number of usernames per request is 10
     */
    @Body("[{usernames}]")
    @Headers("Content-Type: application/json")
    @RequestLine("POST /minecraft/profile/lookup/bulk/byname")
    @NotNull MojangMultiUsername getMultipleUniqueIds(@NotNull @Param(value = "usernames", expander = StringArrayQuoteExpander.class) String... usernames) throws MojangApiException;

    /**
     * Requests player information by username.
     *
     * @param username the case-insensitive name of the player
     * @return the username lookup response
     * @throws MojangApiException when the Mojang API returns an HTTP status of 400 or higher
     */
    @RequestLine("GET /minecraft/profile/lookup/name/{username}")
    @NotNull MojangUsername getPlayer(@NotNull @Param("username") String username) throws MojangApiException;

    /**
     * Retrieves player information by unique id.
     *
     * @param uniqueId the unique id of the player
     * @return the username lookup response
     * @throws MojangApiException when the Mojang API returns an HTTP status of 400 or higher
     */
    @RequestLine("GET /minecraft/profile/lookup/{username}")
    @NotNull MojangUsername getPlayer(@NotNull @Param("uuid") UUID uniqueId) throws MojangApiException;

    /**
     * Requests player properties by unique id.
     *
     * @param uniqueId the unique id of the player
     * @return the player properties response
     * @throws MojangApiException when the Mojang API returns an HTTP status of 400 or higher
     */
    @MojangDomain(MojangClient.Domain.MOJANG_SESSIONSERVER)
    @RequestLine("GET /session/minecraft/profile/{uniqueId}?unsigned=false")
    @NotNull MojangProperties getProperties(@NotNull @Param("uniqueId") UUID uniqueId) throws MojangApiException;

    /**
     * Fetches the full Minecraft version manifest from Piston Meta.
     *
     * @return the version manifest containing all known versions
     * @throws MojangApiException when the Piston Meta API returns an HTTP status of 400 or higher
     */
    @MojangDomain(MojangClient.Domain.PISTON_META)
    @RequestLine("GET /mc/game/version_manifest_v2.json")
    @NotNull PistonManifest getVersionManifest() throws MojangApiException;

    /**
     * Fetches version metadata for the given manifest entry.
     *
     * @param entry the version manifest entry
     * @return the version metadata containing download URLs and asset index
     * @throws MojangApiException when the Piston Meta API returns an HTTP status of 400 or higher
     */
    default @NotNull PistonMetadata getVersionMetadata(@NotNull PistonManifest.Entry entry) throws MojangApiException {
        return this.getVersionMetadata(entry.getSha1(), entry.getVersion());
    }

    /**
     * Fetches version metadata by SHA-1 hash and version identifier.
     *
     * @param sha1 the SHA-1 hash of the version metadata JSON
     * @param version the version identifier (e.g. {@code "1.21.10"})
     * @return the version metadata containing download URLs and asset index
     * @throws MojangApiException when the Piston Meta API returns an HTTP status of 400 or higher
     */
    @MojangDomain(MojangClient.Domain.PISTON_META)
    @RequestLine("GET /v1/packages/{sha1}/{version}.json")
    @NotNull PistonMetadata getVersionMetadata(@NotNull @Param("sha1") String sha1, @NotNull @Param("version") String version) throws MojangApiException;

}
