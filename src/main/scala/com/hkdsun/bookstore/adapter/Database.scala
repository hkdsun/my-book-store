package com.hkdsun.bookstore.adapter

import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.MongoClient
import com.hkdsun.bookstore.config.Configuration

object MongoFactory extends Configuration {
  def getConnection: MongoClient = MongoClient(dbHost)
  def getCollection(dbCollection: String): MongoCollection = getConnection(dbName)(dbCollection)
  def closeConnection(conn: MongoClient) = conn.close
}

