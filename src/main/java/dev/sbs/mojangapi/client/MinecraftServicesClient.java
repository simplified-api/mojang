package dev.sbs.minecraftapi.client.mojang.client;

import dev.sbs.minecraftapi.client.mojang.request.MinecraftServicesRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Inet6Address;
import java.util.Optional;

public class MinecraftServicesClient extends MojangClient<MinecraftServicesRequest> {

    public MinecraftServicesClient() {
        this(Optional.empty());
    }

    public MinecraftServicesClient(@Nullable Inet6Address inet6Address) {
        this(Optional.ofNullable(inet6Address));
    }

    public MinecraftServicesClient(@NotNull Optional<Inet6Address> inet6Address) {
        super(Domain.MINECRAFT_SERVICES, inet6Address);
    }

}