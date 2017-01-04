/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2017 CAB      *
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

package mathact.core.layout.infrastructure

import javafx.geometry.Rectangle2D

import mathact.core.model.config.LayoutConfigLike
import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.model.holders.DriveRef
import collection.mutable.{ListBuffer ⇒ MutList}


/** Layout
  * Created by CAB on 04.11.2016.
  */

object Layout {
  //Definitions
  type WinID = Tuple2[DriveRef, Int]
  case class Rectangle(x: Double, y: Double, w: Double, h: Double)
  case class Point(x: Double, y: Double)
  case class Dimension(w: Double, h: Double)
  case class WindowData(
    drive: DriveRef,
    windowId: Int,
    preference: WindowPreference)
  case class WindowPosition(
    drive: DriveRef,
    windowId: Int,
    pos: Point)
  case class ScreenData(
    area: Rectangle,
    current: Point)
  trait LayoutCalcLike{
    val config: LayoutConfigLike
    val screenBounds: Rectangle2D
    val windows: Seq[WindowData]
    def evalAll(states: Map[WinID, WindowState]): List[WindowPosition]
    def evalGiven(states: Map[WinID, WindowState], drive: DriveRef, windowId: Int): WindowPosition}
  //Algorithms
  class FillAllScreenLayout(
    val config: LayoutConfigLike,
    val screenBounds: Rectangle2D,
    val windows: Seq[WindowData])
  extends LayoutCalcLike {
    //Variables
    private val locations = MutList[MutList[Rectangle]](MutList[Rectangle]())
    private var area = Rectangle(100, 100, 100, 100)
    //Functions
    private def intersects (r1: Rectangle, r2: Rectangle): Boolean = {
      var (tw, th, rw, rh) = (r1.w, r1.h, r2.w, r2.h)
      rw <= 0 || rh <= 0 || tw <= 0 || th <= 0 match{
        case true ⇒
          false
        case false ⇒
          val (tx, ty, rx, ry) = (r1.x, r1.y, r2.x, r2.y)
          rw += rx
          rh += ry
          tw += tx
          th += ty
          (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry)}}
    private def findFreeSpace(size: Dimension): (Int, Point) = {  //Return: layer number, Point
      val lastLayer = locations.last
      //If first component it the layer then return, else find free space
      if(lastLayer.isEmpty){
        (locations.size - 1, Point(area.x, area.y))}
      else{
        //Search new position
        val (mw,mh) = (area.w - size.w, area.h - size.h)
        var found = false
        var (cx,cy) = (area.x, area.y)
        var searchRect = new Rectangle(cx, cy, size.w, size.h)
        while(! found && cx < mw){
          cx += 10
          cy = area.y
          while(! found && cy < mh){
            cy += 10
            searchRect = searchRect.copy(cx, cy, searchRect.w, searchRect.h)
            found = ! lastLayer.exists(r ⇒ intersects(r, searchRect))}}
        //Return if found or new layer if not
        if(found){
          (locations.size - 1, Point(cx, cy))}
        else{
          (locations.size, new Point(area.x, area.y))}}}
    private def addLocation(layer: Int, point: Point, size: Dimension) = {
      //Add new layer if need
      if(locations.size == layer){ locations += MutList[Rectangle]()}
      //Add layout
      locations(layer) += new Rectangle(point.x, point.y, size.w, size.h)}
    //Methods
    private def occupyLocation(size: Dimension, setX: Option[Double], setY: Option[Double]): Point = {
      val (layer,point) = findFreeSpace(size)
      val pos = new Point(
        setX.getOrElse(point.x),
        setY.getOrElse(point.y))
      addLocation(layer, pos, size)
      pos}
    //Methods
    def evalAll(states: Map[WinID, WindowState]): List[WindowPosition] = {
      //Preparing
      locations.clear()
      locations += MutList[Rectangle]()
      area = Rectangle(
        config.screenIndent,
        config.screenIndent,
        screenBounds.getWidth - config.screenIndent * 2,
        screenBounds.getHeight - config.screenIndent * 2)
      //Calc for each
      val sortedWindows = states
        .filter(_._2.isShown)
        .toSeq.sortBy(_._2.title)
        .flatMap{ case ((d,i),_) ⇒ windows.find(w ⇒ w.drive == d && w.windowId == i) }
        .toList
        .reverse
      sortedWindows.flatMap{ window ⇒ states.get(Tuple2(window.drive, window.windowId)).map{ state ⇒
        WindowPosition(
          window.drive,
          window.windowId,
          occupyLocation(Dimension(state.w, state.h), window.preference.prefX, window.preference.prefY))}}}
    def evalGiven(states: Map[WinID, WindowState], drive: DriveRef, windowId: Int): WindowPosition = {
      states.get(Tuple2(drive, windowId)) match{
        case Some(state) ⇒
          WindowPosition(drive, windowId, occupyLocation(Dimension(state.w, state.h), None, None))
        case None ⇒
          WindowPosition(drive, windowId, Point(0, 0))}}}
  class WindowsStairsLayout(
    val config: LayoutConfigLike,
    val screenBounds: Rectangle2D,
    val windows: Seq[WindowData])
  extends LayoutCalcLike {
    //Variables
    private var currentScreenData = ScreenData(Rectangle(100, 100, 100, 100), Point(100, 100))
    //Functions
    private def evalNextPosition(screen: ScreenData): ScreenData = {
      screen.current.x < (screen.area.x + screen.area.w) && screen.current.y < (screen.area.y + screen.area.h) match {
        case true ⇒
          screen.copy(current = Point(screen.current.x + config.stairsStep, screen.current.y + config.stairsStep))
        case false ⇒
          screen.copy(current = Point(screen.area.x, screen.area.y))}}
    //Methods
    def evalAll(states: Map[WinID, WindowState]): List[WindowPosition] = {
      //Preparing
      val screen = ScreenData(
        Rectangle(
          config.screenIndent,
          config.screenIndent,
          screenBounds.getWidth - config.screenIndent * 2,
          screenBounds.getHeight - config.screenIndent * 2),
        Point(
          config.screenIndent,
          config.screenIndent))
      val sortedWindows = states
        .filter(_._2.isShown)
        .toSeq.sortBy(_._2.title)
        .flatMap{ case ((d,i),_) ⇒ windows.find(w ⇒ w.drive == d && w.windowId == i) }
        .reverse
      //Calc
      val (newScreen, allPositions) = sortedWindows
        .foldRight((screen, List[WindowPosition]())){ case (window, (sc, positions)) ⇒ (
          evalNextPosition(sc),
          positions :+ WindowPosition(window.drive, window.windowId, Point(sc.current.x, sc.current.y)))}
      currentScreenData = newScreen
      allPositions}
    def evalGiven(states: Map[WinID, WindowState], drive: DriveRef, windowId: Int): WindowPosition = {
      currentScreenData = evalNextPosition(currentScreenData)
      WindowPosition(drive, windowId, Point(currentScreenData.current.x, currentScreenData.current.y))}}}
