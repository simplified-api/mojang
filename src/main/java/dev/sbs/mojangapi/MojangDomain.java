package dev.sbs.minecraftapi.client.mojang;

import dev.sbs.minecraftapi.client.mojang.request.MojangRoute;
import dev.simplified.client.ratelimit.RateLimit;
import dev.simplified.client.route.DynamicRouteProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

/**
 * Enumerates every Mojang/Minecraft domain that the Mojang HTTP contract routes to.
 * <p>
 * Each constant supplies its host (and optional base path) along with its rate-limit policy
 * for client-side throttling. Domains are addressed at runtime through the {@link MojangRoute}
 * annotation, which {@link dev.simplified.client.route.RouteDiscovery RouteDiscovery} resolves
 * via the {@link DynamicRouteProvider} contract.
 * <p>
 * The default global rate limit is 600 requests per 10 minutes per IP address; the session
 * server is bucketed separately at 200 requests per minute. Other domains inherit the default.
 *
 * @see MojangRoute
 * @see DynamicRouteProvider
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
        new RateLimit(200, 1, ChronoUnit.MINUTES)
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
        this(route, new RateLimit(600, 10, ChronoUnit.MINUTES));
    }

}
