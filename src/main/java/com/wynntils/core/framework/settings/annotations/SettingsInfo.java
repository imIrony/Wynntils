/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.core.framework.settings.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SettingsInfo {
    String name();
    String displayPath() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @interface Instance {}
}
