package org.logstashplugins.util;

import java.util.Map;

@FunctionalInterface
public interface MessageProcessor {
    String process(Map<String, Object> data);
}