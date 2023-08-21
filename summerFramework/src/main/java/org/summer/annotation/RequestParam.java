package org.summer.annotation;

import org.summer.mvc.utils.WebUtils;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value();
    String defaultValue() default WebUtils.DEFAULT_PARAM_VALUE;
}
