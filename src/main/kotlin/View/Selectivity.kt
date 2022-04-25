package View

import javafx.scene.chart.NumberAxis
import tornadofx.View
import tornadofx.form
import tornadofx.linechart
import tornadofx.series
import tornadofx.*

class Selectivity : View("My View") {
    override val root = form {
        prefWidth = 250.0
        prefHeight = 250.0
    }

    fun draw(list: List<Int>, name: String) {
        val items = mutableMapOf<Int, Int>()
        for(i in list.indices)
            items.put(i, list[i])
        with(root) {
            clear()
            linechart("Process Distribution", NumberAxis(), NumberAxis()) {
                series(name) {
                    for (i in list.indices)
                        data(i, list[i])
                }
            }

        }

    }
}