package net.potatocloud.webinterface.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class ScreenLogResponse {

    @Schema(description = "Name of the screen", examples = "lobby-1")
    private String screenName;
    @Schema(description = "A log line of the screen", examples = "[12:00:00] [INFO] Starting server...")
    private String line;

}
