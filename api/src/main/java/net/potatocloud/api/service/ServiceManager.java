package net.potatocloud.api.service;

import net.potatocloud.api.group.ServiceGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ServiceManager {

    /**
     * Gets a service by its name.
     *
     * @param name the name of the service
     * @return the service
     */
    Service getService(String name);

    /**
     * Gets all services.
     *
     * @return a list of all services
     */
    List<Service> getAllServices();

    /**
     * Gets all online services.
     *
     * @return a list of all online services
     */
    default List<Service> getOnlineServices() {
        return getAllServices().stream().filter(Service::isOnline).toList();
    }

    /**
     * Gets all services in the given group.
     *
     * @param groupName the name of the group
     * @return a list of all services in the given group
     */
    default List<Service> getAllServices(String groupName) {
        return getAllServices().stream()
                .filter(service -> service.getServiceGroup().getName().equals(groupName))
                .toList();
    }

    /**
     * Gets all services in the given group.
     *
     * @param group the group
     * @return a list of all services in the given group
     */
    default List<Service> getAllServices(ServiceGroup group) {
        return getAllServices().stream()
                .filter(service -> service.getServiceGroup().equals(group))
                .toList();
    }

    /**
     * Gets all online services in the given group.
     *
     * @param groupName the name of the group
     * @return a list of all online services in the given group
     */
    default List<Service> getOnlineServices(String groupName) {
        return getOnlineServices().stream()
                .filter(service -> service.getServiceGroup().getName().equals(groupName))
                .toList();
    }

    /**
     * Updates an existing service.
     *
     * @param service the service to update
     */
    void updateService(Service service);

    /**
     * Starts a service in the given group.
     *
     * @param groupName the name of the group
     */
    void startService(String groupName);

    /**
     * Starts a service in the given group.
     *
     * @param group the group
     */
    default void startService(ServiceGroup group) {
        startService(group.getName());
    }

    /**
     * Starts a service in the given group asynchronously.
     *
     * @param groupName the name of the group
     * @return a future containing the service
     */
    CompletableFuture<Service> startServiceAsync(String groupName);

    /**
     * Starts a service in the given group asynchronously.
     *
     * @param group the group
     * @return a future containing the service
     */
    default CompletableFuture<Service> startServiceAsync(ServiceGroup group) {
        return startServiceAsync(group.getName());
    }

    /**
     * Starts multiple services in the given group.
     *
     * @param groupName the name of the group
     * @param amount    the amount of services to start
     */
    default void startServices(String groupName, int amount) {
        for (int i = 0; i < amount; i++) {
            startService(groupName);
        }
    }

    /**
     * Starts multiple services in the given group.
     *
     * @param group  the group
     * @param amount the amount of services to start
     */
    default void startServices(ServiceGroup group, int amount) {
        startServices(group.getName(), amount);
    }

    /**
     * Starts multiple services in the given group asynchronously.
     *
     * @param groupName the name of the group
     * @param amount    the amount of services to start
     * @return a future containing all started services
     */
    default CompletableFuture<List<Service>> startServicesAsync(String groupName, int amount) {
        final List<CompletableFuture<Service>> futures = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            futures.add(startServiceAsync(groupName));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(future -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList()
                );
    }

    /**
     * Starts multiple services in the given group asynchronously.
     *
     * @param group  the group
     * @param amount the amount of services to start
     * @return a future containing all started services
     */
    default CompletableFuture<List<Service>> startServicesAsync(ServiceGroup group, int amount) {
        return startServicesAsync(group.getName(), amount);
    }

    /**
     * Gets the current service the api is running on.
     * <p>
     * This only works if the API is used from within a plugin.
     * </p>
     *
     * @return the current service
     */
    Service getCurrentService();

}