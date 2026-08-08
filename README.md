# Mojang API

Feign contracts and typed responses for Mojang and Minecraft services: profile and username lookup, signed skin and cape textures, the Piston version manifest, and the client-JAR and asset CDNs. Ships a raw-socket Minecraft server pinger alongside them.

> [!IMPORTANT]
> One contract spans **fourteen hosts**. Each method carries a `@MojangRoute` naming the domain it targets, and each domain owns its own rate-limit bucket. Mojang buckets IPv6 callers by `/56` subnet, so a client bound to a rotating source address is the supported way to scale past one bucket.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Usage](#usage)
- [Domains and Routing](#domains-and-routing)
- [Endpoints](#endpoints)
- [Profiles and Textures](#profiles-and-textures)
- [Piston: Manifest to Assets](#piston-manifest-to-assets)
- [Server Ping](#server-ping)
- [Error Handling](#error-handling)
- [Gradle Tasks](#gradle-tasks)
  - [Build and Test](#build-and-test)
- [Package Structure](#package-structure)
- [Contributing](#contributing)
- [License](#license)

## Features

- **One contract, fourteen domains** - `@MojangRoute` selects the host per method, and the framework resolves it at proxy-construction time through the `DynamicRouteProvider` contract
- **Per-domain rate limits** - each `MojangDomain` constant carries its own `RateLimit`, so the session server throttles independently of everything else
- **Bulk username lookup** - up to 10 names in one `POST`, with the JSON array body built by a parameter expander rather than a hand-written encoder
- **Signed textures** - `MojangProperties` carries the base64 texture blob and its signature; `MojangProperty.Value` decodes it and reaches the skin and cape URLs by path
- **Streaming downloads** - the client JAR and skin textures come back as `InputStream` so a multi-hundred-megabyte body never lands in the heap
- **The whole Piston chain** - version manifest, per-version metadata, asset index, and individual asset objects, each step reachable from the previous one's entry object
- **Raw server ping** - `MinecraftServerPing` speaks the Minecraft handshake protocol over a TCP socket and normalises both MOTD encodings into one shape

## Getting Started

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| [JDK](https://adoptium.net/) | **21+** | Required |
| [Gradle](https://gradle.org/) | 8.x | Wrapper is bundled (`./gradlew`) |
| [Git](https://git-scm.com/) | 2.x+ | For cloning the repository |

No credentials. Every endpoint declared here is public.

### Installation

Add the JitPack repository and the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.simplified-api:mojang:master-SNAPSHOT")
}
```

`client`, `gson-extras`, `collections`, `utils`, `reflection` and `minecraft-library/text` come in transitively as `api` dependencies, so declaring `mojang` is normally enough.

Or clone and build locally:

```bash
git clone https://github.com/simplified-api/mojang.git
cd mojang
./gradlew build
```

### Usage

```java
Client<MojangContract> client = Client.create(
    ClientConfig.builder(MojangContract.class, GsonSettings.defaults())
        .withErrorDecoder(MojangApiException::new)
        .build()
);

MojangContract mojang = client.getContract();

// Name to uuid, and back.
MojangUsername player = mojang.getPlayer("CraftedFury");
MojangUsername again  = mojang.getPlayer(player.getUniqueId());

// Up to ten names in one request. Names that do not exist are omitted, not nulled.
MojangMultiUsername bulk = mojang.getMultipleUniqueIds("Notch", "jeb_", "Dinnerbone");
bulk.getUniqueId("jeb_").ifPresent(System.out::println);

// Signed texture properties, and the decoded skin URL inside them.
MojangProperties properties = mojang.getProperties(player.getUniqueId());
properties.getProperty().getValue().getSkinUrl().ifPresent(System.out::println);
```

Rate-limit state is per domain, so ask about the one you are going to hit:

```java
if (!client.isRateLimited(MojangDomain.MOJANG_SESSIONSERVER))
    mojang.getProperties(uniqueId);
```

> [!TIP]
> Because the session server has its own bucket, wrap the contract in a `Proxy` with `withAvailability(c -> !c.isRateLimited(MojangDomain.MINECRAFT_SERVICES))` and an IPv6 prefix when you need throughput. Mojang counts IPv6 per `/56`, which is what makes source-address rotation worth the setup.

## Domains and Routing

`MojangRoute` is meta-annotated `@DynamicRoute`, so the framework resolves it during proxy construction and bakes the host and the rate-limit bucket key into each method's metadata. A type-level `@MojangRoute` sets the default; a method-level one overrides it.

| Domain | Host | Rate limit |
|--------|------|------------|
| `MINECRAFT_SERVICES` *(default)* | `api.minecraftservices.com` | 200 / 2 min |
| `MOJANG_SESSIONSERVER` | `sessionserver.mojang.com` | **400 / 10 sec** |
| `PISTON_META` | `piston-meta.mojang.com` | 200 / 2 min |
| `PISTON_DATA` | `piston-data.mojang.com` | 200 / 2 min |
| `MINECRAFT_RESOURCES` | `resources.download.minecraft.net` | 200 / 2 min |
| `MINECRAFT_TEXTURES` | `textures.minecraft.net` | 200 / 2 min |
| `MOJANG` / `MOJANG_ACCOUNT` / `MOJANG_API` / `MOJANG_AUTH` / `MOJANG_AUTHSERVER` | `*.mojang.com` | 200 / 2 min |
| `MINECRAFT` / `MINECRAFT_SESSION` / `MINECRAFT_SKINS` | `*.minecraft.net` | 200 / 2 min |

The last two rows are declared for completeness - no method on the contract currently routes to them.

200 requests per 2 minutes is Mojang's documented per-IP default, counted per `/56` subnet on IPv6. The session server's profile endpoint is the one documented exception. The CDN hosts carry no published limit of their own, so they inherit the default rather than claiming one.

## Endpoints

| Method | Domain | Returns |
|--------|--------|---------|
| `getPlayer(String)` | services | `MojangUsername` |
| `getPlayer(UUID)` | services | `MojangUsername` |
| `getMultipleUniqueIds(String...)` | services | `MojangMultiUsername` |
| `getProperties(UUID)` | sessionserver | `MojangProperties` |
| `getMojangProfile(String \| UUID)` | services + sessionserver | `MojangProfile` |
| `getVersionManifest()` | piston-meta | `PistonManifest` |
| `getVersionMetadata(sha1, version)` | piston-meta | `PistonMetadata` |
| `getAssetIndex(sha1, id)` | piston-meta | `PistonAssets` |
| `downloadClientJar(sha1)` | piston-data | `InputStream` |
| `downloadTexture(hash)` | textures | `InputStream` |
| `downloadAsset(prefix, hash)` | resources | `byte[]` |

Each of the last five also has an overload taking the entry object from the previous step, so the chain reads without manually pulling hashes out.

> [!WARNING]
> `downloadClientJar` and `downloadTexture` return a live `InputStream`. **The caller owns it and must close it** - the framework deliberately does not close the response after decoding, and an unclosed stream holds its connection out of the pool until the socket times out.

```java
try (InputStream jar = mojang.downloadClientJar(metadata.getDownloads().getClient())) {
    Files.copy(jar, target, StandardCopyOption.REPLACE_EXISTING);
}
```

## Profiles and Textures

The session server returns textures as a base64 blob plus a signature, not as fields. `MojangProperty` holds the blob; `getValue()` decodes it into a typed object whose skin, cape and model are reached by path.

```java
MojangProperty.Value value = properties.getProperty().getValue();

value.getSkinUrl();     // textures.SKIN.url
value.getCapeUrl();     // textures.CAPE.url
value.isSlim();         // textures.SKIN.metadata.model == "slim", else derived from the uuid
```

`MojangProfile` is the convenience wrapper over that: one object carrying the uuid, username, timestamp, profile actions and a `Textures` block. `Textures.Value` also ships the two vanilla default skins as base64 constants, with `isDefaultSteve()` / `isDefaultAlex()` to recognise them.

> [!CAUTION]
> Constructing `MojangProfile` from `MojangProperties` **downloads the skin and cape PNGs**. `Textures(MojangProperties)` calls `ImageIO.read(new URL(...))` per texture and base64-encodes the result, over a plain JDK connection - outside the client's pool, rate limiting, timeouts and error decoding. `getMojangProfile(uuid)` therefore performs up to three network round trips and `getMojangProfile(username)` up to four, two of which the framework knows nothing about either way. Prefer `getProperties(...)` when you only need the URLs.

## Piston: Manifest to Assets

Every Minecraft artifact is reachable from the version manifest by following one hash at a time.

```java
PistonManifest manifest = mojang.getVersionManifest();

PistonManifest.Entry entry = manifest.getVersions()
    .stream()
    .filter(version -> version.getVersion().equals(manifest.getLatest().getRelease()))
    .findFirst()
    .orElseThrow();

PistonMetadata metadata = mojang.getVersionMetadata(entry);
PistonAssets   assets   = mojang.getAssetIndex(metadata.getAssetIndex());

byte[] icon = mojang.downloadAsset(assets.getObjects().get("icons/icon_32x32.png"));
```

```
version_manifest_v2.json          -> PistonManifest    (every release and snapshot)
  -> /v1/packages/{sha1}/{ver}     -> PistonMetadata    (downloads, assetIndex, javaVersion, mainClass)
    -> /v1/packages/{sha1}/{id}    -> PistonAssets      (path -> hash + size)
      -> /{hash[0:2]}/{hash}       -> byte[]            (one asset object)
    -> /v1/objects/{sha1}/client.jar -> InputStream     (the client JAR)
```

The `downloadAsset(PistonAssets.Entry)` overload derives the two-character CDN prefix from the hash itself, which is the only place that layout rule is written down.

## Server Ping

`MinecraftServerPing` is not an HTTP client. It opens a TCP socket, sends a protocol handshake and a status request, and reads the JSON status response plus a round-trip latency measurement.

```java
MinecraftPing ping = new MinecraftServerPing().pingServer("mc.hypixel.net");

ping.getDescription().getStrippedText();   // MOTD with the § colour codes removed
ping.getPlayers().getOnline();
ping.getVersion().getName();
ping.getPing();                            // connect latency in ms
```

Servers answer with one of two MOTD encodings - a plain string, or a chat-component object with an `extra` array. The pinger folds both into `description.text` before binding, converting a component tree to its legacy `§`-coded form, so `MinecraftPing.Description` exposes a single `String` either way.

Defaults are port `25565` and a 2-second connect timeout; both overloads take overrides.

## Error Handling

Any status of 400 or higher surfaces as `MojangApiException`, carrying the full response and the decoded `MojangErrorResponse`.

```java
try {
    mojang.getPlayer(username);
} catch (MojangApiException e) {
    e.getStatus().getCode();
    e.getResponse().getId();       // Mojang's `error` code
    e.getResponse().getReason();   // Mojang's `errorMessage`
    e.getResponse().getPath();     // the endpoint path Mojang echoes back
}
```

A username that does not exist is a `404`, not an empty response. The bulk lookup is the exception: unknown names are silently dropped from the array, so `getProfile(name)` returning empty is the only signal.

## Gradle Tasks

### Build and Test

```bash
./gradlew build       # compile and assemble jar
./gradlew test        # no-op; this module ships no tests
```

## Package Structure

```
mojang/
├── src/
│   └── main/java/api/simplified/mojang/
│       ├── MojangContract.java             # the one contract, routed across fourteen domains
│       ├── MinecraftServerPing.java        # raw TCP status ping, not HTTP
│       ├── MojangApiGsonContributor.java   # SPI hook registering the bulk-lookup deserializer
│       ├── exception/                      # MojangApiException, MojangErrorResponse
│       ├── request/                        # MojangDomain (hosts + rate limits), MojangRoute
│       └── response/
│           ├── MojangUsername.java         # name <-> uuid
│           ├── MojangMultiUsername.java    # bulk lookup + its custom JsonDeserializer
│           ├── MojangProperties.java       # signed property list off the session server
│           ├── MojangProperty.java         # the base64 blob and its decoded Value
│           ├── MojangProfile.java          # convenience wrapper; downloads textures
│           ├── MinecraftPing.java          # server status response
│           └── Piston{Manifest,Metadata,Assets}.java
├── src/main/resources/META-INF/services/   # GsonContributor SPI registration
├── build.gradle.kts  settings.gradle.kts  gradle/libs.versions.toml
└── LICENSE.md  CONTRIBUTING.md  CLAUDE.md
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, code style guidelines, and how to submit a pull request.

## License

This project is licensed under the **Apache License 2.0** - see [LICENSE](LICENSE.md) for the full text.

Minecraft is a trademark of Mojang AB, a Microsoft subsidiary. This library is an independent client for Mojang's public web services and is not affiliated with, endorsed by, or sponsored by Mojang AB or Microsoft. Anything you download through it - client JARs, assets, skins - remains Mojang's copyrighted material, and its use is subject to the [Minecraft EULA](https://www.minecraft.net/en-us/eula) and [Usage Guidelines](https://www.minecraft.net/en-us/usage-guidelines).
