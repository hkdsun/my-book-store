package com.hkdsun.bookstore.config

import com.typesafe.config.ConfigFactory
import util.Try
import scala.collection.JavaConversions._

trait Configuration {
  val config = ConfigFactory.load()
  val homedir = sys.env("HOME")
  val supportExts = Try(config.getStringList("discovery.servedExtensions").toList).getOrElse(List[String]("mobi")).toSet
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val rootDirectory = Try(config.getString("discovery.rootDir")).getOrElse(homedir + "/.mybookstore")
  lazy val dbHost = Try(config.getString("db.host")).getOrElse("localhost")
  lazy val dbPort = Try(config.getInt("db.port")).getOrElse(3306)
  lazy val dbName = Try(config.getString("db.name")).getOrElse("bookstore")
  lazy val dbUser = Try(config.getString("db.user")).toOption.orNull
  lazy val dbPassword = Try(config.getString("db.password")).toOption.orNull
}
