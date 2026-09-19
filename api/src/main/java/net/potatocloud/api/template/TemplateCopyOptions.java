package net.potatocloud.api.template;

/**
 * Configures which service files are copied to a template.
 *
 * @param path the relative path to copy, or an empty string for all files
 * @param replaceExisting whether existing template files should be replaced
 */
public record TemplateCopyOptions(String path, boolean replaceExisting) {

    public TemplateCopyOptions {
        path = path.trim().replace('\\', '/');
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
    }

    /**
     * Copies all service files and replaces existing template files.
     *
     * @return the copy options
     */
    public static TemplateCopyOptions all() {
        return new TemplateCopyOptions("", true);
    }

    /**
     * Copies one file or directory inside the service directory.
     *
     * @param path the relative path
     * @return the copy options
     */
    public static TemplateCopyOptions path(String path) {
        return new TemplateCopyOptions(path, true);
    }

    /**
     * Returns options that keep files already present in the template.
     *
     * @return the copy options
     */
    public TemplateCopyOptions keepExisting() {
        return new TemplateCopyOptions(path, false);
    }

    /**
     * Gets whether all service files are selected.
     *
     * @return {@code true} when all files are selected
     */
    public boolean copiesAll() {
        return path.isEmpty();
    }
}
