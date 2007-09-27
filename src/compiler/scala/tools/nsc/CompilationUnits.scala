/* NSC -- new Scala compiler
 * Copyright 2005-2006 LAMP/EPFL
 * @author  Martin Odersky
 */
// $Id$

package scala.tools.nsc

import scala.tools.nsc.util.{FreshNameCreator,OffsetPosition,Position,SourceFile}
import scala.tools.nsc.io.AbstractFile
import scala.collection.mutable.HashSet

trait CompilationUnits { self: Global =>

  /** One unit of compilation that has been submitted to the compiler.
    * It typically corresponds to a single file of source code.  It includes
    * error-reporting hooks.  */
  class CompilationUnit(val source: SourceFile) extends CompilationUnitTrait {
    /** the fresh name creator */
    var fresh : FreshNameCreator = new FreshNameCreator.Default

    /** the content of the compilation unit in tree form */
    var body: Tree = EmptyTree

    /** Note: depends now contains toplevel classes.
     *  To get their sourcefiles, you need to dereference with .sourcefile
     */
    val depends = new HashSet[Symbol]

    /** used to track changes in a signature */
    var pickleHash : Long = 0

    def position(pos: Int) = source.position(pos)

    /** The icode representation of classes in this compilation unit.
     *  It is empty up to phase 'icode'.
     */
    val icode: HashSet[icodes.IClass] = new HashSet

    val errorPositions = new HashSet[Position]

    def error(pos: Position, msg: String) =
      if (inIDE || !(errorPositions contains pos)) {
        if (!inIDE) errorPositions += pos
        reporter.error((pos), msg)
      }

    def warning(pos: Position, msg: String) =
      if (inIDE || !(errorPositions contains pos)) {
        if (!inIDE) errorPositions += pos
        reporter.warning((pos), msg)
      }

    def deprecationWarning(pos: Position, msg: String) =
      if (settings.deprecation.value) warning(pos, msg)
      else currentRun.deprecationWarnings = true

    def uncheckedWarning(pos: Position, msg: String) =
      if (settings.unchecked.value) warning(pos, msg)
      else currentRun.uncheckedWarnings = true

    def incompleteInputError(pos: Position, msg:String) =
      if (inIDE || !(errorPositions contains pos)) {
        val hadErrors = !errorPositions.isEmpty
        if (!hadErrors)
          reporter.incompleteInputError((pos), msg)
        else reporter.error((pos), msg)
        if (!inIDE) errorPositions += pos
      }

    override def toString() = source.toString()

    def clear() = {
      fresh = null
      body = null
      depends.clear
      errorPositions.clear
    }
  }
}


