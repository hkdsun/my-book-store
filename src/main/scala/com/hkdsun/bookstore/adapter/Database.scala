package com.hkdsun.bookstore.adapter

import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.MongoClient
import com.hkdsun.bookstore.config.Configuration

object MongoFactory extends Configuration {
  val connection: MongoClient = MongoClient(dbHost)
  def getCollection(dbCollection: String): MongoCollection = connection(dbName)(dbCollection)
}

