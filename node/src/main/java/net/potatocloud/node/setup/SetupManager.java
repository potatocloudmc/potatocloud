package net.potatocloud.node.setup;

public class SetupManager {

    private Setup currentSetup;

    public Setup currentSetup() {
        return currentSetup;
    }

    public void startSetup(Setup setup) {
        this.currentSetup = setup;
        setup.start();
    }

    public void endSetup() {
        currentSetup = null;
    }
}