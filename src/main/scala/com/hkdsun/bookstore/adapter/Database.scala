package com.hkdsun.bookstore

import reactivemongo.api._
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.ExecutionContext.Implicits.global

final class MongoDB extends Configuration {
  val driver = new MongoDriver
  val connection = driver.connection(List(dbHost))
  val db = connection(dbName)
}

object MongoDB {
  def apply(collection: String) = {
    val mongo = new MongoDB()
    val db = mongo.db[BSONCollection](collection)
    sys.addShutdownHook {
      mongo.driver.close()
    }
    db
  }
}