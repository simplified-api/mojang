package api.simplified.mojang.exception;

import com.google.gson.annotations.SerializedName;
import dev.simplified.annotations.AccessLevel;
import dev.simplified.annotations.Getter;
import dev.simplified.annotations.NoArgsConstructor;
import dev.simplified.client.exception.ApiErrorResponse;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MojangErrorResponse implements ApiErrorResponse {

    @SerializedName("error")
    protected String id = "UNKNOWN";
    @SerializedName("errorMessage")
    protected String reason = "Unknown Reason";
    protected String path = "";

}
