# Clj-Boot

An attempt to implement something similar to [Spring Boot](http://projects.spring.io/spring-boot/) in Clojure.
The idea is to provide sane defaults and a reasonable template to start a new web application project in a professional context.

## Professional .. enterprise

By professional I mean that certain concerns should be addressed which are not relevant in all hobby projects. 
I do not think the Clojure needs heavy opinionated frameworks and this is not aimed to become such.

# What have we here

* Sane logging (UTF-8, [Logback](http://logback.qos.ch/) based through clj-log)
* Reasonable HTTP access log, [Lolog](https://github.com/lokori/lolog)
* Embedded light-weight server ([HTTP-kit](http://www.http-kit.org/))
* Support REPL workflow (start/stop/restart hooks in user.clj)
* Heartbeat URL (status page)
* External configuration outside the JAR (with type checking to prevent mistakes)
* Web application and Ring separated (to allow functional unit testing without HTTP protocol and server)
* Localization support
* JSON serialization support
* Retry macro
* Sane exception handling
* Client-side Javascript error logger


# What was intentionally dropped

* Relational DB support 
* API support ([Swagger](https://github.com/metosin/compojure-api/blob/master/src/compojure/api/swagger.clj))
* Context sensitive authorization

These are recurring problems but much more context sensitive. More difficult to provide a "default solution" which would make sense.

# Somewhat opinionated assumptions

Obviously certain libraries have been selected such as Compojure. Beoynd the obvious there are a few worth mentioning:

* The software provides route to the status page. In development mode the status page contains settings. In production mode it's just OK for ping and monitoring.
* build-id.txt file is used to determine the build version if it exists. Generate during the CI build to automagically access the version.
* install-history.txt is used to show installation history. Generate during automated deployment to automagically access the installation history.
* Localization support is for fi/sv at the moment. Change this if you need it, it's just an example.
* Logback configuration automatically loads from logback.xml. 


# Running 

```
lein uberjar
java -jar target/weba-standalone.jar
```

Then you can try the front page: http://localhost:8081
The status page should respond at: http://localhost:8081/fi/status

A sample session's output might look like this
```
2014-11-25 08:56:29.709 INFO  weba.server: Starting server, version dev
2014-11-25 08:56:29.714 INFO  weba.settings: logback configuration reset:  /Users/anttivi/work/projects/weba/resources/logback.xml
2014-11-25 08:56:29.740 INFO  weba.server: Server started at port 8081
2014-11-25 08:56:33.397 INFO  lolog.core: Request 2 start.  remote-addr: 0:0:0:0:0:0:0:1 ,method: GET ,uri: /fi/ ,query-string:  ,user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36 ,referer: 
2014-11-25 08:56:33.416 INFO  lolog.core: Request 2 end. Duration: 20 ms. uri: /fi/
2014-11-25 08:56:33.517 INFO  lolog.core: Request 3 start.  remote-addr: 0:0:0:0:0:0:0:1 ,method: GET ,uri: /favicon.ico ,query-string:  ,user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36 ,referer: 
2014-11-25 08:56:33.518 INFO  lolog.core: Request 3 end. Duration: 1 ms. uri: /favicon.ico
```


# Credits

While much of the code has been written by me, much of it has been borrowed. I can't recall everything but the code is heavily based on 
[Aituhaku](https://github.com/Opetushallitus/aituhaku) project written for [Finnish National Board of Education](http://www.oph.fi). The code
for that project was written partly in Finnish because that's how we agreed on the project. I translated the code to english and modified
it to suit the purpose of serving as a more general example.

## Why?

Nothing like this seems to exist. 

## License

Copyright © 2014 Antti Virtanen

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
