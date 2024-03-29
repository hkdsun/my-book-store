package com.hkdsun.bookstore

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.{ EnumSet ⇒ JEnumSet }
import scala.collection.mutable.{ Set ⇒ MSet }

object FileTools {
  def getEbooks(str: String): Set[EbookFile] = {
    val path = Paths.get(str).normalize().toAbsolutePath()
    val visitor = new EbookFileVisitor()
    Files.walkFileTree(path, JEnumSet.of(FileVisitOption.FOLLOW_LINKS), 50, visitor)
    visitor.files.toSet
  }
}

case class EbookFile(filename: String, ext: String, abspath: Path)

class EbookFileVisitor extends SimpleFileVisitor[Path] with Configuration {
  val files = MSet.empty[EbookFile]

  override def visitFile(file: Path, attrs: BasicFileAttributes) = try {
    val fullname = file.getFileName.toString
    val name = fullname.split('.').dropRight(1).mkString(".")
    val ext = fullname.split('.').last
    if (supportExts.contains(ext))
      files += EbookFile(name, name.split('.').last, file)
    FileVisitResult.CONTINUE
  } catch {
    case _: Throwable ⇒ FileVisitResult.TERMINATE
  }
}
