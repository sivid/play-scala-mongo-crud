package controllers

import javax.inject.Inject

import akka.actor.FSM.Failure
import play.api._
import play.api.mvc._
import play.api.libs.json._
import reactivemongo.api.Cursor
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{MongoController, ReactiveMongoComponents, ReactiveMongoApi}
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
// fixes "cannot find an implicit executioncontext"
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Success
import scala.concurrent.Future

// BSON-JSON conversions/collection
import play.modules.reactivemongo.json.collection._

class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {
  // https://www.playframework.com/documentation/2.4.x/ScalaActions
  // A Controller is nothing more than a singleton object that generates Action values.

  /*
   * Get a JSONCollection (a Collection implementation that is designed to work
   * with JsObject, Reads and Writes.)
   * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
   * the collection reference to avoid potential problems in development with
   * Play hot-reloading.
   */
  def collection: JSONCollection = db.collection[JSONCollection]("persons")
  import play.api.data.Form
  import models._
  import models.JsonFormats._

  def index = Action {
    Ok(views.html.index("This message came from Application.scala"))
  }

  def personsGetList = Action.async {
//    Ok("Got request [" + request + "]")
    val cursor: Cursor[User] = collection.
      find(Json.obj("isActive" -> true)). // find all
      sort(Json.obj("created" -> -1)). // sort
      cursor[User] // perform the query and get a cursor of JsObject

    // gather all JsObjects in a list
    val futureUsersList: Future[List[User]] = cursor.collect[List]()

    // transform the list into a JsArray
    val futurePersonsJsonArray: Future[JsArray] = futureUsersList.map { users =>
      Json.arr(users)
    }
    println("hi")
    // everything's ok! Let's reply with the array
    futurePersonsJsonArray.map { persons =>
      println(persons.toString())
      Ok(persons.toString)
    }
  }
  def personsUpdate(id: String) = Action.async(parse.json) { request =>
    request.body.validate[UserInsert].map { // I'm so surprised by myself
      user =>
        // find user by MongoDB object id
        val userSelector = Json.obj("_id" -> Json.obj("$oid" -> id))
        collection.update(userSelector, user).map {
          lastError =>
            println(s"Successfully updated with LastError: $lastError")
            Created(s"User Updated")
        }
    }.getOrElse(Future.successful(BadRequest("Invalid json")))
  }
  def personsDelete(id: String) = Action.async {
    // this only removes one entry at a time
    val userSelector = Json.obj("_id" -> Json.obj("$oid" -> id))
    println("deleting: " + id.isInstanceOf[String])
    collection.findAndRemove(userSelector).map {
      lastError =>
        println(s"Successfully deleted with LastError: $lastError")
        Created(s"User Deleted")
    }
  }

  def personsCreate = Action.async(parse.json) { request =>
    /*
     * request.body is a JsValue.
     * There is an implicit Writes that turns this JsValue as a JsObject,
     * so you can call insert() with this JsValue.
     * (insert() takes a JsObject as parameter, or anything that can be
     * turned into a JsObject using a Writes.)
     */
    println("request body is")
    println(request.body)
    request.body.validate[UserInsert].map { user =>
      // `user` is an instance of the case class `models.UserInsert`
      // we don't need findAndUpdate, because I say so
      collection.insert(user).map { lastError =>
        Logger.debug(s"Successfully inserted with LastError: $lastError")
        Created
      }
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }
}
