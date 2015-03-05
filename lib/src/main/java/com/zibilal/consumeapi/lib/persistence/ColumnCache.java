package com.zibilal.consumeapi.lib.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by bmuhamm on 5/7/14.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnCache {
    String columName() default "";
    boolean isPrimaryKey()default false;
    boolean isMultiplePrimaryKey() default false;
    boolean autoincrement() default false;
    boolean isNotNull() default false;
    boolean isNotSave() default false;
    boolean isKeyword() default false;
}
