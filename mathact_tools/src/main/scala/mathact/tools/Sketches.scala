/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2016 CAB      *
 *          # #      # #                                  #  #                     *
 *         #  #    #  #           # #     # #           #     #              # #   *
 *        #   #  #   #             #       #          #        #              #    *
 *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
 *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
 *     #          #   # # # #   #       #      #  #           #  #         #       *
 *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
 *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
 * @                                                                             @ *
\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */

package mathact.tools

import mathact.core.app.Application
import mathact.core.bricks.blocks.WorkbenchLike
import mathact.core.bricks.data.SketchData

import scala.reflect._

import scala.collection.mutable.{ArrayBuffer => MutList}

/** Base class for sketch list object
  * Created by CAB on 13.10.2016.
  */

class Sketches extends Application{
  //Variables
  private val rawSketchList = MutList[SketchDsl]()
  //DSL
  protected class SketchDsl(
    clazz: Class[_],
    sName: Option[String],
    sDesc: Option[String],
    isAutorun: Boolean,
    showLogging: Boolean,
    showVisualisation: Boolean)
  {
    //Add to list
    rawSketchList += this
    //Methods
    def name(n: String): SketchDsl =
      new SketchDsl(clazz, n match{case "" ⇒ None case _ ⇒ Some(n)}, sDesc, isAutorun, showLogging, showVisualisation)
    def description(s: String): SketchDsl =
      new SketchDsl(clazz, sName, s match{case "" ⇒ None case _ ⇒ Some(s)}, isAutorun, showLogging, showVisualisation)
    def autorun:  SketchDsl =
      new SketchDsl(clazz, sName, sDesc, true, showLogging, showVisualisation)
    def logging:  SketchDsl =
      new SketchDsl(clazz, sName, sDesc, isAutorun, true, showVisualisation)
    def visualisation:  SketchDsl =
      new SketchDsl(clazz, sName, sDesc, isAutorun, showLogging, true)
    //Convert
    private[mathact] def toSketchData =
      SketchData(clazz, clazz.getTypeName, sName, sDesc, isAutorun, showLogging, showVisualisation)}
  protected def sketchOf[T <: WorkbenchLike : ClassTag]: SketchDsl =
    new SketchDsl(classTag[T].runtimeClass,None,None,false,false,false)
  object SketchOf{
    def apply[T <: WorkbenchLike : ClassTag](
      name: String = "",
      description: String = "",
      autorun: Boolean = false,
      logger:Boolean = false,
      visualisation: Boolean = false)
    : Unit = {
       rawSketchList  += new SketchDsl(
         clazz = classTag[T].runtimeClass,
         sName = name match{case "" ⇒ None case _ ⇒ Some(name)},
         sDesc = description match{case "" ⇒ None case _ ⇒ Some(description)},
         isAutorun = autorun,
         showLogging = logger,
         showVisualisation = visualisation)}}
  //Methods
  private[mathact] def sketchList: List[SketchData] = rawSketchList
    .toList
    .map(_.toSketchData)
    .foldRight(List[SketchData]()){
      case (s,l) if l.exists(_.className == s.className) ⇒ l
      case (s,l) ⇒ s +: l}}
