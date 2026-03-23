package dev.sbs.minecraftapi.client.mojang.response;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Version metadata from Mojang's Piston Meta API, containing download URLs,
 * asset index information, and Java version requirements for a single
 * Minecraft version.
 *
 * @see <a href="https://piston-meta.mojang.com/v1/packages/4af6fef7c282333bd87f44b67a83d0e71c859d62/1.21.10.json">Example - 1.21.10</a>
 * @see PistonManifest
 */
@Getter
public final class PistonMetadata {

    /** The version identifier (e.g. {@code "1.21.10"}). */
    @SerializedName("id")
    private @NotNull String id;

    /** The version type classification. */
    @SerializedName("type")
    private @NotNull Type type = Type.SNAPSHOT;

    /** The download entries for client and server artifacts. */
    @SerializedName("downloads")
    private @NotNull Downloads downloads = new Downloads();

    /** The asset index reference for resolving game assets. */
    @SerializedName("assetIndex")
    private @NotNull AssetIndex assetIndex = new AssetIndex();

    /** The asset version identifier (e.g. {@code "21"}). */
    @SerializedName("assets")
    private @NotNull String assets;

    /** The Java runtime version requirements. */
    @SerializedName("javaVersion")
    private @NotNull JavaVersion javaVersion = new JavaVersion();

    /** The fully qualified main class name. */
    @SerializedName("mainClass")
    private @NotNull String mainClass;

    /** The ISO 8601 timestamp when this version was originally released. */
    @SerializedName("releaseTime")
    private @NotNull String releaseTime;

    /**
     * Download entries for client and server artifacts.
     */
    @Getter
    public static final class Downloads {

        /** The client JAR download entry. */
        @SerializedName("client")
        private @NotNull Entry client = new Entry();

        /** The client obfuscation mappings download entry. */
        @SerializedName("client_mappings")
        private @NotNull Entry clientMappings = new Entry();

        /** The server JAR download entry. */
        @SerializedName("server")
        private @NotNull Entry server = new Entry();

        /** The server obfuscation mappings download entry. */
        @SerializedName("server_mappings")
        private @NotNull Entry serverMappings = new Entry();

        /**
         * A single downloadable artifact with integrity metadata.
         */
        @Getter
        public static final class Entry {

            /** The SHA-1 hash of the artifact. */
            @SerializedName("sha1")
            private @NotNull String sha1;

            /** The size of the artifact in bytes. */
            @SerializedName("size")
            private long size;

            /** The download URL for the artifact. */
            @SerializedName("url")
            private @NotNull String url;

        }

    }

    /**
     * Asset index reference for resolving game assets.
     */
    @Getter
    public static final class AssetIndex {

        /** The asset index identifier (e.g. {@code "21"}). */
        @SerializedName("id")
        private @NotNull String id;

        /** The SHA-1 hash of the asset index JSON. */
        @SerializedName("sha1")
        private @NotNull String sha1;

        /** The size of the asset index JSON in bytes. */
        @SerializedName("size")
        private long size;

        /** The total size of all assets referenced by this index in bytes. */
        @SerializedName("totalSize")
        private long totalSize;

        /** The download URL for the asset index JSON. */
        @SerializedName("url")
        private @NotNull String url;

    }

    /**
     * Java runtime version requirements.
     */
    @Getter
    public static final class JavaVersion {

        /** The Java runtime component name (e.g. {@code "java-runtime-delta"}). */
        @SerializedName("component")
        private @NotNull String component;

        /** The required Java major version (e.g. {@code 21}). */
        @SerializedName("majorVersion")
        private int majorVersion;

    }

    /**
     * The version type classification for a Minecraft release.
     */
    public enum Type {

        /** A stable, full release. */
        @SerializedName("release")
        RELEASE,

        /** A development snapshot or pre-release. */
        @SerializedName("snapshot")
        SNAPSHOT

    }

}
