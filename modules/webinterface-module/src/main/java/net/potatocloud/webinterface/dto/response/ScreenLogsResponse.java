package net.potatocloud.webinterface.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class ScreenLogsResponse {

    @Schema(description = "Name of the screen", examples = "lobby-1")
    private String screenName;
    @Schema(description = "Logs of the screen", examples = {"[12:00:00] [INFO] Starting server...", "[12:00:01] [INFO] Server started successfully!"})
    private List<String> logs;

}
