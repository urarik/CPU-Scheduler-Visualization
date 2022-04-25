package View

import Progress
import javafx.scene.paint.*
import javafx.scene.text.Font
import tornadofx.*
import java.awt.Point
import Process
import java.util.*

class GanttChart : View("My View") {
    val offset = 50
    override val root = form {
        prefWidth = 100.0
        prefHeight = 250.0
    }
    fun draw(progresses: List<Progress>, totalExecutionTime: Double, processes: List<Process>) {
        val treeMap = TreeMap<Double, Int>()

        if(processes.size > 20) {
            with(root) {
                clear()
                text("Too many processes!!") {
                    font = Font(30.0)
                }
            }
            return
        }
        var startPoint = Point(offset, 80)
        var currentExecutionTime = 0.0

        with(root) {
            clear()
            val length = (this.width.toInt() - offset) - startPoint.x
            val x = length/totalExecutionTime
            for (progress in progresses) {
                val width = (length - x * (totalExecutionTime - (progress.endTime - progress.startTime))).toInt()
                val x = offset + x*progress.startTime
                rectangle(x, startPoint.y, width, 100.0) {
                    fill = Color.TRANSPARENT
                    stroke = Color.BLACK
                    isManaged = false
                }
                text("p${progress.pId}") {
                    setX((2*x + width)/2.0 - 10)
                    setY(135.0)
                    font = Font(30.0)
                    fill = Color.RED
                    isManaged = false

                }
                text(progress.startTime.toString()) {
                    setX(x - 5)
                    setY(210.0)
                    font = Font(15.0)
                    isManaged = false
                }
                currentExecutionTime += progress.startTime
            }

            startPoint = Point(offset, 80)
            for(process in processes) {
                val x = offset + x*process.arrivalTime
                line {
                    startX = x
                    startY = 75.0
                    endX = x
                    endY = 85.0
                    isManaged = false

                }
                text(process.arrivalTime.toString()) {
                    setX(x - 3)
                    setY(65.0)
                    font = Font(15.0)
                    isManaged = false
                }
                text("p" + process.pId.toString()) {
                    val freq = treeMap.get(process.arrivalTime)
                    setX(x - 3)
                    if(freq == null) {
                        treeMap.put(process.arrivalTime, 1)
                        setY(50.0)
                    } else {
                        setY(50.0 - 20 * freq)
                        treeMap.put(process.arrivalTime, freq + 1)
                    }
                    font = Font(15.0)
                    isManaged = false
                }
            }
        }
        root.show()
    }
}
