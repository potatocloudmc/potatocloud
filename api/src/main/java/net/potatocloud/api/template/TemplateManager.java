package net.potatocloud.api.template;

import java.util.List;
import java.util.Optional;

/**
 * Manages the templates of the node this instance is connected to.
 */
public interface TemplateManager {

    /**
     * Gets all templates.
     *
     * @return the templates
     */
    List<Template> templates();

    /**
     * Finds a template by its name.
     *
     * @param name the template name
     * @return the template, if it exists
     */
    default Optional<Template> find(String name) {
        return templates().stream()
                .filter(template -> template.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Checks whether a template exists.
     *
     * @param name the template name
     * @return true if the template exists
     */
    default boolean exists(String name) {
        return find(name).isPresent();
    }

    /**
     * Creates a template if it does not exist yet.
     *
     * @param template the template
     */
    void create(Template template);

    /**
     * Deletes a template including all of its files.
     *
     * @param template the template
     */
    void delete(Template template);
}
