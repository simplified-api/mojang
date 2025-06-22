package dev.sbs.minecraftapi.client.mojang.client;

import dev.sbs.minecraftapi.client.mojang.request.MojangSessionRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Inet6Address;
import java.util.Optional;

public class MojangSessionClient extends MojangClient<MojangSessionRequest> {

    public MojangSessionClient() {
        this(Optional.empty());
    }

    public MojangSessionClient(@Nullable Inet6Address inet6Address) {
        this(Optional.ofNullable(inet6Address));
    }

    public MojangSessionClient(@NotNull Optional<Inet6Address> inet6Address) {
        super(Domain.MOJANG_API, inet6Address);
    }

}