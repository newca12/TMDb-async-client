# TMDb-async-client [![Build Status](https://travis-ci.org/newca12/TMDb-async-client.png?branch=master)](https://travis-ci.org/newca12/TMDb-async-client) [![Coverage Status](https://coveralls.io/repos/newca12/TMDb-async-client/badge.png)](https://coveralls.io/r/newca12/TMDb-async-client) [![Ohloh](https://www.ohloh.net/p/TMDb-async-client/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/TMDb-async-client)

### About ###
TMDb-async-client is a native Scala SDK that provides asynchronous access to the [The Movie Database][1] (TMDb) API.  
It is built heavily on [spray-client][2] for async non-blocking HTTP I/O and [spray-json][3] for parsing JSON responses into Scala case classes.

TMDb-async-client is an EDLA project.

The purpose of [edla.org](http://www.edla.org) is to promote the state of the art in various domains.

### API Key ###
You will need an API key to The Movie Database to access the API.  To obtain a key, follow these steps:

1. Register for and verify an [account](https://www.themoviedb.org/account/signup).
2. [Log](https://www.themoviedb.org/login) into your account
3. Select the API section on left side of your account page.
4. Click on the link to generate a new API key and follow the instructions.

### Usage

See [Usage.scala](https://github.com/newca12/TMDb-async-client/blob/master/src/main/scala/org/edla/tmdb/Usage.scala) for a runnable example.

Each function returns a `Future` of the response from the TMDb API, parsed into a convenient case class.

``` scala
  val apiKey = "REPLACE THIS WITH YOUR OWN API KEY"

  val tmdbClient = TmdbClient(apiKey)

  val movie = tmdbClient.getMovie(54181)

  val movies = tmdbClient.searchMovie("shark")

  tmdbClient.downloadPoster(movie, "/tmp/poster.jpg")

  tmdbClient.shutdown
```
### License ###
© 2014 Olivier ROLAND. Distributed under the GPLv3 License.

[1]: http://www.themoviedb.org/
[2]: http://spray.io/documentation/1.2.0/spray-client/
[3]: https://github.com/spray/spray-json