package dev.sbs.minecraftapi.client.mojang.client;

import dev.sbs.minecraftapi.client.mojang.request.MojangApiRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Inet6Address;
import java.util.Optional;

public class MojangApiClient extends MojangClient<MojangApiRequest> {

    public MojangApiClient() {
        this(Optional.empty());
    }

    public MojangApiClient(@Nullable Inet6Address inet6Address) {
        this(Optional.ofNullable(inet6Address));
    }

    public MojangApiClient(@NotNull Optional<Inet6Address> inet6Address) {
        super(Domain.MOJANG_API, inet6Address);
    }

}