package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.annotations.SerializedName;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

/**
 * The full Minecraft version manifest from Mojang's Piston Meta API, containing
 * the latest release and snapshot identifiers and all known version entries.
 *
 * @see <a href="https://piston-meta.mojang.com/mc/game/version_manifest_v2.json">version_manifest_v2.json</a>
 * @see PistonMetadata
 */
@Getter
public final class PistonManifest {

    /** The latest release and snapshot version identifiers. */
    @SerializedName("latest")
    private @NotNull Latest latest = new Latest();

    /** All known version entries, ordered newest first. */
    @SerializedName("versions")
    private @NotNull ConcurrentList<Entry> versions = Concurrent.newList();

    /**
     * The latest release and snapshot version identifiers.
     */
    @Getter
    public static final class Latest {

        /** The latest stable release version identifier (e.g. {@code "1.21.10"}). */
        @SerializedName("release")
        private @NotNull String release;

        /** The latest snapshot version identifier (e.g. {@code "25w24a"}). */
        @SerializedName("snapshot")
        private @NotNull String snapshot;

    }

    /**
     * A single Minecraft version entry from the manifest.
     */
    @Getter
    public static final class Entry {

        /** The version identifier (e.g. {@code "1.21.5"}, {@code "25w14craftmine"}). */
        @SerializedName("id")
        private @NotNull String version;

        /** The version type ({@code "release"} or {@code "snapshot"}). */
        @SerializedName("type")
        private @NotNull String type;

        /** The URL to the {@link PistonMetadata} JSON for this version. */
        @SerializedName("url")
        private @NotNull String url;

        /** The timestamp when this version entry was last updated in {@code ISO-8601} standard. */
        @SerializedName("time")
        private @NotNull OffsetDateTime time;

        /** The timestamp when this version was originally released in {@code ISO-8601} standard. */
        @SerializedName("releaseTime")
        private @NotNull OffsetDateTime releaseTime;

        /** The SHA-1 hash of the version metadata JSON. */
        @SerializedName("sha1")
        private @NotNull String sha1;

        /** The compliance level for this version. */
        @SerializedName("complianceLevel")
        private int complianceLevel;

    }

}
