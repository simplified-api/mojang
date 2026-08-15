# Contributing to Mojang API

Thank you for your interest in contributing! This document explains how to get started, what to expect during the review process, and the conventions this project follows.

## Table of Contents

- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Development Setup](#development-setup)
  - [IntelliJ IDEA](#intellij-idea)
- [Making Changes](#making-changes)
  - [Branching Strategy](#branching-strategy)
  - [Code Style](#code-style)
  - [Commit Messages](#commit-messages)
  - [Validating Output](#validating-output)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Reporting Issues](#reporting-issues)
- [Project Architecture](#project-architecture)
- [Legal](#legal)

## Getting Started

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| JDK | **21+** | Required |
| Gradle | 8.x | Wrapper is bundled (`./gradlew`) |
| Git | 2.x+ | For cloning and contributing |
| IDE | Any | IntelliJ IDEA is the recommended editor |

No credentials are needed. Every endpoint declared here is public, which is also why there is no way to test a change without spending real rate-limit budget - see [Validating Output](#validating-output).

> [!IMPORTANT]
> This module ships **no tests**. Nothing catches a wrong `@SerializedName`, a wrong domain on a `@MojangRoute`, or a request line Mojang has retired. Every change is verified by calling the live endpoint, and the PR is expected to say which one and what came back.

### Development Setup

1. **Fork and clone the repository**

   [Fork the repository](https://github.com/simplified-api/mojang/fork), then clone your fork:

   ```bash
   git clone https://github.com/<your-username>/mojang.git
   cd mojang
   ```

2. **Verify the JDK toolchain**

   Gradle's Java toolchain feature will download JDK 21 automatically if needed. Confirm with:

   ```bash
   ./gradlew --version
   ```

3. **Run the build**

   ```bash
   ./gradlew build
   ```

4. **Build against local siblings (optional)**

   Every upstream dependency is `strictly()`-pinned to a JitPack SHA in `build.gradle.kts`. To test against unpublished sibling changes - most often `client`, when routing or rate limiting is involved - build from the `Simplified-Api` parent, whose `settings.gradle.kts` substitutes those coordinates for local sources.

### IntelliJ IDEA

1. Open the project root (the directory containing `settings.gradle.kts`). IntelliJ auto-imports the Gradle build.
2. Ensure the **Project SDK** under **File > Project Structure** is set to a JDK 21 installation.
3. Enable **annotation processing** - the annotation processor generates every accessor in `response/`, and the IDE reports phantom errors until the processor runs.
4. The build script excludes `.schema/` from the IDE module.

## Making Changes

### Branching Strategy

- Create a feature branch from `master` for your work.
- Use a descriptive branch name: `fix/session-server-bucket`, `feat/piston-server-mappings`, `docs/route-table`.

```bash
git checkout -b feat/my-feature master
```

### Code Style

The repository uses Simplified Annotations for boilerplate reduction and enforces a consistent Javadoc, exception, and control-flow style.

#### Javadoc

- **Punctuation** - Single hyphens ` - ` only as separators. Never em dashes, `&mdash;`, or `--`.
- **Voice** - Class/interface = noun phrase. Method = third-person singular verb ("Returns the..."). Field = sentence fragment, no tags.
- **Tags** - Always include `@param`, `@return`, `@throws` where applicable. Lowercase sentence fragments, no trailing period. Single space after the parameter name - never column-align.
- **Cross-references** - Use `{@link}` / `{@linkplain}` / `@see`. Use `{@code}` for inline code. Import link targets so they render with short names.
- **Overrides** - Use `/** {@inheritDoc} */` for methods that override library/framework types. Do not rewrite the parent doc.
- **Field getters** - Field-like interface methods (no params, non-void return) use a noun-phrase fragment without `@return` and without "Gets"/"Returns". A `@Getter` field carries its doc on the field, not a separate method Javadoc block.
- **Structure** - `<p>` on its own line between paragraphs; `<ul>` / `<li>` for lists; `<b>` for emphasis inside list items.
- **Forbidden tags** - Never use `@author` or `@since`.
- **Link a live example** - the Piston DTOs carry an `@see` pointing at a real manifest or package URL. Keep that up when the shape changes; it is the fastest way for the next reader to check a field against the wire.

#### Control flow

Omit braces on single-line bodies; use braces when the body wraps across multiple lines. Applies to all single-statement forms (`if`, `for`, `while`, `do`, lambda bodies).

```java
if (id == -1) throw new IOException("Server prematurely ended stream.");

for (PistonManifest.Entry entry : manifest.getVersions()) {
    if (!"release".equals(entry.getType()))
        continue;
    index(entry);
}
```

#### Collections

Use `getFirst()` / `getLast()` for sequenced access - never `get(0)` or `get(size() - 1)`. This excludes non-`SequencedCollection` types such as Gson's `JsonArray`.

#### Exception classes

Project exceptions follow a **five-constructor pattern** in this order:

1. `(Throwable cause)`
2. `(String message)`
3. `(Throwable cause, String message)`
4. `(@PrintFormat String message, Object... args)`
5. `(Throwable cause, @PrintFormat String message, Object... args)`

Root exceptions (extending `RuntimeException`) reverse the `super()` parameter order:

```java
super(message, cause);
super(String.format(message, args), cause);
```

Child exceptions pass through to the parent, which handles the reversal:

```java
super(cause, message);
super(cause, message, args);
```

Message conventions:

- No trailing punctuation.
- Start with an uppercase letter.
- Use `'%s'` for interpolated values in format strings.

Annotations:

- `@NotNull` on `Throwable cause` and `String message` parameters.
- `@PrintFormat` on format string parameters (from `org.intellij.lang.annotations`).
- `@Nullable` on `Object... args` parameters.

Javadoc:

- **Class-level** - "Thrown when [condition]." Never use the words "unchecked" or "exception" in the description.
- **Constructor** - "Constructs a new {@code ClassName} with [description]."
- **`@param` tags** - lowercase, no trailing period.

> [!NOTE]
> `MojangApiException` is not one of these. It is built by the framework's error decoder, so its single `(Gson, ErrorContext)` constructor is fixed by the `ClientConfig.withErrorDecoder` method-reference shape.

#### Contracts

- Every method carries a `@MojangRoute` unless it wants the type-level default. Adding a method under the wrong domain sends the request to the wrong host **and** charges the wrong rate-limit bucket, and neither failure names the annotation.
- A new host goes in `MojangDomain` with its own `RateLimit`, never as a literal in a request line. The rate limit is the reason the enum exists.
- A body over a few megabytes returns `InputStream`, and its Javadoc says the caller must close it. A body of known-small size returns `byte[]`.
- Where an endpoint's argument is derivable from another response's entry object, add a `default` overload taking that entry. Rules like the two-character asset-CDN prefix should be written once, in the overload, rather than at each call site.

### Commit Messages

Write clear, concise commit messages that describe *what* changed and *why*.

```
Bucket the session server separately from the default domain

sessionserver.mojang.com enforces its own limit and a burst of
property lookups was exhausting the shared bucket for every other
domain on the same contract. Give the constant its own RateLimit so
the two throttle independently.
```

- Use the imperative mood ("Add", "Fix", "Update", not "Added", "Fixes").
- Keep the subject line under 72 characters.
- Add a body when the *why* isn't obvious from the subject.
- Dependency bumps use the `build(deps):` prefix and name the artifact and the new SHA.

### Validating Output

There is no suite, so verification is manual and specific.

- **Compile**

  ```bash
  ./gradlew build
  ```

- **Call the endpoint.** For any change to a request line, a route, or a DTO field, run the call once against the live service and paste the outcome into the PR. A scratch `main()` against a real `Client` is enough.

- **Diff against the wire.** Fetch the raw JSON alongside it and compare field by field:

  ```bash
  curl -s https://piston-meta.mojang.com/mc/game/version_manifest_v2.json | jq '.versions[0]'
  ```

  A field Gson does not recognise is dropped silently, so "it parsed" proves only that nothing threw.

- **Check the bucket.** After a routing change, assert that `client.isRateLimited(MojangDomain.X)` reflects the domain you expected - a misrouted method still succeeds while charging the wrong bucket, and that only surfaces later as an unexplained throttle.

> [!TIP]
> Spend one request, not a loop. The default budget is 200 per 2 minutes per IP and the session server allows about 400 per 10 seconds; exhausting either while testing gets your address throttled for the rest of the window.

## Submitting a Pull Request

1. **Push your branch** to your fork.

   ```bash
   git push origin feat/my-feature
   ```

2. **Open a Pull Request** against the `master` branch of [simplified-api/mojang](https://github.com/simplified-api/mojang).

3. **In the PR description**, include:
   - A summary of the changes and the motivation behind them.
   - The exact endpoint you called and a trimmed copy of what it returned.
   - A link to the documentation or wiki page describing the shape, where one exists.
   - Any new `MojangDomain` constant and the rate limit you gave it, with the source for that number.

4. **Respond to review feedback.** PRs may go through one or more rounds of review before being merged.

### What gets reviewed

- **Route correctness.** The domain on a method is checked against the host the endpoint actually lives on. This is the failure mode with the least helpful symptom in the whole module.
- **Rate-limit honesty.** A constant's `RateLimit` is a claim about the remote service. Cite where the number came from; a guessed limit is worse than the default because it looks researched.
- **Stream ownership.** Any `InputStream` return must document that the caller closes it. The framework does not close the response after decoding, by design.
- **Nullability and defaults.** An upstream field absent on some responses is `Optional` or defaulted; one that is always present is `@NotNull`. Getting this backwards produces a null far from its cause.
- **Javadoc and exception style** as documented above. Inconsistent style will be flagged.

## Reporting Issues

Use [GitHub Issues](https://github.com/simplified-api/mojang/issues) to report bugs or request features.

When reporting a bug, include:

- **JDK version** (`java -version`)
- **Operating system**
- **The contract method** that reproduces the issue
- **The domain** it routed to, and whether you were IPv4 or IPv6
- **The raw response** from `curl`, trimmed to the failing subtree
- **Expected vs. actual bound value**
- **Full stack trace** (if applicable)

For a `MinecraftServerPing` issue, include the server address and port, and whether the MOTD came back as a plain string or a chat-component object - the two paths through the pinger are different code.

## Project Architecture

A brief overview to help you find your way around the codebase:

```
api.simplified.mojang/
├── MojangContract.java           # one Feign interface, routed across fourteen hosts
├── MinecraftServerPing.java      # TCP socket + Minecraft handshake; not HTTP, not Feign
├── MojangApiGsonContributor.java # SPI hook; teaches Gson the bulk-lookup array shape
├── exception/                    # MojangApiException + the decoded MojangErrorResponse
├── request/
│   ├── MojangDomain.java         # host + RateLimit per domain; the DynamicRouteProvider
│   └── MojangRoute.java          # @DynamicRoute meta-annotated selector
└── response/                     # one DTO per endpoint shape
```

### Routing

```
@MojangRoute(DOMAIN) on the interface     -> the default for every method
@MojangRoute(DOMAIN) on a method          -> overrides it
  -> RouteDiscovery, at proxy construction
    -> host baked into the method metadata
    -> RateLimit bucket key baked in alongside it
```

Resolution happens once, when the `Client` is built - not per request. A wrong annotation is therefore a permanent property of the proxy rather than an intermittent fault.

### Two clients that are not the Client

Two paths in this module bypass the framework entirely, and both are deliberate:

| Path | Transport | Consequence |
|------|-----------|-------------|
| `MinecraftServerPing` | raw `Socket` | no pooling, no rate limiting; its own timeout argument |
| `MojangProfile.Textures` | `ImageIO.read(new URL(...))` | downloads skin and cape PNGs with no pooling, no rate limiting, no error decoding |

`MojangProperty` and `MinecraftServerPing` also each hold their own static `GsonSettings.defaults().create()`, separate from whatever Gson the `Client` was configured with.

## Legal

By submitting a pull request, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE.md), the same license that covers this project.

**Do not commit downloaded artifacts.** Client JARs, asset objects, skin PNGs and captured player profiles must never enter the repository - they are Mojang's copyrighted material or another player's data, and neither is yours to redistribute.

Minecraft is a trademark of Mojang AB, a Microsoft subsidiary. This library is an independent client for Mojang's public web services and is not affiliated with, endorsed by, or sponsored by Mojang AB or Microsoft. Use of anything fetched through it is subject to the [Minecraft EULA](https://www.minecraft.net/en-us/eula) and [Usage Guidelines](https://www.minecraft.net/en-us/usage-guidelines).
