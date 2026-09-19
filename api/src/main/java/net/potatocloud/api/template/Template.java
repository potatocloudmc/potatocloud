package net.potatocloud.api.template;

/**
 * Represents a service template.
 *
 * @param name the template name
 */
public record Template(String name) {

    public Template {
        name = name.trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Template name must not be empty");
        }

        if (name.equals(".") || name.equals("..") || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Invalid template name: " + name);
        }
    }

    /**
     * Creates a template with the given name.
     *
     * @param name the template name
     * @return the template
     */
    public static Template of(String name) {
        return new Template(name);
    }
}
