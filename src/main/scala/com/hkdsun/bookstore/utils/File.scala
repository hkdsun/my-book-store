package com.hkdsun.bookstore.utils

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.{ EnumSet ⇒ JEnumSet }
import scala.collection.mutable.{ Set ⇒ MSet }
import com.hkdsun.bookstore.config.Configuration

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
    val name = file.getFileName.toString
    val ext = name.split('.').last
    if (supportExts.contains(ext))
      files += EbookFile(name, name.split('.').last, file)
    FileVisitResult.CONTINUE
  } catch {
    case _: Throwable ⇒ FileVisitResult.TERMINATE
  }
}
