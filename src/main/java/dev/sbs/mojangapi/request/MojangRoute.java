package dev.sbs.minecraftapi.client.mojang.request;

import dev.simplified.client.route.DynamicRoute;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method- or type-level annotation that selects the Mojang {@link MojangDomain domain} a Feign
 * request is routed to.
 * <p>
 * Resolved at construction time by
 * {@link dev.simplified.client.route.RouteDiscovery RouteDiscovery} via its {@link DynamicRoute}
 * meta-annotation. The {@code value()} method returns a {@link MojangDomain}, which implements
 * {@link dev.simplified.client.route.DynamicRouteProvider DynamicRouteProvider} and supplies the
 * host and rate-limit policy for the matched route.
 *
 * @see MojangDomain
 * @see DynamicRoute
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@DynamicRoute
public @interface MojangRoute {

    @NotNull MojangDomain value();

}
