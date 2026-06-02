Open-RMBT-Statistics
=========

> *Open-RMBT* is an open source, multithreaded bandwidth measurement system.

It consists of the following components:
* Web site
* JavaScript client
* Android client 
* iOS client
* Desktop client
* Measurement server
* QoS measurement server
* Control server
* Statistics server (in this repository) 
* Map server

*Open-RMBT* is released under the [Apache License, Version 2.0](LICENSE). It was developed
by the [Austrian Regulatory Authority for Broadcasting and Telecommunications (RTR-GmbH)](https://www.rtr.at/).


Related material
----------------

* [RMBT specification](https://www.netztest.at/doc/)
* [RTR-NetTest/open-rmbt](https://github.com/rtr-nettest/open-rmbt) - Orignal repository, today mainly contains Map and QoS-server 
* [RTR-NetTest/rmbt-server](https://github.com/rtr-nettest/rmbt-server) - Test Server for conducting measurements based on the RMBT protocol
* [RTR-NetTest/rmbtws](https://github.com/rtr-nettest/rmbtws) - JavaScript client for conducting RMBT-based speed measurements
* [RTR-NetTest/open-rmbt-control](https://github.com/rtr-nettest/open-rmbt-control) - Control server
* [RTR-NetTest/open-rmbt-ios](https://github.com/rtr-nettest/open-rmbt-ios) - iOS app
* [RTR-NetTest/open-rmbt-android](https://github.com/rtr-nettest/open-rmbt-android) - Android app
* [RTR-NetTest/rtr-nettest/open-rmbt-website](https://github.com/rtr-nettest/open-rmbt-website) - Web site


System requirements for the Statistics-Server
-------------------

* single (virtual) server with sufficient RAM and CPU performance
* Base system Debian 13 or newer
* At least a single static public IPv4 address (IPv6 support recommended)

  *NOTE: other Linux distributions can also be used, but commands and package names may be different*

Installation
--------------

1. Setup IP/DNS/hostname
2. firewall (e.g. iptables)
3. Install git
4. Install and configure sshd
5. Install and configure ntp
6. dpkg-reconfigure locales (database requires en_US.UTF-8)
7. dpkg-reconfigure tzdata


## Database Server

The statistics server uses a Postgresql database. In a production environment it is recommended
to use a local replica of the master database, but for testing purposes the main database
can be used directly. See [RTR-NetTest/open-rmbt](https://github.com/rtr-nettest/open-rmbt)
for basic setup instructions.

## Statistics Server

### Install components 

* Apache Tomcat 10 or higher
* nginx; configure nginx to forward requests to localhost:8080
* letsencrypt; create certificate
* JDK17 .. JDK25
* redis

### Build the RMBTStatisticServer.war archive

> mvn compile war:war

### Alternative: Get WAR using Github action

The `WAR build` action produces a WAR file that can be used on a server. This only applies to the `master` branch.

### Configure Tomcat

##### Configure catalina.properties
Edit `/etc/tomcat9/catalina.properties`, at the end of the file add:

```properties
spring.profiles.active=prod
```
This activates the production spring profile.

##### Configure context.xml
Edit `/etc/tomcat10/context.xml`, add to `<Context>`:

```xml
<!-- Control/Statistic - Identification used in /version endpoint -->
<Parameter name="HOST_ID" value="[host_id]" override="false"/>

<!-- Statistic - database connection -->
<Parameter name="STATISTIC_DB_USER" value="rmbt" override="false"/>
<Parameter name="STATISTIC_DB_PASSWORD" value="[change_me]" override="false"/>
<Parameter name="STATISTIC_DB_HOST" value="localhost" override="false"/>
<Parameter name="STATISTIC_DB_PORT" value="5432" override="false"/>
<Parameter name="STATISTIC_DB_NAME" value="rmbt" override="false"/>

<!-- Statistic redis connection -->
<Parameter name="STATISTIC_REDIS_HOST" value="localhost" override="false"/>
<Parameter name="STATISTIC_REDIS_PORT" value="6379" override="false"/>
```
Substitute parts with `[]`. [host_id] is a short string which identifies the host, e.g. "host1".
Make sure the file `context.xml` is owned by `tomcat`.

##### Configure Logging - Console or Logstash

The default configuration is to send log to `console`. In current Debian installations systemd 
redirects console output to systemd's journal. 
Older systems logged to `/var/log/tomcat9/catalina.out`.

The following `context.xml` configuration sends log to Logstash at `elk.example.com`:

```xml
<!-- Logging  -->
<Parameter name="LOG_HOST"     value="elk.example.com"       override="false"/>
<Parameter name="LOG_PORT"     value="5000"                  override="false"/>
<Parameter name="LOGGING_HOST" value="dev"                   override="false"/>
```

Alternatively, one might want to define a custom logging configuration.
First, the alternative configuration file need to be specified in `context.xml`:
```xml
 <Parameter name="LOGGING_CONFIG_FILE_STATISTIC" value="/etc/tomcat10/logback.xml" override="false"/>
```
Again, make sure that the file `/etc/tomcat10/logback.xml` is owned by `tomcat`.

This example logs to both Logstash and console:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %5p [%t] %-40.40logger{39} : %m%n</pattern>
        </encoder>
    </appender>

    <appender name="logstash" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>elk.example.com:5000</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"app_name":"statistic-service","host":"dev"}</customFields>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="logstash"/>
    </root>

</configuration>
```

### Install and configure PDF export

[Weasyprint](https://weasyprint.org/) is required for PDF export.

 apt -y install weasyprint
