package org.logstashplugins.util;

import java.util.Map;

public class MessageProcessorCreator {
    public static String processJsonString(Map<String, Object> data) {
        return "{\"" + data.keySet().iterator().next() + "\":" + "\"" + data.values().iterator().next() + "\"}";
    }

    public static String processBinaryString(Map<String, Object> data) {
        return data.toString();
    }

}
