package dev.sbs.mojangapi;

import dev.sbs.mojangapi.response.MojangMultiUsername;
import dev.simplified.gson.GsonContributor;
import dev.simplified.gson.GsonSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Registers the Mojang-specific type adapters with {@link GsonSettings#defaults()}.
 * <p>
 * Discovered via the {@link java.util.ServiceLoader} entry at
 * {@code META-INF/services/dev.simplified.gson.GsonContributor} whenever this
 * module is on the classpath; consumers of {@code GsonSettings.defaults()} get
 * the adapters automatically without touching their own bootstrap code.
 */
public final class MojangApiGsonContributor implements GsonContributor {

    @Override
    public void contribute(GsonSettings.@NotNull Builder builder) {
        builder.withTypeAdapter(MojangMultiUsername.class, new MojangMultiUsername.Deserializer());
    }

}
