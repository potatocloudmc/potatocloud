package net.potatocloud.node.setup;

import java.util.List;
import java.util.Map;

public interface SetupChoicesProvider {

    List<String> getChoices(Map<String, String> answers);

}
