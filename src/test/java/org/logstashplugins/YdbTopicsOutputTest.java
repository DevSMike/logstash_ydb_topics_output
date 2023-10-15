package org.logstashplugins;

import co.elastic.logstash.api.*;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class YdbTopicsOutputTest {

    private static final String CONNECTION_STRING = "grpc://localhost:2136?database=/local";

    private static final String TOPIC_PATH = "my-topic";

    @Test
    public void testMessageWriting()  {
        Map<String, Object> configValues = new HashMap<>();
        configValues.put("topic_path", TOPIC_PATH);
        configValues.put("connection_string", CONNECTION_STRING);
        Configuration config = new ConfigurationImpl(configValues);

        YdbTopicsOutput output = new YdbTopicsOutput("test", config, null);

        CustomEvent event1 = new CustomEvent();
        event1.setField("Hello, from YDB", null);

        output.output(List.of(event1));
        output.stop();

        assertEquals(output.getCurrentMessage(), "{1=Hello, from YDB}");
    }
}
