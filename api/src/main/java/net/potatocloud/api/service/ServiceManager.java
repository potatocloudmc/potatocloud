package net.potatocloud.api.service;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.template.Template;
import net.potatocloud.api.template.TemplateCopyOptions;

import java.util.Comparator;
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
     * Gets all services assigned to a fallback group.
     *
     * @return the fallback services
     */
    default List<Service> fallbackServices() {
        return services().stream()
                .filter(service -> {
                    final Group group = service.group();
                    return group != null && group.fallback();
                })
                .toList();
    }

    /**
     * Gets the best available fallback service.
     *
     * @return the fallback service, or an empty optional if none is available
     */
    default Optional<Service> findBestFallback() {
        return fallbackServices().stream()
                .filter(Service::running)
                .filter(service -> !service.full())
                .min(Comparator.comparingInt(Service::playerCount));
    }

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
     * Stops all services in a group.
     *
     * @param group the group whose services should be stopped
     * @return a future that completes when all services have stopped
     */
    default CompletableFuture<Void> stop(Group group) {
        return CompletableFuture.allOf(
                group.services().stream()
                        .map(this::stop)
                        .toArray(CompletableFuture[]::new)
        );
    }

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
     * @param options the copy options
     */
    void copyTo(Service service, Template template, TemplateCopyOptions options);

    /**
     * Copies service files to a template.
     *
     * @param service the service to copy files from
     * @param template the template to copy to
     */
    default void copyTo(Service service, Template template) {
        copyTo(service, template, TemplateCopyOptions.all());
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
