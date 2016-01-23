# TMDb-async-client
[![Build Status](https://img.shields.io/travis/newca12/TMDb-async-client.svg)](https://travis-ci.org/newca12/TMDb-async-client)
[![codecov.io](https://img.shields.io/codecov/c/github/newca12/TMDb-async-client.svg)](https://codecov.io/github/newca12/TMDb-async-client?branch=master)
[![Ohloh](http://www.ohloh.net/p/TMDb-async-client/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/TMDb-async-client)
[![Maven Central](https://img.shields.io/maven-central/v/org.edla/tmdb-async-client_2.11.svg)](https://maven-badges.herokuapp.com/maven-central/org.edla/tmdb-async-client_2.11)

### About ###
TMDb-async-client is a native Scala SDK that provides asynchronous access to [The Movie Database][1] (TMDb) API.  
It is built heavily on [akka-http][2] for async non-blocking HTTP I/O and [spray-json][3] for parsing JSON responses into Scala case classes.  
[akka-stream][4] is used to allow limitation of client request frequency and number of parallel requests in respect of TMDb policies.

TMDb-async-client is an EDLA project.

The purpose of [edla.org](http://www.edla.org) is to promote the state of the art in various domains.

### API Key ###
You will need an API key to The Movie Database to access the API.  To obtain a key, follow these steps:

1. Register for and verify an [account](https://www.themoviedb.org/account/signup).
2. [Log](https://www.themoviedb.org/login) into your account
3. Select the API section on left side of your account page.
4. Click on the link to generate a new API key and follow the instructions.

### Usage ###

See [Usage.scala](https://github.com/newca12/TMDb-async-client/blob/master/src/main/scala/org/edla/tmdb/client/Usage.scala) for a runnable example.

Each function returns a `Future` of the response from the TMDb API, parsed into a convenient case class.

``` scala
  val apiKey = "REPLACE_THIS_WITH_YOUR_OWN_API_KEY"

  val tmdbClient = TmdbClient(apiKey)

  val movie = tmdbClient.getMovie(54181)

  val movies = tmdbClient.searchMovie("shark")

  tmdbClient.downloadPoster(movie, "/tmp/poster.jpg")

  tmdbClient.shutdown
```

### Developers ###

API Key need to be available as an environment variable named apiKey if you want to run sbt test

### License ###
© 2014-2016 Olivier ROLAND. Distributed under the GPLv3 License.

[1]: http://www.themoviedb.org/
[2]: http://doc.akka.io/docs/akka-stream-and-http-experimental/snapshot/scala.html
[3]: http://doc.akka.io/docs/akka-stream-and-http-experimental/snapshot/scala/http/common/json-support.html
[4]: http://doc.akka.io/docs/akka-stream-and-http-experimental/snapshot/scala/stream-cookbook.html#Globally_limiting_the_rate_of_a_set_of_streams