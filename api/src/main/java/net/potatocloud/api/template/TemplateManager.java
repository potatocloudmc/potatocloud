package net.potatocloud.api.template;

import java.util.List;
import java.util.Optional;

/**
 * Represents the template manager.
 */
public interface TemplateManager {

    /**
     * Gets all templates.
     *
     * @return a list of all templates
     */
    List<Template> templates();

    /**
     * Gets a template by its name.
     *
     * @param name the name of the template
     * @return the template, or an empty optional if not found
     */
    default Optional<Template> find(String name) {
        return templates().stream()
                .filter(template -> template.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Checks whether a template with the given name exists.
     *
     * @param name the name of the template
     * @return {@code true} if the template exists, otherwise {@code false}
     */
    default boolean exists(String name) {
        return find(name).isPresent();
    }

    /**
     * Creates a new template.
     *
     * @param template the template to create
     */
    void create(Template template);

    /**
     * Deletes a template and all of its files.
     *
     * @param template the template to delete
     */
    void delete(Template template);
}
