# Logstash YDB Topics Output Plugin

На вход плагина поступают данные из **Logstash** , а на выход передаются непосредственно в **YDB Topics**.


## Инструкция по развертыванию плагина на локальной машине

### 1. Подготовка
Скачать Logstash Codebase по [ссылке](https://disk.yandex.ru/d/nnfPnenQhdP8yw)
### 2. Добавление файла в проект
В корне проекта создать файл *gradle.properties* и установить параметр **LOGSTASH_CORE_PATH=D:/Lib/logstash-main/logstash-core**
### 3. Подготовка окружения
Для запуска и проверки работоспособности плагина потребуется запустить [YDB](https://ydb.tech/ru/docs/getting_started/self_hosted/ydb_docker)
### 4. Инструкция по установке плагина
Для установки плагина в Logstash нужно:
- Собрать проект командной
    - ```./gradlew gem ``` на Linux системах
    - ```./gradlew.bat ``` на Windows системах
- Установить плагин этой командой: ```bin/logstash-plugin install --no-verify --local /path/to/javaPlugin.gem```
- Использовать тестовую конфигурацию для запуска плагина командой ```bin/logstash -f /path/to/java_output.conf```
## Тестовая Конфигурация Output Плагина (анонимная аутентификация)

```
input {
  stdin {
    codec => line
  }
}

output {
  ydb_topics_output {
    count => 3  
    prefix => "message" 
    topic_path => "topic_path"  
    producer_id => "test_producer"
    connection_string => "grpc://localhost:2136?database=/local"
  }
}
```
## Тестовая Конфигурация Output Плагина (аутентификация по токену)

```
input {
  stdin {
    codec => line
  }
}

output {
  ydb_topics_output {
    count => 3  
    prefix => "message" 
    topic_path => "topic_path"  
    producer_id => "test_producer"
    connection_string => "grpc://localhost:2136?database=/local"
    access_token => "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOi"
  }
}
```

## Тестовая Конфигурация Output Плагина (аутентификация при помощи файла сервисного аккаунта)

```
input {
  stdin {
    codec => line
  }
}

output {
  ydb_topics_output {
    count => 3  
    prefix => "message" 
    topic_path => "topic_path"  
    producer_id => "test_producer"
    connection_string => "grpc://localhost:2136?database=/local"
    service_account_key => "path/to/sa_file.json"
  }
}
```