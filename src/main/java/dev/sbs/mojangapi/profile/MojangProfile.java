package dev.sbs.minecraftapi.client.mojang.profile;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.StringUtil;
import dev.sbs.minecraftapi.client.mojang.response.MojangPropertiesResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Getter
@NoArgsConstructor
public class MojangProfile {

    private UUID uniqueId;
    private String username;
    private Instant timestamp;
    private ConcurrentList<String> profileActions = Concurrent.newList();
    private Textures textures;

    public MojangProfile(@NotNull MojangPropertiesResponse properties) {
        this.uniqueId = properties.getUniqueId();
        this.username = properties.getUsername();
        this.timestamp = mapProperty(properties, property -> property.getValue().getTimestamp()).orElse(Instant.now());
        this.profileActions = properties.getProfileActions();
        this.textures = new Textures(properties);
    }

    private static <T> Optional<T> mapProperty(@NotNull MojangPropertiesResponse properties, @NotNull Function<MojangProperty, T> function) {
        return properties.getProperties()
            .findFirst()
            .map(function);
    }

    @Getter
    @NoArgsConstructor // Sbs Gson
    @AllArgsConstructor // Default
    public static class Textures {

        private boolean slim;
        private Raw raw = new Raw("", Optional.empty());
        private Optional<Value> skin = Optional.empty();
        private Optional<Value> cape = Optional.empty();

        @SneakyThrows
        public Textures(@NotNull MojangPropertiesResponse properties) { // Mojang Api
            MojangProperty property = properties.getProperty();
            MojangProperty.Value value = property.getValue();
            this.slim = value.isSlim();
            this.raw = new Raw(
                property.getRaw(),
                property.getSignature()
            );
            this.skin = value.getSkinUrl().map(skinUrl -> new Value(
                skinUrl,
                encodeImage(getImage(skinUrl))
            ));
            this.cape = value.getCapeUrl().map(capeUrl -> new Value(
                capeUrl,
                encodeImage(getImage(capeUrl))
            ));
        }

        @SneakyThrows
        private static @NotNull BufferedImage getImage(@NotNull String requestUrl) {
            return ImageIO.read(new URL(requestUrl));
        }

        @SneakyThrows
        private static @NotNull String encodeImage(@NotNull BufferedImage bufferedImage) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", outputStream);
            return StringUtil.encodeBase64ToString(outputStream.toByteArray());
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Raw {

            private String value;
            private Optional<String> signature = Optional.empty();

        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Value {

            // 8667ba71-b85a-4004-af54-457a9734eed7
            @Getter private final static @NotNull Textures.Value defaultSteve = new Textures.Value(
                String.format("http://assets.mojang.com/SkinTemplates/%s.png", "steve"),
                "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAFDUlEQVR42u2a20sUURzH97G0LKMotPuWbVpslj1olJXdjCgyisowsSjzgrB0gSKyC5UF1ZNQWEEQSBQ9dHsIe+zJ/+nXfM/sb/rN4ZwZ96LOrnPgyxzP/M7Z+X7OZc96JpEISfWrFhK0YcU8knlozeJKunE4HahEqSc2nF6zSEkCgGCyb+82enyqybtCZQWAzdfVVFgBJJNJn1BWFgC49/VpwGVlD0CaxQiA5HSYEwBM5sMAdKTqygcAG9+8coHKY/XXAZhUNgDYuBSPjJL/GkzVVhAEU5tqK5XZ7cnFtHWtq/TahdSw2l0HUisr1UKIWJQBAMehDuqiDdzndsP2EZECAG1ZXaWMwOCODdXqysLf++uXUGv9MhUHIByDOijjdiSAoH3ErANQD73C7TXXuGOsFj1d4YH4OTJAEy8y9Hd0mCaeZ5z8dfp88zw1bVyiYhCLOg1ZeAqC0ybaDttHRGME1DhDeVWV26u17lRAPr2+mj7dvULfHw2q65fhQRrLXKDfIxkau3ZMCTGIRR3URR5toU38HbaPiMwUcKfBAkoun09PzrbQ2KWD1JJaqswjdeweoR93rirzyCMBCmIQizqoizZkm2H7iOgAcHrMHbbV9KijkUYv7qOn55sdc4fo250e+vUg4329/Xk6QB/6DtOws+dHDGJRB3XRBve+XARt+4hIrAF4UAzbnrY0ve07QW8uHfB+0LzqanMM7qVb+3f69LJrD90/1axiEIs6qIs21BTIToewfcSsA+Bfb2x67OoR1aPPzu2i60fSNHRwCw221Suz0O3jO+jh6V1KyCMGse9721XdN5ePutdsewxS30cwuMjtC860T5JUKpXyKbSByUn7psi5l+juDlZYGh9324GcPKbkycaN3jUSAGxb46IAYPNZzW0AzgiQ5tVnzLUpUDCAbakMQXXrOtX1UMtHn+Q9/X5L4wgl7t37r85OSrx+TYl379SCia9KXjxRpiTjIZTBFOvrV1f8ty2eY/T7XJ81FQAwmA8ASH1ob68r5PnBsxA88/xAMh6SpqW4HRnLBrkOA9Xv5wPAZjAUgOkB+SHxgBgR0qSMh0zmZRsmwDJm1gFg2PMDIC8/nAHIMls8x8GgzOsG5WiaqREgYzDvpTwjLDy8NM15LpexDEA3LepjU8Z64my+8PtDCmUyRr+fFwA2J0eAFYA0AxgSgMmYBMZTwFQnO9RNAEaHOj2DXF5UADmvAToA2ftyxZYA5BqgmZZApDkdAK4mAKo8GzPlr8G8AehzMAyA/i1girUA0HtYB2CaIkUBEHQ/cBHSvwF0AKZFS5M0ZwMQtEaEAmhtbSUoDADH9ff3++QZ4o0I957e+zYAMt6wHkhzpjkuAcgpwNcpA7AZDLsvpwiuOkBvxygA6Bsvb0HlaeKIF2EbADZpGiGzBsA0gnwQHGOhW2snRpbpPexbAB2Z1oicAMQpTnGKU5ziFKc4xSlOcYpTnOIUpzgVmgo+XC324WfJAdDO/+ceADkCpuMFiFKbApEHkOv7BfzfXt+5gpT8V7rpfYJcDz+jAsB233r6yyBsJ0mlBCDofuBJkel4vOwBFPv8fyYAFPJ+wbSf/88UANNRVy4Awo6+Ig2gkCmgA5DHWjoA+X7AlM//owLANkX0w0359od++pvX8fdMAcj3/QJ9iJsAFPQCxHSnQt8vMJ3v2wCYpkhkAOR7vG7q4aCXoMoSgG8hFAuc/grMdAD4B/kHl9da7Ne9AAAAAElFTkSuQmCC"
            );

            // ec561538-f3fd-461d-aff5-086b22154bce
            @Getter private final static @NotNull Textures.Value defaultAlex = new Textures.Value(
                String.format("http://assets.mojang.com/SkinTemplates/%s.png", "alex"),
                "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"
            );

            private String url;
            private String data;

            public boolean isDefaultAlex() {
                return this.getData().equals(getDefaultAlex().getData());
            }

            public boolean isDefaultSteve() {
                return this.getData().equals(getDefaultSteve().getData());
            }

        }

    }

}
