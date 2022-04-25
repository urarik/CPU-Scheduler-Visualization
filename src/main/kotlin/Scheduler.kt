import View.MainView
import tornadofx.*

@ExperimentalStdlibApi
fun main(args: Array<String>) {
    launch<Scheduler>(args)
}

@ExperimentalStdlibApi
open class Scheduler: App(MainView::class)