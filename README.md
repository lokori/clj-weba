# clj-weba ? 

An attempt to implement something similar to [Spring Boot](http://projects.spring.io/spring-boot/) in Clojure.
The idea is to provide sane defaults and a reasonable template to start a new web application project in a professional context.

Since there is already a project named Clojure Boot this experiment is clj-weba. Supposedly this doesn't mean anything.

The code was presented in the awesome [ClojuTre 2014](http://clojutre.org) event.


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
* Client-side Javascript error logger (Why? [Thoughtworks explains](http://www.thoughtworks.com/radar/techniques/capturing-client-side-javascript-errors)
* Interesting source/properties tests (see below)


# What was intentionally dropped

* Relational DB support 
* API support ([Swagger](https://github.com/metosin/compojure-api/blob/master/src/compojure/api/swagger.clj))
* Context sensitive authorization

These are recurring problems but much more context sensitive. More difficult to provide a "default solution" which would make sense.

# Somewhat opinionated assumptions

Obviously certain libraries have been selected such as Compojure. Beoynd the obvious there are a few worth mentioning:

* The software provides route to the status page. In development mode the status page contains settings. In production mode it's just OK for ping and monitoring.
* build-id.txt file is used to determine the build version if it exists. Generate during the CI build to automagically access the version.
* install-history.txt is used to show installation history. Generate in your [deployment pipeline](dev.solita.fi/2014/10/01/simple-deployment-pipeline.html) to automagically access the installation history.
* Localization support is for fi/sv at the moment and JS-based. Change this if you need it, it's just an example.
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


# Unit testing a web application

Unit testing a web application is quite possible. A sample is included and the test run looks like this:

```
lein test

lein test rest.statuspage-test
2014-11-25 10:55:29.461 INFO  lolog.core: Request 2 start.  remote-addr: localhost ,method: GET ,uri: /fi/status ,query-string:  ,user-agent:  ,referer: 
2014-11-25 10:55:29.544 INFO  lolog.core: Request 2 end. Duration: 280 ms. uri: /fi/status

Ran 1 tests containing 1 assertions.
0 failures, 0 errors.
```

The interesting point is that the test is almost purely functional. The logging is a side-effect, but it would be a bit difficult to trace possible errors without it in CI builds.
To make unit testing possible, the application logic (Ring wrapper stack) is separated from the web server. The test uses the Ring stack but doesn't start up web server. 

This has two obvious benefits: the tests run faster and they can be executed simultaneously.

# Localization

Go to [http://localhost:8081/api/i18n/fi](http://localhost:8081/api/i18n/fi) and the software returns a JSON containing the localization
keys and values. The idea is to use these keys in your UI. This assumes your UI is Javascript-based rather than HTML rendered in the backend,
but I suppose this is a valid assumption these days.

You should get something like this back from the URL:
```
{"ultimate":{"pwner":"Conan the Cimmerian"}}
```



# Heartbeat URL (status page)

There are three "levels" in my opinion:
1. simply provide a ping URL which returns "OK" if everything is fine
2. provide easy access to see the settings
3. provide access to system information

Options 1 and 2 are here. I have done option 3 but modern monitoring systems seem to make it obsolete.

Here's what the option 2 might look like:

![Status page](https://raw.github.com/lokori/clj-weba/master/img/statuspage.png)



# Source code tests

There are things which are relatively easy to check automatically and a source test is provided which checks the following:

* Javascript source for console.log/debug calls which require developer tools to work. Not a good idea in production.
* localization properties files have matching keys
* pre/post conditions are defined properly. Clojure compiler doesn't warn if they are incorrectly defined.

Using the same pattern it's possible to check more interesting things from Clojure source code using the 
[Reader](http://clojure.org/reader) and building on the [homoiconocity](http://en.wikipedia.org/wiki/Homoiconicity) of the language. 
To my understanding, despite being very awesome, Haskel and all that Hindley-Milner stuff will not do this :) 



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
