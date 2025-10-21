package net.potatocloud.node.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommandInfo {

    String name();

    String description() default "";

    String usage() default "";
}
