package View

import Process
import ProcessTokenizer
import Result
import SchedulingAlgorithms.*
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*
import java.io.File


@ExperimentalStdlibApi
class MainView : View() {
    private val ganttChart = find(GanttChart::class)
    private val selectivity = find(Selectivity::class)
    private val labels = mutableListOf<Label>()
    private val texts = mutableListOf<TextField>()
    lateinit var timeUnitText: TextField
    lateinit var hboxForAdd: HBox
    lateinit var hboxForMine: HBox
    lateinit var alphaOrQuantumText: Label
    lateinit var alphaOrQuantumTextField: TextField
    lateinit var tauText: Label
    lateinit var tauOrCountTextField: TextField
    lateinit var mineAlphaText: Label
    lateinit var mineAlphaTextField: TextField
    lateinit var mineTauText: Label
    lateinit var mineTauTextField: TextField

    lateinit var file: List<File>
    lateinit var schedType: ComboBox<String>
    lateinit var processTable: TableView<Process>
    lateinit var resultTable: TableView<Result>

    //var processes: List<Process> = emptyList()
    val processes = FXCollections.observableArrayList<Process>()
    val results = FXCollections.observableArrayList<Result>()
    //tabpane
    override val root = HBox()

    init {
        with(root) {
            vbox {
                hbox {
                    schedType = combobox {
                        items = listOf(
                            "FCFS",
                            "NonPreemptiveSJF",
                            "NonPreemptiveSJF with Exponential Averaging",
                            "PreemptiveSJF",
                            "Round Robin",
                            "HRRN",
                            "Round Robin in MS-Windows",
                            "Mine"
                        ).asObservable()
                        style {
                            font = Font(13.0)
                        }
                        selectionModel.selectFirst()
                    }
                    label("Time unit for throughput") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(20.0)
                    }
                    timeUnitText = textfield() {
                        filterInput { change ->
                            !change.isAdded || change.controlNewText.let {
                                it.isInt()
                            }
                        }
                        text = "10"
                        font = Font(20.0)
                    }
                }
                hboxForAdd = hbox {
                    alphaOrQuantumText = label("alpha(0.0 ~ 1.0)") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(20.0)
                    }
                    alphaOrQuantumTextField = textfield() {
                        filterInput { change ->
                            if (schedType.value == "NonPreemptiveSJF with Exponential Averaging")
                                !change.isAdded || change.controlNewText.let {
                                    it.isDouble() && it.toDouble() in 0.0..1.0
                                } else !change.isAdded || change.controlNewText.isInt()
                        }
                        text = "0.5"
                        font = Font(20.0)
                    }
                    tauText = label("tau0") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(20.0)
                    }
                    tauOrCountTextField = textfield() {
                        text = "5"
                        font = Font(20.0)
                    }
                    isVisible = false
                    schedType.valueProperty().addListener { e ->
                        if (schedType.value == "NonPreemptiveSJF with Exponential Averaging") {
                            hboxForAdd.isVisible = true
                            tauText.isVisible = true
                            tauText.text = "tau0"
                            tauOrCountTextField.isVisible = true
                            alphaOrQuantumText.text = "alpha(0.0 ~ 1.0)"
                            alphaOrQuantumTextField.text = "0.5"
                        } else if (schedType.value == "Round Robin" || schedType.value == "Round Robin in MS-Windows" || schedType.value == "Mine") {
                            alphaOrQuantumText.text = "Quantum"
                            hboxForAdd.isVisible = true
                            tauText.isVisible = false
                            tauOrCountTextField.isVisible = false
                            alphaOrQuantumTextField.text = "1000"
                        } else hboxForAdd.isVisible = false
                        if(schedType.value == "Mine") {
                            tauOrCountTextField.isVisible = true
                            tauText.isVisible = true
                            tauText.text = "loopCount"
                        }
                    }
                }
                hboxForMine = hbox {
                    mineAlphaText = label("alpha(0.0 ~ 1.0)") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(20.0)
                    }
                    mineAlphaTextField = textfield() {
                        filterInput { change ->
                            !change.isAdded || change.controlNewText.let {
                                it.isDouble() && it.toDouble() in 0.0..1.0
                            }
                        }
                        text = "0.5"
                        font = Font(20.0)
                    }
                    mineTauText = label("tau0") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(20.0)
                    }
                    mineTauTextField = textfield() {
                        text = "1000.0"
                        font = Font(20.0)
                    }
                    schedType.valueProperty().addListener { _->
                        hboxForMine.isVisible = schedType.value == "Mine"
                    }
                    isVisible = false
                }

                borderpane {
                    top = gridpane {
                        row {
                            labels.add(label("Execution time"))
                            texts.add(textfield())
                            labels.add(label("Turnaround time"))
                            texts.add(textfield())
                        }
                        row {
                            labels.add(label("Waiting time"))
                            texts.add(textfield())
                            labels.add(label("Response time"))
                            texts.add(textfield())
                        }
                        row {
                            labels.add(label("Throughput"))
                            texts.add(textfield())
                            labels.add(label("Weighted waiting time"))
                            texts.add(textfield())
                        }
                        row {
                            labels.add(label("CPU utilization ratio"))
                            texts.add(textfield())
                        }
                    }
                    center = vbox {
                        add(ganttChart.root)
                        add(selectivity.root)
                    }

                    bottom = hbox {
                        button("File choose").apply {
                            font = Font(20.0)
                            spacing = 10.0
                            setOnAction {
                                file = chooseFile(filters = emptyArray())
                                val temp = ProcessTokenizer(file[0]).getProcesses()
                                processes.setAll(temp)

                            }
                        }
                        button("Run").apply {
                            font = Font(20.0)

                            setOnAction {
                                val timeUnit = timeUnitText.text.toInt()
                                val scheduler = when (schedType.selectedItem) {
                                    "FCFS" -> FCFS(processes, timeUnit)
                                    "NonPreemptiveSJF" -> NonPreemptiveSJF(processes, timeUnit)
                                    "NonPreemptiveSJF with Exponential Averaging" ->
                                        ExpNonPreemptiveSJF(
                                            processes,
                                            timeUnit,
                                            tauOrCountTextField.text.toDouble(),
                                            alphaOrQuantumTextField.text.toDouble()
                                        )
                                    "PreemptiveSJF" -> PreemptiveSJF(processes, timeUnit)
                                    "Round Robin" -> RoundRobin(
                                        processes,
                                        timeUnit,
                                        alphaOrQuantumTextField.text.toInt()
                                    )
                                    "HRRN" -> HRRN(processes, timeUnit)
                                    "Round Robin in MS-Windows" -> RoundRobinInMS(processes, timeUnit, alphaOrQuantumTextField.text.toInt())
                                    "Mine" -> Mine(processes, timeUnit,
                                        alphaOrQuantumTextField.text.toInt(),
                                        mineAlphaTextField.text.toDouble(),
                                        tauOrCountTextField.text.toInt(),
                                        mineTauTextField.text.toDouble())
                                    else -> FCFS(processes, timeUnit)
                                }
                                var (result, progresses) = scheduler.run()
                                result = result.sortedBy { it.pId }

                                results.setAll(result)
                                texts[0].text = scheduler.totalExecutionTime.toString()
                                texts[1].text = scheduler.averageTurnaroundTime.toString()
                                texts[2].text = scheduler.averageWaitingTime.toString()
                                texts[3].text = scheduler.averageResponseTime.toString()
                                texts[4].text = scheduler.throughput.toString()
                                texts[5].text = scheduler.averageWeightedWaitingTime.toString()
                                texts[6].text = scheduler.cpuUtilization.toString()
                                ganttChart.draw(progresses, scheduler.totalExecutionTime, processes.toList())
                                selectivity.draw(scheduler.completedProcesses, schedType.selectedItem!!)
                            }
                        }
                    }.apply { paddingAll = 10.0 }
                    style { borderColor += box(Color.BLACK) }
                }
                paddingAll = 20.0
                spacing = 10.0
            }
            vbox {
                processTable = tableview(processes) {
                    readonlyColumn("pID", Process::pId)
                    readonlyColumn("burstTime", Process::burstTime).prefWidth(150.0)
                    readonlyColumn("arrivalTime", Process::arrivalTime).prefWidth(150.0)
                    readonlyColumn("Priority", Process::priority).prefWidth(150.0)
                    style {
                        font = Font(15.0)
                    }
                    //columnResizePolicy = SmartResize.POLICY

                }
                resultTable = tableview(results) {
                    readonlyColumn("pID", Result::pId)
                    readonlyColumn("ExecutionTime", Result::executionTime).prefWidth(150.0)
                    readonlyColumn("WaitingTime", Result::waitingTime).prefWidth(150.0)
                    readonlyColumn("ResponseTime", Result::responseTime).prefWidth(150.0)
                    readonlyColumn("TurnaroundTime", Result::turnaroundTime).prefWidth(200.0)
                    //readonlyColumn("Throughput", ResultProcessInfo::throughput)
                    //readonlyColumn("CPU Ratio", ResultProcessInfo::cpuRatio)
                    style {
                        font = Font(15.0)
                    }
                }

                paddingAll = 20.0
                spacing = 10.0
            }
        }
        labels.forEach {
            with(it) {
                prefWidth = 250.0
                prefHeight = 30.0
                font = Font(20.0)
                paddingAll = 10.0
                /*style {
                   borderColor += box(Color.BLACK)
                }*/
            }
        }
        texts.forEach {
            with(it) {
                prefWidth = 200.0
                prefHeight = 30.0
                font = Font(15.0)
                paddingAll = 10.0
                isEditable = false
            }
        }
    }
}
