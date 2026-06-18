package net.potatocloud.webinterface.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import net.potatocloud.webinterface.model.ApiPlatformVersion;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@Jacksonized
@Accessors(fluent = true, chain = true)
public class PlatformDetailResponse extends PlatformSummaryResponse {

    @Schema(description = "List of versions available for this platform")
    private List<ApiPlatformVersion> versions;

}