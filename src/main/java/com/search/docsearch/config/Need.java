package com.search.docsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class Need implements Condition {

    @Value("${kafka.need}")
    private boolean needKafka;
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return needKafka;
    }
}
