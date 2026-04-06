package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.annotations.SerializedName;
import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Asset index from Mojang's Piston Meta API, mapping asset paths to their
 * download hashes and sizes. Each asset can be downloaded from
 * {@code https://resources.download.minecraft.net/<first2>/<hash>}.
 *
 * @see <a href="https://piston-meta.mojang.com/v1/packages/feba0de119b966770e2933384ca0a332edb6e034/27.json">Example - 1.21.10</a>
 * @see PistonMetadata.AssetIndex
 */
@Getter
public final class PistonAssets {

    /** The map of asset paths to their download entries. */
    @SerializedName("objects")
    private @NotNull ConcurrentMap<String, Entry> objects = Concurrent.newMap();

    /**
     * A single asset entry with integrity metadata.
     */
    @Getter
    public static final class Entry {

        /** The SHA-1 hash of the asset, also used to construct the download URL. */
        @SerializedName("hash")
        private @NotNull String hash;

        /** The size of the asset in bytes. */
        @SerializedName("size")
        private long size;

    }

}
