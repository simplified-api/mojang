package dev.sbs.minecraftapi.client.mojang.request;

import dev.sbs.minecraftapi.client.mojang.exception.MojangApiException;
import dev.sbs.minecraftapi.client.mojang.response.MojangMultiUsername;
import dev.sbs.minecraftapi.client.mojang.response.MojangProfile;
import dev.sbs.minecraftapi.client.mojang.response.MojangProperties;
import dev.sbs.minecraftapi.client.mojang.response.MojangUsername;
import dev.sbs.minecraftapi.client.mojang.response.PistonAssets;
import dev.sbs.minecraftapi.client.mojang.response.PistonManifest;
import dev.sbs.minecraftapi.client.mojang.response.PistonMetadata;
import dev.simplified.client.request.Contract;
import dev.simplified.client.request.expander.StringArrayQuoteExpander;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;

/**
 * Feign contract interface for Mojang and Minecraft services.
 * <p>
 * Methods are dynamically routed to different Mojang domains via {@link MojangRoute} annotations.
 * The default domain is {@link MojangDomain#MINECRAFT_SERVICES MINECRAFT_SERVICES}; individual
 * methods override this to target the session server, Piston Meta, Piston Data CDN, or resource
 * CDN as appropriate.
 * <p>
 * Most endpoints do not require authentication. The global rate limit is 200 requests per
 * 2 minutes per IP address; for IPv6, limits are bucketed by /56 subnet. The session server has
 * a separate limit of approximately 400 requests per 10 seconds.
 *
 * @see MojangDomain
 * @see MojangRoute
 * @see <a href="https://minecraft.wiki/w/Mojang_API">Mojang API</a>
 */
@MojangRoute(MojangDomain.MINECRAFT_SERVICES)
public interface MojangContract extends Contract {

    // ---- Profile lookup ----

    /**
     * Fetches multiple player profiles by username in bulk.
     * <p>
     * Accepts up to 10 case-insensitive usernames per request. Players that
     * do not exist are silently omitted from the response.
     *
     * @param usernames the player usernames to look up
     * @return the bulk username lookup response containing matched profiles
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     * @see #getMultipleUniqueIds(String...)
     */
    default @NotNull MojangMultiUsername getMultipleUniqueIds(@NotNull Collection<String> usernames) throws MojangApiException {
        return this.getMultipleUniqueIds(usernames.toArray(new String[] { }));
    }

    /**
     * Fetches multiple player profiles by username in bulk.
     * <p>
     * Accepts up to 10 case-insensitive usernames per request. Players that
     * do not exist are silently omitted from the response.
     *
     * @param usernames the player usernames to look up
     * @return the bulk username lookup response containing matched profiles
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    @Body("[{usernames}]")
    @Headers("Content-Type: application/json")
    @RequestLine("POST /minecraft/profile/lookup/bulk/byname")
    @NotNull MojangMultiUsername getMultipleUniqueIds(@NotNull @Param(value = "usernames", expander = StringArrayQuoteExpander.class) String... usernames) throws MojangApiException;

    /**
     * Fetches a player's unique id and case-corrected username by their
     * case-insensitive username.
     *
     * @param username the case-insensitive player username
     * @return the profile containing the player's unique id and username
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    @RequestLine("GET /minecraft/profile/lookup/name/{username}")
    @NotNull MojangUsername getPlayer(@NotNull @Param("username") String username) throws MojangApiException;

    /**
     * Fetches a player's username and profile status by unique id.
     *
     * @param uniqueId the player's unique id
     * @return the profile containing the player's unique id and username
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    @RequestLine("GET /minecraft/profile/lookup/{uniqueId}")
    @NotNull MojangUsername getPlayer(@NotNull @Param("uniqueId") UUID uniqueId) throws MojangApiException;

    /**
     * Fetches a player's profile properties, including Base64-encoded skin
     * and cape texture data with cryptographic signatures.
     *
     * @param uniqueId the player's unique id
     * @return the profile properties including signed texture data
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    @MojangRoute(MojangDomain.MOJANG_SESSIONSERVER)
    @RequestLine("GET /session/minecraft/profile/{uniqueId}?unsigned=false")
    @NotNull MojangProperties getProperties(@NotNull @Param("uniqueId") UUID uniqueId) throws MojangApiException;

    /**
     * Fetches a complete {@link MojangProfile} for the given username by chaining a username
     * lookup with a properties fetch.
     *
     * @param username the case-insensitive player username
     * @return the full Mojang profile, including signed texture data
     * @throws MojangApiException if either underlying call fails
     */
    default @NotNull MojangProfile getMojangProfile(@NotNull String username) throws MojangApiException {
        return this.getMojangProfile(this.getPlayer(username).getUniqueId());
    }

    /**
     * Fetches a complete {@link MojangProfile} for the given unique id, wrapping the resulting
     * {@link MojangProperties} in a {@link MojangProfile}.
     *
     * @param uniqueId the player's unique id
     * @return the full Mojang profile, including signed texture data
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    default @NotNull MojangProfile getMojangProfile(@NotNull UUID uniqueId) throws MojangApiException {
        return new MojangProfile(this.getProperties(uniqueId));
    }

    // ---- Piston Meta ----

    /**
     * Fetches the full Minecraft version manifest from Piston Meta, containing
     * every known release and snapshot with download URLs for their metadata.
     *
     * @return the version manifest
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    @MojangRoute(MojangDomain.PISTON_META)
    @RequestLine("GET /mc/game/version_manifest_v2.json")
    @NotNull PistonManifest getVersionManifest() throws MojangApiException;

    /**
     * Fetches version metadata for the given manifest entry.
     *
     * @param entry the version manifest entry
     * @return the version metadata containing download URLs and asset index
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     * @see #getVersionMetadata(String, String)
     */
    default @NotNull PistonMetadata getVersionMetadata(@NotNull PistonManifest.Entry entry) throws MojangApiException {
        return this.getVersionMetadata(entry.getSha1(), entry.getVersion());
    }

    /**
     * Fetches version metadata by SHA-1 hash and version identifier, containing
     * client/server JAR download URLs, asset index reference, and Java version
     * requirements.
     *
     * @param sha1 the SHA-1 hash of the version metadata JSON
     * @param version the version identifier (e.g. {@code "1.21.10"})
     * @return the version metadata
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    @MojangRoute(MojangDomain.PISTON_META)
    @RequestLine("GET /v1/packages/{sha1}/{version}.json")
    @NotNull PistonMetadata getVersionMetadata(@NotNull @Param("sha1") String sha1, @NotNull @Param("version") String version) throws MojangApiException;

    /**
     * Fetches an asset index by SHA-1 hash and index identifier, containing
     * the mapping of every asset path to its download hash and size.
     *
     * @param sha1 the SHA-1 hash of the asset index JSON
     * @param id the asset index identifier (e.g. {@code "27"})
     * @return the asset index
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    @MojangRoute(MojangDomain.PISTON_META)
    @RequestLine("GET /v1/packages/{sha1}/{id}.json")
    @NotNull PistonAssets getAssetIndex(@NotNull @Param("sha1") String sha1, @NotNull @Param("id") String id) throws MojangApiException;

    /**
     * Fetches the asset index for the given asset index metadata.
     *
     * @param assetIndex the asset index metadata from {@link PistonMetadata#getAssetIndex()}
     * @return the asset index
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     * @see #getAssetIndex(String, String)
     */
    default @NotNull PistonAssets getAssetIndex(@NotNull PistonMetadata.AssetIndex assetIndex) throws MojangApiException {
        return this.getAssetIndex(assetIndex.getSha1(), assetIndex.getId());
    }

    // ---- Binary downloads ----

    /**
     * Downloads the Minecraft client JAR as a streaming {@link InputStream}
     * from the given SHA-1 hash.
     * <p>
     * The caller owns the returned stream's lifecycle and <b>must</b> close it
     * (e.g. via try-with-resources) to release the underlying HTTP connection
     * back to the pool.
     *
     * @param sha1 the SHA-1 hash identifying the client JAR
     * @return a streaming input stream of the client JAR bytes
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    @MojangRoute(MojangDomain.PISTON_DATA)
    @RequestLine("GET /v1/objects/{sha1}/client.jar")
    @NotNull InputStream downloadClientJar(@NotNull @Param("sha1") String sha1) throws MojangApiException;

    /**
     * Downloads the Minecraft client JAR as a streaming {@link InputStream} from the given download entry.
     * <p>
     * The caller owns the returned stream's lifecycle and <b>must</b> close it
     * (e.g. via try-with-resources) to release the underlying HTTP connection
     * back to the pool.
     *
     * @param entry the download entry from {@link PistonMetadata.Downloads#getClient()}
     * @return a streaming input stream of the client JAR bytes
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     * @see #downloadClientJar(String)
     */
    default @NotNull InputStream downloadClientJar(@NotNull PistonMetadata.Downloads.Entry entry) throws MojangApiException {
        return this.downloadClientJar(entry.getSha1());
    }

    /**
     * Downloads an individual Minecraft asset as a byte array from the
     * resource CDN ({@code resources.download.minecraft.net}).
     *
     * @param prefix the first two characters of the asset hash
     * @param hash the full SHA-1 hash of the asset
     * @return the asset contents
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     */
    @MojangRoute(MojangDomain.MINECRAFT_RESOURCES)
    @RequestLine("GET /{prefix}/{hash}")
    byte @NotNull [] downloadAsset(@NotNull @Param("prefix") String prefix, @NotNull @Param("hash") String hash) throws MojangApiException;

    /**
     * Downloads an individual Minecraft asset for the given asset entry.
     *
     * @param entry the asset entry from {@link PistonAssets#getObjects()}
     * @return the asset contents
     * @throws MojangApiException if the server responds with an HTTP status of 400 or higher
     * @see #downloadAsset(String, String)
     */
    default byte @NotNull [] downloadAsset(@NotNull PistonAssets.Entry entry) throws MojangApiException {
        return this.downloadAsset(entry.getHash().substring(0, 2), entry.getHash());
    }

}
