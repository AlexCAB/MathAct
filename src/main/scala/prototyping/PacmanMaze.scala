package prototyping
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import mathact.utils.ui.UIParams
import scala.swing.{Graphics2D, Frame, Panel}


/**
 * Pacman maze.
 * Created by CAB on 07.09.2015.
 */

object PacmanMaze extends App{
  println("==== PacmanMaze ====")
  //
  object Params extends UIParams.Pacman{
    val squareSize:Int = 25
    val maze0       = ImageIO.read(getClass.getResource("/maze_0.png"))
    val maze1U      = ImageIO.read(getClass.getResource("/maze_1_u.png"))
    val maze1D      = ImageIO.read(getClass.getResource("/maze_1_d.png"))
    val maze1L      = ImageIO.read(getClass.getResource("/maze_1_l.png"))
    val maze1R      = ImageIO.read(getClass.getResource("/maze_1_r.png"))
    val maze2U      = ImageIO.read(getClass.getResource("/maze_2_u.png"))
    val maze2D      = ImageIO.read(getClass.getResource("/maze_2_d.png"))
    val maze2L      = ImageIO.read(getClass.getResource("/maze_2_l.png"))
    val maze2R      = ImageIO.read(getClass.getResource("/maze_2_r.png"))
    val maze3U      = ImageIO.read(getClass.getResource("/maze_3_u.png"))
    val maze3D      = ImageIO.read(getClass.getResource("/maze_3_d.png"))
    val maze3L      = ImageIO.read(getClass.getResource("/maze_3_l.png"))
    val maze3R      = ImageIO.read(getClass.getResource("/maze_3_r.png"))
    val maze4       = ImageIO.read(getClass.getResource("/maze_4.png"))
    val mazeV       = ImageIO.read(getClass.getResource("/maze_v.png"))
    val mazeH       = ImageIO.read(getClass.getResource("/maze_h.png"))
    val mazeE       = ImageIO.read(getClass.getResource("/maze_e.png"))
    val pacman      = ImageIO.read(getClass.getResource("/pacman.png"))
    val pacmanSU    = ImageIO.read(getClass.getResource("/pacman_s_u.png"))
    val pacmanSD    = ImageIO.read(getClass.getResource("/pacman_s_d.png"))
    val pacmanSL    = ImageIO.read(getClass.getResource("/pacman_s_l.png"))
    val pacmanSR    = ImageIO.read(getClass.getResource("/pacman_s_r.png"))
    val pacmanBU    = ImageIO.read(getClass.getResource("/pacman_b_u.png"))
    val pacmanBD    = ImageIO.read(getClass.getResource("/pacman_b_d.png"))
    val pacmanBL    = ImageIO.read(getClass.getResource("/pacman_b_l.png"))
    val pacmanBR    = ImageIO.read(getClass.getResource("/pacman_b_r.png"))
    val ghostB      = ImageIO.read(getClass.getResource("/ghost_b.png"))
    val ghostBU     = ImageIO.read(getClass.getResource("/ghost_b_u.png"))
    val ghostBD     = ImageIO.read(getClass.getResource("/ghost_b_d.png"))
    val ghostBL     = ImageIO.read(getClass.getResource("/ghost_b_l.png"))
    val ghostBR     = ImageIO.read(getClass.getResource("/ghost_b_r.png"))
    val ghostR      = ImageIO.read(getClass.getResource("/ghost_r.png"))
    val ghostRU     = ImageIO.read(getClass.getResource("/ghost_r_u.png"))
    val ghostRD     = ImageIO.read(getClass.getResource("/ghost_r_d.png"))
    val ghostRL     = ImageIO.read(getClass.getResource("/ghost_r_l.png"))
    val ghostRR     = ImageIO.read(getClass.getResource("/ghost_r_r.png"))
    val ghostW      = ImageIO.read(getClass.getResource("/ghost_w.png"))
    val ghostWU     = ImageIO.read(getClass.getResource("/ghost_w_u.png"))
    val ghostWD     = ImageIO.read(getClass.getResource("/ghost_w_d.png"))
    val ghostWL     = ImageIO.read(getClass.getResource("/ghost_w_l.png"))
    val ghostWR     = ImageIO.read(getClass.getResource("/ghost_w_r.png"))
    val pellet      = ImageIO.read(getClass.getResource("/pellet.png"))
    val powerPellet = ImageIO.read(getClass.getResource("/power_pellet.png"))}
  //
//  trait Square
//  case object W extends Square
//  case object o extends Square
//  case object G extends Square
//  case object P extends Square
//  //
//  val walls = List(
//    List(W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W),
//    List(W,o,o,o,o,o,o,o,W,o,o,o,o,o,o,W),
//    List(W,W,W,W,W,o,W,W,W,o,W,W,o,W,o,W),
//    List(W,o,o,G,W,o,o,o,o,o,o,o,o,W,o,W),
//    List(W,o,W,W,W,W,W,o,W,W,o,W,o,W,o,W),
//    List(W,o,o,o,o,W,o,P,o,W,o,W,G,W,o,W),
//    List(W,o,W,W,o,W,W,W,W,W,o,W,W,W,o,W),
//    List(W,o,o,o,o,o,o,o,o,o,o,o,o,o,o,W),
//    List(W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W))
  //
  val walls = List(
    List(true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true),
    List(true,false,false,false,false,false,false,false,true,false,false,false,false,false,false,true),
    List(true,true,true,true,true,false,true,true,true,false,true,true,false,true,false,true),
    List(true,false,false,false,true,false,false,false,false,false,false,false,false,true,false,true),
    List(true,false,true,true,true,true,true,false,true,true,false,true,false,true,false,true),
    List(true,false,false,false,false,true,false,false,false,true,false,true,false,true,false,true),
    List(true,false,true,true,false,true,true,true,true,true,false,true,true,true,false,true),
    List(true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true),
    List(true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true))
  //
  //Objects
  trait Position
  case object Jib extends Position
  case object Up extends Position
  case object Down extends Position
  case object Left extends Position
  case object Right extends Position
  //Classes
  trait MazeObj
  case class Pacman(x:Int, y:Int) extends MazeObj
  case class SmallPacman(x:Int, y:Int, pos:Position) extends MazeObj
  case class BigPacman(x:Int, y:Int, pos:Position) extends MazeObj
  case class Pellet(x:Int, y:Int) extends MazeObj
  case class PowerPellet(x:Int, y:Int) extends MazeObj
  case class BlueGhost(x:Int, y:Int, pos:Position) extends MazeObj
  case class RedGhost(x:Int, y:Int, pos:Position) extends MazeObj
  case class WhiteGhost(x:Int, y:Int, pos:Position) extends MazeObj
  //
  class Maze(uiParams:UIParams.Pacman, maze:List[List[Boolean]]) extends Panel{
    //Calc and set size
    private val n = walls.size       //Lines
    private val m = walls.head.size  //Columns
    private val imgW = m * uiParams.squareSize
    private val imgH = n * uiParams.squareSize
    preferredSize = new Dimension(imgW, imgH)
    //Build background (walls)
    private val backgroundImg = new BufferedImage(imgW,imgH,BufferedImage.TYPE_INT_ARGB)
    private val backgroundGraphics = backgroundImg.createGraphics()
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
      backgroundGraphics.drawImage(img, j * uiParams.squareSize, i * uiParams.squareSize, null)}
    backgroundGraphics.dispose()
    //Variables
    private val screanImg = new BufferedImage(
      backgroundImg.getColorModel,
      backgroundImg.copyData(null),
      backgroundImg.isAlphaPremultiplied, null)
    //Methods
    def update(objects:List[MazeObj]):Unit = {
      val graphics = screanImg.createGraphics()
      //Draw background
      graphics.drawImage(backgroundImg, 0, 0, null)
      //Draw objects
      objects.map{
        case Pacman(x,y)            ⇒ (x, y, uiParams.pacman)
        case SmallPacman(x,y,Up)    ⇒ (x, y, uiParams.pacmanSU)
        case SmallPacman(x,y,Down)  ⇒ (x, y, uiParams.pacmanSD)
        case SmallPacman(x,y,Left)  ⇒ (x, y, uiParams.pacmanSL)
        case SmallPacman(x,y,Right) ⇒ (x, y, uiParams.pacmanSR)
        case BigPacman(x,y,Up)      ⇒ (x, y, uiParams.pacmanBU)
        case BigPacman(x,y,Down)    ⇒ (x, y, uiParams.pacmanBD)
        case BigPacman(x,y,Left)    ⇒ (x, y, uiParams.pacmanBL)
        case BigPacman(x,y,Right)   ⇒ (x, y, uiParams.pacmanBR)
        case Pellet(x,y)            ⇒ (x, y, uiParams.pellet)
        case PowerPellet(x,y)       ⇒ (x, y, uiParams.powerPellet)
        case BlueGhost(x,y,Jib)     ⇒ (x, y, uiParams.ghostB)
        case BlueGhost(x,y,Up)      ⇒ (x, y, uiParams.ghostBU)
        case BlueGhost(x,y,Down)    ⇒ (x, y, uiParams.ghostBD)
        case BlueGhost(x,y,Left)    ⇒ (x, y, uiParams.ghostBL)
        case BlueGhost(x,y,Right)   ⇒ (x, y, uiParams.ghostBR)
        case RedGhost(x,y,Jib)      ⇒ (x, y, uiParams.ghostR)
        case RedGhost(x,y,Up)       ⇒ (x, y, uiParams.ghostRU)
        case RedGhost(x,y,Down)     ⇒ (x, y, uiParams.ghostRD)
        case RedGhost(x,y,Left)     ⇒ (x, y, uiParams.ghostRL)
        case RedGhost(x,y,Right)    ⇒ (x, y, uiParams.ghostRR)
        case WhiteGhost(x,y,Jib)    ⇒ (x, y, uiParams.ghostW)
        case WhiteGhost(x,y,Up)     ⇒ (x, y, uiParams.ghostWU)
        case WhiteGhost(x,y,Down)   ⇒ (x, y, uiParams.ghostWD)
        case WhiteGhost(x,y,Left)   ⇒ (x, y, uiParams.ghostWL)
        case WhiteGhost(x,y,Right)  ⇒ (x, y, uiParams.ghostWR)}
      .foreach{case(x, y, img) ⇒ graphics.drawImage(img, x, y, null)}
      graphics.dispose()}
    //Override methods
    override def paintComponent(g:Graphics2D) =
      g.drawImage(screanImg, 0, 0, size.getWidth.toInt, size.getHeight.toInt, null)}






  //
  class MyFrame extends Frame{
    //
    val maze = new Maze(Params, walls)
    //
    contents = maze
    override def closeOperation() = {System.exit(1)}
    //




    maze.update(List(
      BigPacman(25,12,Right),
      Pellet(50,12),
      BlueGhost(75,12,Right)






    ))










  }


  val m = new MyFrame
  m.visible = true





































}
