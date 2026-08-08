package api.simplified.mojang.request;

import dev.simplified.client.ratelimit.RateLimit;
import dev.simplified.client.route.DynamicRouteProvider;
import dev.simplified.client.route.RouteDiscovery;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

/**
 * Enumerates every Mojang/Minecraft domain that the Mojang HTTP contract routes to.
 * <p>
 * Each constant supplies its host (and optional base path) along with its rate-limit policy
 * for client-side throttling. Domains are addressed at runtime through the {@link MojangRoute}
 * annotation, which {@link RouteDiscovery RouteDiscovery} resolves
 * via the {@link DynamicRouteProvider} contract.
 * <p>
 * The default global rate limit is 200 requests per 2 minutes per IP address, bucketed by
 * {@code /56} subnet for IPv6. The session server profile endpoint is bucketed separately at
 * roughly 400 requests per 10 seconds. Other domains inherit the default.
 *
 * @see MojangRoute
 * @see DynamicRouteProvider
 * @see <a href="https://minecraft.wiki/w/Mojang_API">Mojang API</a>
 */
@Getter
@RequiredArgsConstructor
public enum MojangDomain implements DynamicRouteProvider {

    MOJANG("mojang.com"),
    MOJANG_ACCOUNT("account.mojang.com"),
    MOJANG_API("api.mojang.com"),
    MOJANG_AUTH("auth.mojang.com"),
    MOJANG_AUTHSERVER("authserver.mojang.com"),
    MOJANG_SESSIONSERVER(
        "sessionserver.mojang.com",
        new RateLimit(400, 10, ChronoUnit.SECONDS)
    ),
    MINECRAFT("minecraft.net"),
    MINECRAFT_RESOURCES("resources.download.minecraft.net"),
    MINECRAFT_SERVICES("api.minecraftservices.com"),
    MINECRAFT_SESSION("session.minecraft.net"),
    MINECRAFT_SKINS("skins.minecraft.net"),
    MINECRAFT_TEXTURES("textures.minecraft.net"),
    PISTON_DATA("piston-data.mojang.com"),
    PISTON_META("piston-meta.mojang.com");

    private final @NotNull String route;
    private final @NotNull RateLimit rateLimit;

    MojangDomain(@NotNull String route) {
        this(route, new RateLimit(200, 2, ChronoUnit.MINUTES));
    }

}
