# rtr-statistic

Building a war archive
-----

> mvn compile war:war

Github action
----

The `WAR build` action produces a WAR file that can be used on a server. This only applies to the `feature/war` branch.


Logstash configuration
---

In tomcats `context.xml`, set the path to a config like

```xml
<Parameter name="LOGGING_CONFIG_FILE_STATISTIC" value="/var/lib/tomcat9/conf/logback.xml" override="false"/>
```

and set the configured xml accordingly, e.g.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration>

<configuration scan="true">
    <include resource="org/springframework/boot/logging/logback/base.xml"/>

    <appender name="logstash" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <param name="Encoding" value="UTF-8"/>
        <remoteHost>your-host.example.com</remoteHost>
        <port>5000</port>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"app_name":"statistic-service", "host":"your-host.example.com"}</customFields>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="logstash"/>
    </root>
</configuration>


```