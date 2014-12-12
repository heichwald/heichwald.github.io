---
layout: post
title: 'Awesome logs visualization with Kibana from a Play or Node application'
date: '2014-12-12'
author: Herve
tags: 'LOGS, KIBANA, PLAY, NODE, ELASTICSEARCH'
---

#Dependencies

{% highlight bash %}
herve@crazycat:~$ sudo apt-get update
{% endhighlight %}

##Install java

{% highlight bash %}
#Check you have jdk installed
herve@crazycat:~$ javac -version
The program 'javac' can be found in the following packages:

#Install JDK 7
herve@crazycat:~$ sudo apt-get install openjdk-7-jdk
herve@crazycat:~$ javac -version
javac 1.7.0_65 
{% endhighlight %}

##Install elasticsearch
[Download page](http://www.elasticsearch.org/overview/elkdownloads/)
{% highlight bash %}
herve@crazycat:~$ wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.4.1.deb
herve@crazycat:~$ sudo dpkg -i elasticsearch-1.4.1.deb

#Elastic search 1.4 disable CORS by default, but we need it for Kibana 1.3, so edit the following file
herve@crazycat:~$ sudo vim /etc/elasticsearch/elasticsearch.yml

#Add these 2 lines at he end of the file
http.cors.allow-origin: "/.*/"
http.cors.enabled: true

#Start service
herve@crazycat:~/Downloads$ sudo /etc/init.d/elasticsearch start
 * Starting Elasticsearch Server
{% endhighlight %}

Click [here](http://localhost:9200/) to ensure it is working.   
You should get somethig like:
{% highlight javascript %}
{
  "status" : 200,
  "name" : "Demiurge",
  "cluster_name" : "elasticsearch",
  "version" : {
    "number" : "1.4.1",
    "build_hash" : "89d3241d670db65f994242c8e8383b169779e2d4",
    "build_timestamp" : "2014-11-26T15:49:29Z",
    "build_snapshot" : false,
    "lucene_version" : "4.10.2"
  },
  "tagline" : "You Know, for Search"
}
{% endhighlight %}

##Install logstash
[Download page](http://www.elasticsearch.org/overview/logstash/download/)
{% highlight bash %}
herve@crazycat:~$ wget https://download.elasticsearch.org/logstash/logstash/packages/debian/logstash_1.4.2-1-2c0f5a1_all.deb
herve@crazycat:~$ sudo dpkg -i logstash_1.4.2-1-2c0f5a1_all.deb 
{% endhighlight %}

###Create a default logstash configuration file

{% highlight bash %}
herve@crazycat:~$ sudo vim /etc/logstash/conf.d/default.conf
{% endhighlight %}

{% highlight javascript %}
input {
  stdin { }
  tcp {
    port => 4560
  }
}

output {
  stdout { codec => rubydebug }
  elasticsearch {}
}

filter {
  json {
    source => "message"
  }
}
#Note: stdin and stdout are optional, feel free to remove them if you don't want log files.
{% endhighlight %}

###Start logstash and Kibana aka logstash-web
{% highlight bash %}
herve@crazycat:~$ sudo /etc/init.d/logstash start
logstash started.
herve@crazycat:~$ sudo /etc/init.d/logstash-web start
logstash-web started.
{% endhighlight %}

>This installed Kibana 3.0.1 at that time. Note also that if you install elasticsearch on another server, you can edit the hostname in `/opt/logstash/vendor/kibana/config.js`

Go to [Kibana](http://localhost:9292/index.html#/dashboard) to ensure, the web server displays fine.

#Creating a simple play application and logs with logback and logstash

Install play through activator:
{% highlight bash %}
cd ~
wget http://downloads.typesafe.com/typesafe-activator/1.2.12/typesafe-activator-1.2.12.zip
unzip typesafe-activator-1.2.12.zip

#Add it to the path -depends on your linux distribution, here for Kubuntu-
echo 'PATH=$PATH:~/activator-1.2.12' >> ~/.bashrc

#Open a new terminal or tab and confirm your path contains activator
herve@crazycat:~$ echo $PATH
/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/home/herve/activator-1.2.12

#Create a new play app
herve@crazycat:~$ cd ~
herve@crazycat:~$ activator new app-logs play-scala
{% endhighlight %}

##Logstash-logback connector

Modify `app-logs/build.sbt` with:
{% highlight scala %}
libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "net.logstash.logback" % "logstash-logback-encoder" % "3.4"
)
{% endhighlight %}

Modify the configuration
Remove the following from `conf/application.conf`
{% highlight bash %}
# Root logger:
logger.root=ERROR

# Logger used by the framework:
logger.play=INFO

# Logger provided to your application:
logger.application=DEBUG
{% endhighlight %}

Create `conf/application-logger.xml` with:
{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <conversionRule conversionWord="coloredLevel" converterClass="play.api.Logger$ColoredLevel" /> 
  <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
      <!-- remoteHost and port are optional (default values shown) -->
      <remoteHost>127.0.0.1</remoteHost>
      <port>4560</port>
      <!-- encoder is required -->
      <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
  </appender>

  <!-- Optional -->
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%coloredLevel %logger{15} - %message%n%xException{5}</pattern>
    </encoder>
  </appender>

  <logger name="play" level="INFO" />
  <logger name="application" level="DEBUG" />
  
  <!-- Off these ones as they are annoying, and anyway we manage configuration ourself -->
  <logger name="com.avaje.ebean.config.PropertyMapLoader" level="OFF" />
  <logger name="com.avaje.ebeaninternal.server.core.XmlConfigLoader" level="OFF" />
  <logger name="com.avaje.ebeaninternal.server.lib.BackgroundThread" level="OFF" />
  <logger name="com.gargoylesoftware.htmlunit.javascript" level="OFF" />

  <root level="ERROR">
    <!-- Optional -->
    <appender-ref ref="STDOUT" />
    <appender-ref ref="LOGSTASH" />
  </root>  
</configuration>
{% endhighlight %}

> **Optional: going further by optimizing performance**  
Play performs fast when used in a non blocking way. By default the logger as configured above will be blocking. This is not a big deal for us right now because logstash
is running on the same server and we are not logging a lot of events when processing a request. Now imagine one second that the logstash instance is slow or located on a different server.
A few ms would be spent each time to log an event and they will add up quickly if the application logs 10 events before sending a response to the client. It might easily be 5ms x 10 events = 50 ms.
In this case and in most cases in a Play app, you might consider using the [async appender offered by logstash](http://logback.qos.ch/manual/appenders.html#AsyncAppender).

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <conversionRule conversionWord="coloredLevel" converterClass="play.api.Logger$ColoredLevel" /> 
  <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
      <!-- remoteHost and port are optional (default values shown) -->
      <remoteHost>127.0.0.1</remoteHost>
      <port>4560</port>
      <!-- encoder is required -->
      <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
  </appender>

  <!-- Optional -->
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%coloredLevel %logger{15} - %message%n%xException{5}</pattern>
    </encoder>
  </appender>

  <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="LOGSTASH" />
  </appender>

  <logger name="play" level="INFO" />
  <logger name="application" level="DEBUG" />
  
  <!-- Off these ones as they are annoying, and anyway we manage configuration ourself -->
  <logger name="com.avaje.ebean.config.PropertyMapLoader" level="OFF" />
  <logger name="com.avaje.ebeaninternal.server.core.XmlConfigLoader" level="OFF" />
  <logger name="com.avaje.ebeaninternal.server.lib.BackgroundThread" level="OFF" />
  <logger name="com.gargoylesoftware.htmlunit.javascript" level="OFF" />

  <root level="ERROR">
    <!-- Optional -->
    <appender-ref ref="STDOUT" />
    <appender-ref ref="ASYNC" />
  </root>  
</configuration>
{% endhighlight %}

Add some logs

{% highlight scala %}
//app/controllers/Application.scala

package controllers

import play.api._
import play.api.mvc._
import play.api.Logger

object Application extends Controller {

  def index = Action {
	// Log some debug info
	Logger.info("Rendering homepage.")
	Logger.warn("Not really in trouble.")
    Ok(views.html.index("Your new application is ready."))
  }

}
{% endhighlight %}

Start the application
{% highlight bash %}
herve@crazycat:~/app-logs$ activator run
[info] Loading project definition from /home/herve/app-logs/project
[info] Set current project to app-logs (in build file:/home/herve/app-logs/)

--- (Running the application, auto-reloading is enabled) ---

[info] play - Listening for HTTP on /0:0:0:0:0:0:0:0:9000

(Server started, use Ctrl+D to stop and go back to the console...)

[info] play - Application started (Dev)
{% endhighlight %} 

To see your logs, browse to the play [homepage](http://localhost:9000/) and refresh the page a few times then [browse elastic](http://localhost:9200/logstash-2014.12.11/_search) -change date- to confirm you see logs in elasticsearch.  
One entry looks like:
{% highlight javascript %}
{
	message: "Rendering homepage.",
	@version: 1,
	@timestamp: "2014-12-11T18:42:46.396Z",
	host: "127.0.0.1:53054",
	logger_name: "application",
	thread_name: "play-akka.actor.default-dispatcher-8",
	level: "INFO",
	level_value: 30000,
	HOSTNAME: "crazycat",
	application.home: "/home/herve/app-logs"
}
{% endhighlight %} 

# Kibana

<a href="/public/images/kibana.jpg" data-lightbox="kibana" data-title="Kibana in action">
	**It is time to see Kibana in action** ![](/public/images/kibana.jpg "Click to enlarge")
</a>

> You can define your own queries and add/remove/move panels on the dashboard. In the image above, we defined two queries, refined the time period to be last 15 minutes
and add an histogram panel to show the count of INFO vs WARN messages each 10 sec.

#Create a new Node.js app

{% highlight bash %}
herve@crazycat:~$ sudo apt-get install nodejs
herve@crazycat:~$ sudo apt-get install npm
herve@crazycat:~$ mkdir app-logs-express
herve@crazycat:~$ cd app-logs-express
herve@crazycat:~/app-logs-express$ npm init (say yes to everything)
herve@crazycat:~/app-logs-express$ npm install express --save
herve@crazycat:~/app-logs-express$ npm install winston-logstash --save
herve@crazycat:~/app-logs-express$ vim index.js
{% endhighlight %} 

{% highlight javascript %}
var winston = require('winston');
require('winston-logstash');
  winston.add(winston.transports.Logstash, {
    port: 4560,
    node_name: 'log-app',
    host: '127.0.0.1'
  });

var express = require('express');
var app = express();

app.get('/', function (req, res) {
  winston.info('Hello World!');
  res.send('Hello World!');
})

var server = app.listen(3000, function () {

  var host = server.address().address;
  var port = server.address().port;

  winston.info('Example app listening at http://%s:%s', host, port);

})
{% endhighlight %} 

Start the server
{% highlight bash %}
herve@crazycat:~/app-logs-express$ nodejs index.js
#Open a browser tab and browse to [express page](localhost:3000) to confirm your server is working
{% endhighlight %} 

One entry in elasticsearch looks like:
{% highlight javascript %}
{
	message: "Hello World!",
	@version: "1",
	@timestamp: "2014-12-11T21:13:57.043Z",
	host: "127.0.0.1:53288",
	level: "info",
	label: "log-app"
}
{% endhighlight %} 

#Differentiating applications with type

When having multiple applications on different servers, you can refine your daily index to include a type.
{% highlight bash %}
#Modify `/etc/logstash/conf/default.conf` tcp section:
  tcp {
    port => 4560
    type => nodejs 
  }
#Restart logstash
herve@crazycat:~$ sudo /etc/init.d/logstash restart
{% endhighlight %} 

Refresh express hello world a couple of times and check that [elasticsearch type](http://localhost:9200/logstash-2014.12.11/nodejs/_search) -*change date*- now exists