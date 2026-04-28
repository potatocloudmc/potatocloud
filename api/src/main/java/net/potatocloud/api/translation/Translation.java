package net.potatocloud.api.translation;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.property.PropertyHolder;

import java.util.Locale;

public interface Translation {

    /**
     * Gets the group of the translation.
     *
     * @return the group of the translation
     */
    String getGroup();

    /**
     * Gets locale of the translation.
     *
     * @return the locale of the translation
     */
    Locale getLocale();

    /**
     * Gets key of the translation.
     *
     * @return the key of the translation
     */
    String getKey();

    /**
     * Gets the value.
     *
     * @return the value
     */
    String getValue();

    /**
     * Sets the value of the translation.
     *
     * @param value the new value of the translation
     */
    void setValue(String value);

    /**
     * Updates the service.
     */
    default void update() {
        CloudAPI.getInstance().getTranslationManager().updateTranslation(this);
    }
}
