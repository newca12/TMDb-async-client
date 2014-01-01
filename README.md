# TMDb-async-client [![Build Status](https://buildhive.cloudbees.com/job/newca12/job/scala-atp/badge/icon)](https://buildhive.cloudbees.com/job/newca12/job/TMDb-async-client/) [![Ohloh](https://www.ohloh.net/p/TMDb-async-client/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/TMDb-async-client)

### About ###
TMDb-async-client is a native Scala SDK that provides asynchronous access to the [The Movie Database][1] (TMDb) API.  
It is built heavily on [spray-client][2] for async non-blocking HTTP I/O and [spray-json][3] for parsing JSON responses into Scala case classes.

TMDb-async-client is an EDLA project.

The purpose of [edla.org](http://www.edla.org) is to promote the state of the art in various domains.

This project is part of [EDLA](http://www.edla.org). 

### Usage

See [Usage.scala](https://github.com/newca12/TMDb-async-client/blob/master/src/main/scala/org/edla/tmdb/Usage.scala) for a runnable example.

Each function returns a `Future` of the response from the TMDb API, parsed into a convenient case class.

``` scala
  val apiKey = "REPLACE THIS WITH YOUR OWN API KEY"

  val tmdbClient = TmdbClient(apiKey)

  val movie = tmdbClient.getMovie(54181)

  val movies = tmdbClient.searchMovie("shark")

  tmdbClient.downloadPoster(movie, "/tmp/poster.jpg")

  //downloadPoster is async so we need to wait a little before shutdown
  //TODO : http://letitcrash.com/post/30165507578/shutdown-patterns-in-akka-2
  Thread.sleep(5000)

  tmdbClient.shutdown
```
### License ###
© 2014 Olivier ROLAND. Distributed under the GPLv3 License.

[1]: http://www.themoviedb.org/
[2]: http://spray.io/documentation/1.2.0/spray-client/
[3]: https://github.com/spray/spray-json