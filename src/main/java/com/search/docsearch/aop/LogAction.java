package com.search.docsearch.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAction {
    String type() default "Unoperated resources";

    String OperationResource() default "Unoperated resources";
}