package net.potatocloud.api.group;

import java.util.List;
import java.util.Optional;

/**
 * Represents the group manager.
 */
public interface GroupManager {

    /**
     * Gets a group by its name.
     *
     * @param name the name of the group
     * @return the group, or an empty optional if not found
     */
    Optional<Group> find(String name);

    /**
     * Gets the list of all groups.
     *
     * @return the list of all groups
     */
    List<Group> groups();

    /**
     * Creates a builder for a new group.
     *
     * @param name the name of the group
     * @return the builder
     */
    default GroupBuilder builder(String name) {
        return new GroupBuilder(name);
    }

    /**
     * Creates a new group from the given object.
     *
     * @param group the group to create
     */
    void create(Group group);

    /**
     * Deletes the given group.
     *
     * @param group the group to delete
     */
    void delete(Group group);

    /**
     * Updates an existing group.
     *
     * @param group the group to update
     */
    void update(Group group);

    /**
     * Checks whether a group with the given name exists.
     *
     * @param name the name of the group
     * @return {@code true} if the group exists, otherwise {@code false}
     */
    default boolean exists(String name) {
        return find(name).isPresent();
    }
}
