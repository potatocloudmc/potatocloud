package net.potatocloud.common.config;

import java.util.List;
import java.util.Set;

public interface ConfigNode {

    String asString();

    int asInt();

    double asDouble();

    boolean asBoolean();

    List<String> asStringList();

    Set<String> keys();

}
