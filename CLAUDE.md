# mojang

Feign contracts and typed responses for Mojang and Minecraft public services, plus a raw-socket
server pinger. Root `api.simplified.mojang.**`. Ships no client - `simplified-dev/client` runs the
contract, which is why routing and rate limiting are declarations here rather than code.

## Build

- Gradle `group` is **`dev.sbs`**, the package root is `api.simplified.mojang`, and the JitPack
  coordinate is `com.github.simplified-api:mojang`. Three spellings, none derived from another.
- Every dependency is `api(...)` with an inline `strictly()` pin. The pins here are **behind** the
  ones in the `hypixel` sibling for the same artifacts; the two modules do not move together, and
  building both from the `Simplified-Api` parent substitutes local sources over each independently.
- `MojangApiGsonContributor` is reached only through
  `META-INF/services/dev.simplified.gson.GsonContributor`. `GsonSettings.defaults()` runs it, and it
  is the only thing that teaches Gson the bulk-lookup array shape.

## Gates

**There are none.** No test sources, no fixtures, so `./gradlew build` proves the module compiles and
nothing else. Nothing catches a wrong `@SerializedName`, a method annotated with the wrong domain, or
a request line Mojang has retired.

Verification is one live call plus a `curl` of the same endpoint compared field by field. Gson drops
an unrecognised key without a word, so "it parsed" is not evidence.

## Routing is resolved once

`MojangRoute` is meta-annotated `@DynamicRoute`, and `RouteDiscovery` resolves it at **proxy
construction**, baking the host and the rate-limit bucket key into each method's metadata. A wrong
annotation is a permanent property of the built proxy, not an intermittent fault.

- The type-level default is `MINECRAFT_SERVICES`; a method-level annotation overrides it.
- A misrouted method that happens to resolve still charges the **wrong bucket**, and the only symptom
  is an unexplained throttle somewhere else later. Check `client.isRateLimited(DOMAIN)` after a
  routing change, not just the response.
- A new host belongs in `MojangDomain` with its own `RateLimit`. The enum exists for the limit, not
  for the string.
- `MOJANG`, `MOJANG_ACCOUNT`, `MOJANG_API`, `MOJANG_AUTH`, `MOJANG_AUTHSERVER`, `MINECRAFT`,
  `MINECRAFT_SESSION` and `MINECRAFT_SKINS` are declared and unrouted. They are not dead - they are
  the addressable surface - but no contract method reaches them today.

### The limits are Mojang's, not ours

`MojangDomain`'s default is 200 requests per 2 minutes and `MOJANG_SESSIONSERVER` is 400 per 10
seconds, both taken from the published API documentation - the per-IP default counted by `/56` subnet
on IPv6, and the one documented per-endpoint exception, the session server's profile lookup.

The CDN hosts publish no limit of their own. They inherit the default deliberately, because a
fabricated number reads as researched and a `RateLimit` constant is a claim about a remote service.
Only the enum constants throttle; a number in prose reaches nothing, so the two are kept in step by
editing both.

## Two paths bypass the framework

Both are deliberate and both are easy to miss, because they sit behind ordinary-looking method calls.

| Path | Transport | What it gives up |
|---|---|---|
| `MinecraftServerPing.pingServer` | raw `Socket` | pooling, rate limiting, the error decoder; takes its own timeout |
| `MojangProfile.Textures(MojangProperties)` | `ImageIO.read(new URL(...))` | the same, per texture |

`getMojangProfile(uuid)` is therefore **up to three network round trips**, two of which the `Client`
knows nothing about: a properties fetch it can see, then a skin download and a cape download it
cannot. `getMojangProfile(username)` adds a fourth in front. Reach for `getProperties` when the URLs
are enough - the constructor's cost is invisible at the call site.

`MojangProperty` and `MinecraftServerPing` each hold a private static
`GsonSettings.defaults().create()`, so a caller that customised the client's Gson does not affect
either.

## Streams are the caller's

`downloadClientJar` and `downloadTexture` return a live `InputStream`. The framework sets
`doNotCloseAfterDecode()` precisely so they can, which means an unclosed stream holds its connection
out of the pool until the socket times out. `downloadAsset` returns `byte[]` because an asset object
is small; do not convert one form to the other without the size argument that justifies it.

## Shapes that mislead

- **Bulk lookup returns a bare JSON array**, not an object, which is why `MojangMultiUsername` needs a
  hand-written `JsonDeserializer` rather than a field. The body is built from `@Body("[{usernames}]")`
  plus `StringArrayQuoteExpander`, which is what quotes each element - a plain expander produces
  `[a,b]` and a 400.
- **A name that does not exist is dropped from the bulk response, silently.** Ten in, seven out is a
  success. `getProfile(name)` returning empty is the only signal, and the single-name endpoint answers
  404 for the same condition - two different shapes for one fact.
- **`MojangProperty.Value.capeModel` reads `textures.SKIN.metadata.model`.** The field name says cape
  and the path says skin; the path is right, because the slim/classic distinction is a skin property.
  `isSlim()` reads it. Renaming the field is safe, trusting the name is not.
- **`isDefaultSlim()` tests the low bit: `(uniqueId.hashCode() & 1) == 1`.** It read
  `hashCode() % 2 == 1` for a long time, which is the same thing only for non-negative hashes - Java's
  `%` keeps the sign of the dividend, so every negative hash answered `-1` and roughly half of all
  uuids could never take the slim default. Do not restore the remainder form; it is not a style
  preference.
- **`Textures.Value` carries two ~2 KB base64 default skins as `static final` constants**, and
  `isDefaultSteve()` / `isDefaultAlex()` compare the *encoded* bytes. That comparison is a PNG
  round trip through `ImageIO`, so it is sensitive to the encoder, not just to the image.
- **The asset CDN path is `/{hash[0:2]}/{hash}`.** The `downloadAsset(PistonAssets.Entry)` overload is
  the only place that rule is written down; a direct `downloadAsset(prefix, hash)` call site that
  computes the prefix itself is a second copy of it.
- **`MinecraftPing.Description` is one `String` for two wire shapes.** A server answers `description`
  as either a plain string or a chat-component object with `extra`; the pinger folds the component
  tree to its legacy `§` form and writes it into `description.text` before binding. Both branches are
  in `pingServer` and neither is covered by anything.
- `MojangErrorResponse` maps `error` onto `id` and `errorMessage` onto `reason`. `MojangApiException`
  is built by the framework decoder, so its `(Gson, ErrorContext)` constructor is fixed by the
  `withErrorDecoder` method-reference shape and the five-constructor pattern does not apply.

## Piston chain

```
version_manifest_v2.json          -> PistonManifest
  -> /v1/packages/{sha1}/{ver}    -> PistonMetadata   (downloads, assetIndex, javaVersion, mainClass)
    -> /v1/packages/{sha1}/{id}   -> PistonAssets     (path -> hash + size)
      -> /{hash[0:2]}/{hash}      -> byte[]
    -> /v1/objects/{sha1}/client.jar -> InputStream
```

Every step's argument comes out of the previous step's entry object, and each has a `default`
overload taking that entry. Prefer the overload - it is where the derivation rules live.

`getVersionMetadata` and `getAssetIndex` share the request line `/v1/packages/{sha1}/{x}.json` and
differ only in return type; they are two methods because the two documents are different shapes at
the same path template, not because the endpoints differ.

## Who consumes this

`Minecraft-Library/asset-renderer` drives the whole Piston chain through `ClientAcquisition` to
download and cache a client JAR, and `PlayerRenderer` reads skins through the profile surface. A
change to a Piston DTO or to `downloadClientJar` reaches that pipeline before it reaches anything
else.

## Skip these

- `build/`, `.gradle/` - Gradle output and daemon state.
- `.schema/` - excluded from the IDE module by `build.gradle.kts`.

## Decisions that stay closed

- Do not put a host literal in a request line. It belongs in `MojangDomain`, with the rate limit that
  is the reason the enum exists.
- Do not guess a `RateLimit`. A fabricated number is worse than inheriting the default, because it
  reads as researched.
- Do not close the response inside the contract for a streaming return. The caller owns it, the
  framework is configured for that, and closing early breaks a body larger than memory.
- Do not fold `MojangProfile` construction into `getProperties`. It performs uninstrumented network
  I/O, and the separation is what lets a caller avoid it.
- Do not route `MinecraftServerPing` through the `Client`. It speaks the Minecraft protocol over TCP;
  there is no HTTP exchange to intercept.
