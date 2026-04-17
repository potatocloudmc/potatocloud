package net.potatocloud.node.setup;

import lombok.Getter;

@Getter
public class SetupManager {

    private Setup currentSetup;

    public void startSetup(Setup setup) {
        this.currentSetup = setup;
        setup.start();
    }

    public void endSetup() {
        currentSetup = null;
    }
}