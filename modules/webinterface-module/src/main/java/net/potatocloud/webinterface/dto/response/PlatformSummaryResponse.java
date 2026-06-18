package net.potatocloud.webinterface.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSummaryResponse {
    private String name;
    private String base;
    private String downloadUrl;
    private boolean custom;
    private boolean proxy;
    private boolean bukkitBased;
    private boolean paperBased;
    private boolean velocityBased;
    private boolean limboBased;
    private List<String> prepareSteps;
}
