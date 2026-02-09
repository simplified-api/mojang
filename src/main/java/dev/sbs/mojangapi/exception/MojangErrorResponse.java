package dev.sbs.minecraftapi.client.mojang.exception;

import com.google.gson.annotations.SerializedName;
import dev.sbs.api.client.exception.ClientErrorResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MojangErrorResponse implements ClientErrorResponse {

    @SerializedName("error")
    protected String type;
    @SerializedName("errorMessage")
    protected String reason;
    protected String path;

    public static class Unknown extends MojangErrorResponse {

        public Unknown() {
            super.type = "UNKNOWN";
            super.reason = "Unknown Reason";
            super.path = "";
        }

    }

}
