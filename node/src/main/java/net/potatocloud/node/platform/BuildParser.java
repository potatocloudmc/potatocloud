package net.potatocloud.node.platform;

import net.potatocloud.api.platform.PlatformVersion;

public interface BuildParser {

    String getName();

    void parse(PlatformVersion version, String baseUrl);


}
