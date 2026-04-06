package dev.sbs.minecraftapi.client.mojang;

import com.google.gson.Gson;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.client.mojang.exception.MojangApiException;
import dev.sbs.minecraftapi.client.mojang.request.MojangEndpoint;
import dev.simplified.client.Client;
import dev.simplified.client.decoder.ClientErrorDecoder;
import dev.simplified.client.ratelimit.RateLimit;
import dev.simplified.client.route.DynamicRouteProvider;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Inet6Address;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Getter
@NoArgsConstructor
public final class MojangClient extends Client<MojangEndpoint> {

    @Override
    protected @NotNull Gson getGson() {
        return MinecraftApi.getGson();
    }

    public MojangClient(@Nullable Inet6Address inet6Address) {
        this(Optional.ofNullable(inet6Address));
    }

    public MojangClient(@NotNull Optional<Inet6Address> inet6Address) {
        super(inet6Address);
    }

    @Override
    protected @NotNull ClientErrorDecoder configureErrorDecoder() {
        return (methodKey, response) -> {
            throw new MojangApiException(methodKey, response);
        };
    }

    public boolean isRateLimited(@NotNull Domain domain) {
        return super.isRateLimited(domain.getBucketId());
    }
    
    public boolean notRateLimited(@NotNull Domain domain) {
        return !this.isRateLimited(domain);
    }

    @Getter
    @RequiredArgsConstructor
    public enum Domain implements DynamicRouteProvider {

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

        Domain(@NotNull String route) {
            this(route, new RateLimit(600, 10, ChronoUnit.MINUTES));
        }

    }

}