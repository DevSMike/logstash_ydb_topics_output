# Logstash YDB Topics Output Plugin

На вход плагина поступают данные из **Logstash** , а на выход передаются непосредтсвенно в **YDB Topics**.

## Тестовая Конфигурация Output Плагина

```
input {
  stdin {
    codec => line
  }
}

output {
  ydb_topics_output {
    count => 3  # Количество сообщений для чтения
    prefix => "message"  # Префикс для сообщений
    topic_path => "topic_path"  # Путь к топику в YDB Topics
    connection_string => "grpc://localhost:2136?database=/local"
  }
}
```
