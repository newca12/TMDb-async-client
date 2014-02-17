package org.edla.tmdb

import spray.json.DefaultJsonProtocol

sealed abstract class TmdbResponse
case class AuthenticateResult(expires_at: String, request_token: String, success: Boolean) extends TmdbResponse
case class ProductionCountry(iso_3166_1: String, name: String)
case class Genre(id: Long, name: String)
case class ProductionCompanie(name: String, id: Long)
case class SpokenLanguage(iso_639_1: String, name: String)
case class Movie(runtime: Long, status: String, backdrop_path: Option[String], overview: String, title: String, vote_count: Long,
                 tagline: String, belongs_to_collection: Option[String], original_title: String, poster_path: String,
                 production_countries: List[ProductionCountry], revenue: Long, homepage: String, imdb_id: String, id: Long,
                 release_date: String, budget: Long, popularity: Double, genres: List[Genre],
                 production_companies: List[ProductionCompanie], adult: Boolean, spoken_languages: List[SpokenLanguage])
case class Result(original_title: String, poster_path: Option[String], release_date: String, id: Long, adult: Boolean, title: String,
                  popularity: Double, vote_count: Long, vote_average: Long, backdrop_path: Option[String])
case class Results(total_results: Long, results: List[Result], page: Long, total_pages: Long)
case class Images(still_sizes: List[String], poster_sizes: List[String], base_url: String, profile_sizes: List[String],
                  secure_base_url: String, logo_sizes: List[String], backdrop_sizes: List[String])
case class Configuration(images: Images, change_keys: List[String])

case class Error(status_code: Long, status_message: String) extends TmdbResponse

object Protocol extends DefaultJsonProtocol {
  implicit val authenticateResultFormat = jsonFormat3(AuthenticateResult)
  implicit val productionCountriesFormat = jsonFormat2(ProductionCountry)
  implicit val genreFormat = jsonFormat2(Genre)
  implicit val productionCompanieFormat = jsonFormat2(ProductionCompanie)
  implicit val spokenLanguageFormat = jsonFormat2(SpokenLanguage)
  implicit val movieFormat = jsonFormat22(Movie)
  implicit val resultFormat = jsonFormat10(Result)
  implicit val resultsFormat = jsonFormat4(Results)
  implicit val imagesFormat = jsonFormat7(Images)
  implicit val configurationFormat = jsonFormat2(Configuration)
  implicit val error = jsonFormat2(Error)
}