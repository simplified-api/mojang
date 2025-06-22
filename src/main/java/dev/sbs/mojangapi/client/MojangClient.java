package dev.sbs.minecraftapi.client.mojang.client;

import dev.sbs.api.client.Client;
import dev.sbs.minecraftapi.client.mojang.exception.MojangApiException;
import dev.sbs.minecraftapi.client.mojang.request.IMojangRequest;
import dev.sbs.api.client.response.CFCacheStatus;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.reflection.Reflection;
import feign.FeignException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Inet6Address;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Getter
public abstract class MojangClient<T extends IMojangRequest> extends Client<T> {

    private final @NotNull Domain domain;
    private final @NotNull T request;

    public MojangClient(@NotNull Domain domain, @Nullable Inet6Address inet6Address) {
        this(domain, Optional.ofNullable(inet6Address));
    }

    public MojangClient(@NotNull Domain domain, @NotNull Optional<Inet6Address> inet6Address) {
        super(domain.getHost().getHost(), inet6Address);
        this.domain = domain;
        this.request = this.build(Reflection.getSuperClass(this));
        super.setCachedResponseHeaders(Concurrent.newUnmodifiableSet(CFCacheStatus.HEADER_KEY));
        super.setErrorDecoder((methodKey, response) -> {
            throw new MojangApiException(FeignException.errorStatus(methodKey, response));
        });
    }

    public boolean isRateLimited() {
        return this.getRecentResponses()
            .stream()
            .filter(response -> response.getTimestamp().toEpochMilli() >= System.currentTimeMillis() - this.getDomain().getRateLimit().getDurationMillis())
            .count() >= this.getDomain().getRateLimit().getAllowed();
    }
    
    public boolean notRateLimited() {
        return !this.isRateLimited();
    }

    @Getter
    public enum Domain {

        MOJANG("mojang.com"),
        MOJANG_ACCOUNT("account.mojang.com"),
        MOJANG_API("api.mojang.com"),
        MOJANG_AUTH("auth.mojang.com"),
        MOJANG_AUTHSERVER("authserver.mojang.com"),
        MOJANG_SESSIONSERVER(
            "sessionserver.mojang.com",
            new RateLimit(200, 10, 1, ChronoUnit.MINUTES)
        ),
        MINECRAFT("minecraft.net"),
        MINECRAFT_SERVICES("api.minecraftservices.com"),
        MINECRAFT_SESSION("session.minecraft.net"),
        MINECRAFT_SKINS("skins.minecraft.net"),
        MINECRAFT_TEXTURES("textures.minecraft.net");

        private final @NotNull URL host;
        private final @NotNull RateLimit rateLimit;

        Domain(@NotNull String host) {
            this(host, new RateLimit(600, 10, 10, ChronoUnit.MINUTES));
        }

        Domain(@NotNull String host, @NotNull RateLimit rateLimit) {
            this.host = getUrl(host);
            this.rateLimit = rateLimit;
        }

        @SneakyThrows
        public static @NotNull URL getUrl(@NotNull String host) {
            if (!host.startsWith("https://") && !host.startsWith("http://"))
                host = String.format("https://%s", host);

            return new URL(host);
        }

    }

    @Getter
    public static class RateLimit {

        private final long maximum;
        private final int bufferPercentage;
        private final long allowed;
        private final long howLong;
        private final @NotNull ChronoUnit unit;
        private final @NotNull Duration duration;
        private long durationMillis;

        public RateLimit(long maximum, int bufferPercentage, long howLong, @NotNull ChronoUnit unit) {
            this.maximum = maximum;
            this.bufferPercentage = bufferPercentage;
            this.allowed = this.maximum - (this.maximum * (this.bufferPercentage / 100));
            this.howLong = howLong;
            this.unit = unit;
            this.duration = Duration.of(this.howLong, this.unit);
            this.durationMillis = this.duration.toMillis();
        }

    }

}