package com.hkdsun.bookstore.adapter

import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.MongoConnection
import com.hkdsun.bookstore.config.Configuration

object MongoFactory extends Configuration {
  def getConnection: MongoConnection = return MongoConnection(dbHost)
  def getCollection(conn: MongoConnection): MongoCollection = return conn(dbName)(dbCollection)
  def closeConnection(conn: MongoConnection) { conn.close }
}


