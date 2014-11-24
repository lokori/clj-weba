# Clj-Boot

An attempt to implement something similar to [Spring Boot](http://projects.spring.io/spring-boot/) in Clojure.
The idea is to provide sane defaults and a reasonable template to start a new web application project in a professional context.

## Professional .. enterprise

By professional I mean that certain concerns should be addressed which are not relevant in all hobby projects. 
I do not think the Clojure needs heavy opinionated frameworks and this is not aimed to become such.

# What have we here

* Sane logging (UTF-8, Logback based through clj-log)
* Reasonable HTTP access log, [Lolog](https://github.com/lokori/lolog)
* Embedded light-weight server (HTTP-kit)
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
* API support (Swagger)
* Context sensitive authorization

These are recurring problems but much more context sensitive. More difficult to provide a "default solution" which would make sense.

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
