package net.potatocloud.module.rest.template;

import net.potatocloud.api.template.Template;

public record TemplateResponse(String name) {

    public static TemplateResponse from(Template template) {
        return new TemplateResponse(template.name());
    }
}
