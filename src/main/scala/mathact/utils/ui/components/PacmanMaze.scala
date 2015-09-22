package mathact.utils.ui.components
import java.awt.Dimension
import java.awt.image.BufferedImage
import mathact.utils.definitions.PacmanObj
import mathact.utils.definitions.Move
import mathact.utils.definitions.PacmanImg
import mathact.utils.ui.UIParams
import scala.swing._


/**
 * Display and update Pacman maze and maze objects.
 * Created by CAB on 09.09.2015.
 */

class PacmanMaze(
  uiParams:UIParams.Pacman,
  width:Int,
  height:Int)
extends Panel with UIComponent{
  //Variables
  private var n = 0
  private var m = 0
  private var backgroundImg:Option[BufferedImage] = None
  private var screenImg:Option[BufferedImage] = None
  //Methods
  def setWalls(walls:List[List[Boolean]]):Unit = {
    //Calc and set size
    n = walls.size       //Lines
    m = walls.head.size  //Columns
    val imgW = m * uiParams.squareSize
    val imgH = n * uiParams.squareSize
    preferredSize = new Dimension(
      if(width == Int.MaxValue) imgW else width,
      if(height == Int.MaxValue) imgH else height)
    //Build background (walls)
    val img = new BufferedImage(imgW,imgH,BufferedImage.TYPE_INT_ARGB)
    val graphics = img.createGraphics()
    for(i ← 0 until n; j ← 0 until m){
      val img = walls(i)(j) match{
        case true ⇒ {
          val u = i != 0 && walls(i - 1)(j)
          val d = i < (n - 1) && walls(i + 1)(j)
          val l = j != 0 && walls(i)(j - 1)
          val r = j < (m - 1) && walls(i)(j + 1)
          (u,d,l,r) match{
            case (true,true,true,true)    ⇒ uiParams.maze4
            case (false,true,true,true)   ⇒ uiParams.maze3D
            case (true,false,true,true)   ⇒ uiParams.maze3U
            case (true,true,false,true)   ⇒ uiParams.maze3R
            case (true,true,true,false)   ⇒ uiParams.maze3L
            case (false,true,false,true)  ⇒ uiParams.maze2D
            case (false,true,true,false)  ⇒ uiParams.maze2L
            case (true,false,false,true)  ⇒ uiParams.maze2U
            case (true,false,true,false)  ⇒ uiParams.maze2R
            case (true,false,false,false) ⇒ uiParams.maze1U
            case (false,true,false,false) ⇒ uiParams.maze1D
            case (false,false,true,false) ⇒ uiParams.maze1L
            case (false,false,false,true) ⇒ uiParams.maze1R
            case (false,false,true,true)  ⇒ uiParams.mazeH
            case (true,true,false,false)  ⇒ uiParams.mazeV
            case _ ⇒ uiParams.maze0}}
        case _ ⇒ uiParams.mazeE
      }
      graphics.drawImage(img, j * uiParams.squareSize, i * uiParams.squareSize, null)}
    graphics.dispose()
    //Set background image and create screen image
    backgroundImg = Some(img)
    screenImg = Some(new BufferedImage(
      img.getColorModel,
      img.copyData(null),
      img.isAlphaPremultiplied, null))}
  def update(objects:List[PacmanObj]):Unit = (backgroundImg, screenImg) match{
    case (Some(background), Some(screen)) ⇒ {
      val graphics = screen.createGraphics()
      //Draw background
      graphics.drawImage(background, 0, 0, null)
      //Draw objects
      objects.flatMap{
        case PacmanObj.ImgObj(x,y,Move.Up,PacmanImg.Pacman0)        ⇒ Some(x, y, uiParams.pacmanBU)
        case PacmanObj.ImgObj(x,y,Move.Down,PacmanImg.Pacman0)      ⇒ Some(x, y, uiParams.pacmanBD)
        case PacmanObj.ImgObj(x,y,Move.Left,PacmanImg.Pacman0)      ⇒ Some(x, y, uiParams.pacmanBL)
        case PacmanObj.ImgObj(x,y,Move.Right,PacmanImg.Pacman0)     ⇒ Some(x, y, uiParams.pacmanBR)
        case PacmanObj.ImgObj(x,y,Move.Stay,PacmanImg.Pacman0)      ⇒ Some(x, y, uiParams.pacmanBR)
        case PacmanObj.ImgObj(x,y,Move.Up,PacmanImg.Pacman1)        ⇒ Some(x, y, uiParams.pacmanSU)
        case PacmanObj.ImgObj(x,y,Move.Down,PacmanImg.Pacman1)      ⇒ Some(x, y, uiParams.pacmanSD)
        case PacmanObj.ImgObj(x,y,Move.Left,PacmanImg.Pacman1)      ⇒ Some(x, y, uiParams.pacmanSL)
        case PacmanObj.ImgObj(x,y,Move.Right,PacmanImg.Pacman1)     ⇒ Some(x, y, uiParams.pacmanSR)
        case PacmanObj.ImgObj(x,y,Move.Stay,PacmanImg.Pacman1)      ⇒ Some(x, y, uiParams.pacmanSR)
        case PacmanObj.ImgObj(x,y,_,PacmanImg.Pacman2)              ⇒ Some(x, y, uiParams.pacman)
        case PacmanObj.Pellet(x,y)                                  ⇒ Some(x, y, uiParams.pellet)
        case PacmanObj.PowerPellet(x,y)                             ⇒ Some(x, y, uiParams.powerPellet)
        case PacmanObj.ImgObj(x,y,Move.Stay,PacmanImg.RedGhost)     ⇒ Some(x, y, uiParams.ghostB)
        case PacmanObj.ImgObj(x,y,Move.Up,PacmanImg.RedGhost)       ⇒ Some(x, y, uiParams.ghostBU)
        case PacmanObj.ImgObj(x,y,Move.Down,PacmanImg.RedGhost)     ⇒ Some(x, y, uiParams.ghostBD)
        case PacmanObj.ImgObj(x,y,Move.Left,PacmanImg.RedGhost)     ⇒ Some(x, y, uiParams.ghostBL)
        case PacmanObj.ImgObj(x,y,Move.Right,PacmanImg.RedGhost)    ⇒ Some(x, y, uiParams.ghostBR)
        case PacmanObj.ImgObj(x,y,Move.Stay,PacmanImg.BlueGhost)    ⇒ Some(x, y, uiParams.ghostR)
        case PacmanObj.ImgObj(x,y,Move.Up,PacmanImg.BlueGhost)      ⇒ Some(x, y, uiParams.ghostRU)
        case PacmanObj.ImgObj(x,y,Move.Down,PacmanImg.BlueGhost)    ⇒ Some(x, y, uiParams.ghostRD)
        case PacmanObj.ImgObj(x,y,Move.Left,PacmanImg.BlueGhost)    ⇒ Some(x, y, uiParams.ghostRL)
        case PacmanObj.ImgObj(x,y,Move.Right,PacmanImg.BlueGhost)   ⇒ Some(x, y, uiParams.ghostRR)
        case PacmanObj.ImgObj(x,y,Move.Stay,PacmanImg.WhiteGhost)   ⇒ Some(x, y, uiParams.ghostW)
        case PacmanObj.ImgObj(x,y,Move.Up,PacmanImg.WhiteGhost)     ⇒ Some(x, y, uiParams.ghostWU)
        case PacmanObj.ImgObj(x,y,Move.Down,PacmanImg.WhiteGhost)   ⇒ Some(x, y, uiParams.ghostWD)
        case PacmanObj.ImgObj(x,y,Move.Left,PacmanImg.WhiteGhost)   ⇒ Some(x, y, uiParams.ghostWL)
        case PacmanObj.ImgObj(x,y,Move.Right,PacmanImg.WhiteGhost)  ⇒ Some(x, y, uiParams.ghostWR)
        case PacmanObj.ImgObj(_,_,_,PacmanImg.EmptyImg) ⇒ None}
        .foreach{case(x, y, img) ⇒ {
        val ix = (x * uiParams.squareSize).toInt
        val iy = (y * uiParams.squareSize).toInt
        val iw = img.getWidth
        val ih = img.getHeight
        val sh = uiParams.squareSize / 2
        graphics.drawImage(img, ix - (iw / 2) + sh, iy - (ih / 2) + sh, null)}}
      //Dispose and update
      graphics.dispose()
      repaint()
      revalidate()}
    case _ ⇒}
  def getN:Int = n
  def getM:Int = m
  //Override methods
  override def paintComponent(g:Graphics2D) = screenImg.foreach(img ⇒
    g.drawImage(img, 0, 0, size.getWidth.toInt, size.getHeight.toInt, null))}
