package net.potatocloud.connector.translation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.potatocloud.api.translation.Translation;

import java.util.Locale;

@Getter
@Setter
@AllArgsConstructor
public class TranslationImpl implements Translation {

    private String group;
    private Locale locale;
    private String key;
    private String value;

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {

    }
}
