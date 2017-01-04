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


package mathact.parts.ui

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Slider, Spinner, SpinnerValueFactory}
import scalafx.scene.layout.HBox

/** Set point
  * Created by CAB on 03.12.2016.
  */

class SetPointDouble(
  elemsHeight: Int,
  spinnerWidth: Int,
  sliderWidth: Int,
  minVal: Double,
  maxVal: Double,
  initVal: Double,
  valStep: Double,
  onChange: Double⇒Unit)
extends HBox(2){
  //Components
  val spinner: Spinner[Double]  = new Spinner[Double]{
    prefHeight = elemsHeight
    prefWidth = spinnerWidth
    style = "-fx-font-size: 11pt;"
    disable = true
    editable = true
    value.onChange{
      Option(slider).foreach(_.value = this.value.value)
      onChange(this.value.value)}
    valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minVal, maxVal, initVal, valStep)
      .asInstanceOf[SpinnerValueFactory[Double]]}
  val slider: Slider = new Slider{
    min = minVal
    max = maxVal
    value = initVal
    showTickLabels = true
    showTickMarks = true
    majorTickUnit = (maxVal - minVal).abs / 10
    minorTickCount = 4
    blockIncrement = valStep
    prefHeight = elemsHeight
    prefWidth = sliderWidth
    disable = true
    value.onChange{ spinner.valueFactory.value.setValue(this.value.value)}}
  //Construction
  alignment = Pos.Center
  padding = Insets(4.0)
  children = Seq(spinner, slider)
  //Methods
  def active(): Unit = {
    spinner.disable = false
    slider.disable = false}
  def passive(): Unit = {
    spinner.disable = true
    slider.disable = true}}




