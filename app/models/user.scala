package models

case class User(
  _id: Id,
  age: Int,
  firstName: String,
  lastName: String,
  isActive: Boolean
)

case class Id($oid: String)

case class UserInsert(
                     age: Int,
                     firstName: String,
                     lastName: String,
                     isActive: Boolean
                     )

object JsonFormats {
  import play.api.libs.json.Json
  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val idFormat = Json.format[Id]
  implicit val userFormat = Json.format[User]
  implicit val userInsertFormat = Json.format[UserInsert]
}