package net.potatocloud.api.service;

/**
 * States used during the service lifecycle.
 */
public enum ServiceState {

    /**
     * The service is preparing files, templates, and environment.
     */
    PREPARING,

    /**
     * The service process is starting.
     */
    STARTING,

    /**
     * The service is running.
     */
    RUNNING,

    /**
     * The service is stopping.
     */
    STOPPING,

    /**
     * The service is fully stopped.
     */
    STOPPED,
}
