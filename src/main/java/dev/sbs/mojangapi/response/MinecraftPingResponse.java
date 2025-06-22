package dev.sbs.minecraftapi.client.mojang.response;

import dev.sbs.api.util.RegexUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class MinecraftPingResponse {

    private Description description;
    private Players players;
    private Version version;
    private String favicon;
    @Setter
    private long ping;

    @Getter
    @ToString
    public static class Description {

        private String text;

        public String getStrippedText() {
            return RegexUtil.strip(this.text, RegexUtil.VANILLA_PATTERN);
        }

    }

    @Getter
    @ToString
    public static class Players {

        private int max;
        private int online;
        private List<Player> sample;

    }

    @Getter
    @ToString
    public static class Player {

        private String name;
        private String id;

    }

    @Getter
    @ToString
    public static class Version {

        /**
         * Version name (ex: 13w41a)
         */
        private String name;
        /**
         * Protocol version
         */
        private int protocol;

    }

}
