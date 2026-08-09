package net.potatocloud.api.service;

import net.potatocloud.api.group.Group;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the service manager.
 */
public interface ServiceManager {

    /**
     * Gets a service by its name.
     *
     * @param name the name of the service
     * @return the service, or an empty optional if not found
     */
    Optional<Service> find(String name);

    /**
     * Gets all services.
     *
     * @return a list of all services
     */
    List<Service> services();

    /**
     * Updates an existing service.
     *
     * @param service the service to update
     */
    void update(Service service);

    /**
     * Starts a new service.
     *
     * @param group the target group
     * @return a future containing the started service
     */
    CompletableFuture<Service> start(Group group);

    /**
     * Stops a service.
     *
     * @param service the service to stop
     * @return a future that completes when the service stops
     */
    CompletableFuture<Void> stop(Service service);

    /**
     * Executes a command on a service.
     *
     * @param service the service to execute the command on
     * @param command the command to execute
     */
    void execute(Service service, String command);

    /**
     * Copies service files to a template.
     *
     * @param service the service to copy files from
     * @param template the template to copy to
     * @param filter the filter to apply
     */
    void copyTo(Service service, String template, String filter);

    /**
     * Copies service files to a template.
     *
     * @param service the service to copy files from
     * @param template the template to copy to
     */
    default void copyTo(Service service, String template) {
        copyTo(service, template, "");
    }

    /**
     * Gets the current service the API is running on.
     * <p>
     * This only works if the API is used from within a plugin.
     * </p>
     *
     * @return the current service, or an empty optional when not running on a service
     */
    Optional<Service> current();
}