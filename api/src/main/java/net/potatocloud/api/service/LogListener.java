package net.potatocloud.api.service;

/**
 * Listens to new console lines of a service.
 */
public interface LogListener {

    /**
     * Called for every new console line.
     *
     * @param line the new line
     */
    void onLog(String line);

}
