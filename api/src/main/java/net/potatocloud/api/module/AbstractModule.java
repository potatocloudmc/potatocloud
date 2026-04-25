package net.potatocloud.api.module;

import lombok.Getter;
import lombok.Setter;
import net.potatocloud.api.utils.version.Version;

@Setter
@Getter
public abstract class AbstractModule implements Module {

    private String name;
    private Version version;

    @Override
    public void onLoad() {}

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

}
