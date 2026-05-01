package api.simplified.mojang.exception;

import com.google.gson.annotations.SerializedName;
import dev.simplified.client.exception.ApiErrorResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MojangErrorResponse implements ApiErrorResponse {

    @SerializedName("error")
    protected String id = "UNKNOWN";
    @SerializedName("errorMessage")
    protected String reason = "Unknown Reason";
    protected String path = "";

}
