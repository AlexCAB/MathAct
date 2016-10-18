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
import mathact.core.bricks.WorkbenchLike
import mathact.core.model.data.sketch.SketchData

import scala.reflect._

import scala.collection.mutable.{ArrayBuffer => MutList}

/** Base class for sketch list object
  * Created by CAB on 13.10.2016.
  */

class Sketches extends Application{
  //Variables
  private val rawSketchList = MutList[SketchDsl]()
  //DSL
  class SketchDsl(
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
      SketchData(clazz, clazz.getCanonicalName, sName, sDesc, isAutorun, showLogging, showVisualisation)}
  def sketchOf[T <: WorkbenchLike : ClassTag]: SketchDsl =
    new SketchDsl(classTag[T].runtimeClass,None,None,false,false,false)
  //Methods
  def sketchList: List[SketchData] = rawSketchList
    .toList
    .map(_.toSketchData)
    .foldRight(List[SketchData]()){
      case (s,l) if l.exists(_.className == s.className) ⇒ l
      case (s,l) ⇒ s +: l}}