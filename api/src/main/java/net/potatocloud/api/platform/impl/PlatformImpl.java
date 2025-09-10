package net.potatocloud.api.platform.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class PlatformImpl implements Platform {

    private final String name;
    private final String downloadUrl;
    private final boolean custom;
    private final boolean isProxy;
    private final String base;

    private final List<PlatformVersion> versions = new ArrayList<>();
    private final List<String> prepareSteps = new ArrayList<>();
}
