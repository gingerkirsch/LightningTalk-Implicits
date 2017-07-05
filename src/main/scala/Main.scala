object What {

  def greet(who: String) =
    "Hello " + who

  def main(args: Array[String]): Unit = {
    val greetTheWorld = greet("World")
    println(greetTheWorld)
  }

}

object What2 {
  def greet(implicit who: String) =
    "Hello " + who

  def main(args: Array[String]): Unit = {
    implicit val world: String = "World"
    val greetTheWorld = greet
    println(greetTheWorld)
  }
}

object What3 {
  def greet(implicit who: String) =
    "Hello " + who

  def main(args: Array[String]): Unit = {
    implicit val world: String = "World"
//    implicit val trudy: String = "Trudy"
    val greetTheWorld = greet
    println(greetTheWorld)
  }
}

object Why {
  import scala.concurrent.Future

  def main(args: Array[String]): Unit = {
    val executionContext = scala.concurrent.ExecutionContext.global
    val futureMessage = "Think, McFly! Think!"
    Future(println(futureMessage))(executionContext)

    java.lang.Thread.sleep(1000)
  }
}

object Why2 {
  import scala.concurrent.Future

  def main(args: Array[String]): Unit = {
    implicit val executionContext = scala.concurrent.ExecutionContext.global
    val futureMessage =
      "If my calculations are correct, when this baby hits 88 miles per hour...\n" +
        "You're gonna see some serious shit!"
    //def apply[T](body: =>T)(implicit executor: ExecutionContext): Future[T]
    Future(println(futureMessage))

    java.lang.Thread.sleep(1000)
  }
}

object Why3 {
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
    val futureMessage1 =
      "Hey, Doc, we better back up. We don't have enough road to get up to 88."
    val futureMessage2 =
      "Roads? Where we're going, we don't need roads."

    Future(println(futureMessage1))
    Future(println(futureMessage2))

    java.lang.Thread.sleep(1000)
  }
}

trait WsCalls {
  import akka.actor.ActorSystem
  import akka.stream.ActorMaterializer
  import play.api.libs.ws.ahc._

  implicit val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }
  implicit val materializer = ActorMaterializer()

  val wsClient = StandaloneAhcWSClient()
}

object Why4 extends WsCalls {
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.libs.ws.StandaloneWSResponse

  def main(args: Array[String]): Unit = {
    val lisbonWeatherUrl =
      "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

    val weatherCall: Future[StandaloneWSResponse] = wsClient.url(lisbonWeatherUrl).get()
    // ".map" needs an implicit ExecutionContext
    weatherCall.map { response =>
      println(response.body)
    }

    weatherCall.onComplete(_ => system.terminate())
  }
}

object Why5 extends WsCalls {
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.libs.ws.JsonBodyReadables._
  import play.api.libs.json.JsValue

  def main(args: Array[String]): Unit = {
    val lisbonWeatherUrl =
      "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

    val weatherCall = wsClient.url(lisbonWeatherUrl).get()
    weatherCall.map { response =>
      // ".body[T]" needs an implicit BodyReabable[T]
      val jsValue = response.body[JsValue]

      val temperature = jsValue \ "main" \ "temp"

      println(temperature)
    }

    weatherCall.onComplete(_ => system.terminate())
  }
}

object Why6 extends WsCalls {
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.libs.ws.JsonBodyReadables._
  import play.api.libs.json.JsValue

  def main(args: Array[String]): Unit = {
    val lisbonWeatherUrl =
      "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

    val weatherCall = wsClient.url(lisbonWeatherUrl).get()
    weatherCall.map { response =>
      val jsValue = response.body[JsValue]

      // ".as[T]" needs an implicit Reads[T], available in DefaultReads
      val temperature = (jsValue \ "main" \ "temp").as[Double]

      println(temperature)
    }

    weatherCall.onComplete(_ => system.terminate())
  }
}

object HowClasses {
  import play.api.libs.json._

  case class WeatherResponse(coord: Coordinates, main: WeatherData, wind: Wind, visibility: Int)
  case class Coordinates(lon: Double, lat: Double)
  case class WeatherData(temp: Double, pressure: Int, humidity: Int, temp_min: Double, temp_max: Double)
  case class Wind(speed: Double, deg: Int)

  implicit val windReads = Json.reads[Wind]
  implicit val weatherDataReads = Json.reads[WeatherData]
  implicit val coordinatesReads = Json.reads[Coordinates]
  implicit val weatherResponseReads = Json.reads[WeatherResponse]
}
object How extends WsCalls {
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.libs.ws.JsonBodyReadables._
  import play.api.libs.json._
  import HowClasses._

  def main(args: Array[String]): Unit = {
    val lisbonWeatherUrl =
      "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

    val weatherCall = wsClient.url(lisbonWeatherUrl).get()
    weatherCall.map { response =>
      val jsValue = response.body[JsValue]

      // ".as[T]" needs an implicit Reads[T]
      val weatherResponse = jsValue.as[WeatherResponse]
      val temperature = weatherResponse.main.temp

      println(temperature)
    }

    weatherCall.onComplete(_ => system.terminate())
  }
}

/**
def main(args: Array[String]): Unit = {
  val lisbonWeatherUrl =
    "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

  val weatherCall = wsClient.url(lisbonWeatherUrl).get()
  weatherCall.parsing { jsValue =>
    val weatherResponse = jsValue.as[WeatherResponse]
    val temperature = weatherResponse.main.temp

    println(temperature)
  }

  weatherCall.onComplete(_ => system.terminate())
}
**/

object How2 extends WsCalls {
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.libs.ws.StandaloneWSResponse
  import play.api.libs.ws.JsonBodyReadables._
  import play.api.libs.json._
  import HowClasses._

  implicit class PimpMyFuture(apiCall: Future[StandaloneWSResponse]) {
    def parsing(f: JsValue => Unit): Future[Unit] = {
      apiCall.map { response =>
        val jsValue = response.body[JsValue]
        f(jsValue)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val lisbonWeatherUrl =
      "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

    val weatherCall = wsClient.url(lisbonWeatherUrl).get()
    weatherCall.parsing { jsValue =>
      val weatherResponse = jsValue.as[WeatherResponse]
      val temperature = weatherResponse.main.temp

      println(temperature)
    }

    weatherCall.onComplete(_ => system.terminate())
  }
}

/**
def main(args: Array[String]): Unit = {
  val lisbonWeatherUrl =
    "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

  val weatherCall = wsClient.url(lisbonWeatherUrl).get()
  weatherCall.parsing[WeatherResponse] { weatherResponse =>
    val temperature = weatherResponse.main.temp

    println(temperature)
  }

  weatherCall.onComplete(_ => system.terminate())
}
**/

object How3 extends WsCalls {
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.libs.ws.StandaloneWSResponse
  import play.api.libs.ws.JsonBodyReadables._
  import play.api.libs.json._
  import HowClasses._

  implicit class PimpMyFuture(apiCall: Future[StandaloneWSResponse]) {
    def parsing[T](f: T => Unit)(implicit reads: Reads[T]): Future[Unit] = {
      apiCall.map { response =>
        val jsValue = response.body[JsValue]
        val parsed = jsValue.as[T]
        f(parsed)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val lisbonWeatherUrl =
      "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

    val weatherCall = wsClient.url(lisbonWeatherUrl).get()
    weatherCall.parsing[WeatherResponse] { weatherResponse =>
      val temperature = weatherResponse.main.temp

      println(temperature)
    }

    weatherCall.onComplete(_ => system.terminate())
  }
}

/**
def main(args: Array[String]): Unit = {
  val lisbonWeatherUrl =
    "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

  getAndParse[WeatherResponse](lisbonWeatherUrl) { weatherResponse =>
    val temperature = weatherResponse.main.temp

    println(temperature)
    system.terminate()
  }
}
**/

object How4 extends WsCalls {
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.libs.ws.JsonBodyReadables._
  import play.api.libs.json._
  import HowClasses._

  def getAndParse[T](url: String)(f: T => Unit)(implicit reads: Reads[T]): Future[Unit] = {
    val weatherCall = wsClient.url(url).get()
    weatherCall.map { response =>
      val jsValue = response.body[JsValue]

      // ".as[T]" needs an implicit Reads[T]
      val parsed = jsValue.as[T]

      f(parsed)
    }
  }

  def main(args: Array[String]): Unit = {
    val lisbonWeatherUrl =
      "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

    getAndParse[WeatherResponse](lisbonWeatherUrl) { weatherResponse =>
      val temperature = weatherResponse.main.temp

      println(temperature)
      system.terminate()
    }
  }
}

/**
  * Why not have the format generic as well?
  */

object How5 extends WsCalls {
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.libs.ws.BodyReadable
  import play.api.libs.json._
  import HowClasses._

  trait Encoding[Format, T] {
    def parse(instance: Format): T
  }

  implicit def jsonEncoding[T](implicit reads: Reads[T]) = new Encoding[JsValue, T] {
    def parse(instance: JsValue): T = instance.as[T]
  }

  def getAndParse[Format, T](url: String)(f: T => Unit)(implicit encoding: Encoding[Format, T], bodyReader: BodyReadable[Format]): Future[Unit] = {
    val weatherCall = wsClient.url(url).get()
    weatherCall.map { response =>
      val parsedFormat = response.body[Format]

      val parsedData = encoding.parse(parsedFormat)

      f(parsedData)
    }
  }

  def main(args: Array[String]): Unit = {
    import play.api.libs.ws.JsonBodyReadables._
    val lisbonWeatherUrl =
      "http://api.openweathermap.org/data/2.5/weather?q=Lisbon&appid=82c6c97edabb170ecedf19c23f069e2c"

    getAndParse[JsValue, WeatherResponse](lisbonWeatherUrl) { weatherResponse =>
      val temperature = weatherResponse.main.temp

      println(temperature)
      system.terminate()
    }
  }
}