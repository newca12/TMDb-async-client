package org.edla.tmdb.api

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object Protocol extends DefaultJsonProtocol {

  final case class AuthenticateResult(expires_at: String, request_token: String, success: Boolean)
  final case class ProductionCountry(iso_3166_1: String, name: String)
  final case class Release(iso_3166_1: String, certification: String, release_date: String)
  final case class Genre(id: Int, name: String)
  final case class ProductionCompanie(name: String, id: Int)
  final case class SpokenLanguage(iso_639_1: String, name: String)
  final case class Collection(poster_path: Option[String], id: Int, name: String, backdrop_path: Option[String])
  final case class Movie(
      runtime: Option[Int],
      status: String,
      backdrop_path: Option[String],
      overview: Option[String],
      title: String,
      vote_count: Int,
      tagline: Option[String],
      belongs_to_collection: Option[Collection],
      original_title: String,
      poster_path: Option[String],
      production_countries: List[ProductionCountry],
      revenue: Int,
      homepage: Option[String],
      imdb_id: Option[String],
      id: Int,
      release_date: Option[String],
      budget: Int,
      popularity: Double,
      genres: List[Genre],
      production_companies: List[ProductionCompanie],
      adult: Boolean,
      spoken_languages: List[SpokenLanguage]
  )
  final case class Result(
      original_title: String,
      poster_path: Option[String],
      release_date: Option[String],
      id: Int,
      adult: Boolean,
      title: String,
      popularity: Double,
      vote_count: Int,
      vote_average: Int,
      backdrop_path: Option[String]
  )
  final case class Results(total_results: Int, results: List[Result], page: Int, total_pages: Int)
  final case class Images(
      still_sizes: List[String],
      poster_sizes: List[String],
      base_url: String,
      profile_sizes: List[String],
      secure_base_url: String,
      logo_sizes: List[String],
      backdrop_sizes: List[String]
  )
  final case class Configuration(images: Images, change_keys: List[String])
  final case class Cast(
      cast_id: Int,
      character: String,
      credit_id: String,
      name: String,
      order: Int,
      profile_path: Option[String]
  )
  final case class Crew(
      credit_id: String,
      departement: Option[String],
      id: Int,
      job: String,
      name: String,
      profile_path: Option[String]
  )
  final case class Credits(id: Int, cast: List[Cast], crew: List[Crew])
  final case class Releases(id: Int, countries: List[Release])

  final case class Error(status_code: Int, status_message: String)

  val noCrew     = Crew("", None, 0, "", "Unknown", None)
  val unReleased = Release("", "", "Unknown")

  implicit val authenticateResultFormat: RootJsonFormat[AuthenticateResult] =
    jsonFormat3(AuthenticateResult)
  implicit val productionCountriesFormat: RootJsonFormat[ProductionCountry] =
    jsonFormat2(ProductionCountry)
  implicit val genreFormat: RootJsonFormat[Genre] = jsonFormat2(Genre)
  implicit val productionCompanieFormat: RootJsonFormat[ProductionCompanie] =
    jsonFormat2(ProductionCompanie)
  implicit val spokenLanguageFormat: RootJsonFormat[SpokenLanguage] =
    jsonFormat2(SpokenLanguage)
  implicit val collectionFormat: RootJsonFormat[Collection]       = jsonFormat4(Collection)
  implicit val movieFormat: RootJsonFormat[Movie]                 = jsonFormat22(Movie)
  implicit val resultFormat: RootJsonFormat[Result]               = jsonFormat10(Result)
  implicit val resultsFormat: RootJsonFormat[Results]             = jsonFormat4(Results)
  implicit val imagesFormat: RootJsonFormat[Images]               = jsonFormat7(Images)
  implicit val configurationFormat: RootJsonFormat[Configuration] = jsonFormat2(Configuration)
  implicit val castFormat: RootJsonFormat[Cast]                   = jsonFormat6(Cast)
  implicit val crewFormat: RootJsonFormat[Crew]                   = jsonFormat6(Crew)
  implicit val creditsFormat: RootJsonFormat[Credits]             = jsonFormat3(Credits)
  implicit val release: RootJsonFormat[Release]                   = jsonFormat3(Release)
  implicit val releases: RootJsonFormat[Releases]                 = jsonFormat2(Releases)
  implicit val error: RootJsonFormat[Error]                       = jsonFormat2(Error)
}
