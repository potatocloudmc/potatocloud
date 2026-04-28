package net.potatocloud.api.translation;

import java.util.List;

public interface TranslationManager {

    /**
     * Gets a service by its name.
     *
     * @param key the key of the translation
     * @return the service
     */
    Translation getTranslation(String key);

    /**
     * Gets all translations.
     *
     * @return a list of all translations
     */
    List<Translation> getAllTranslations();

    /**
     * Updates an existing translation.
     *
     * @param translation the translation to update
     */
    void updateTranslation(Translation translation);

}