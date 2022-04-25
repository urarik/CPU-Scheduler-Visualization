package SchedulingAlgorithms

import Progress
import Result
import Process
import java.util.*

@ExperimentalStdlibApi
class Mine(private val processList: MutableList<Process>, timeUnit: Int, private val quantum: Int, private val alpha: Double, private val loopCounter: Int, private val tau0: Double) :
    Scheduler(processList.size, timeUnit, processList) {
    override val readyQueue = LinkedList<Process>()
    private var counter = 0
    private var predicatedWaitingTime = tau0
    override var readyQueueComparator = Comparator<Process> { process1, process2 ->
        val responseRatio1 = (currentTime - process1.arrivalTime + process1.priority) / process1.priority
        val responseRatio2 = (currentTime - process2.arrivalTime + process2.priority) / process2.priority
        when {
            responseRatio1 < responseRatio2 -> 1
            responseRatio1 > responseRatio2 -> -1
            else -> 0
        }
    }

    init {
        processList.forEach {
            processes.add(Process(it))
        }
        Collections.sort(processes, comparator)
        processList.forEach {
            results.add(Result(it.pId, it.burstTime, 0.0, 0.0, -1.0))
        }

        with(processes.iterator()) {
            forEach {
                if (it.arrivalTime <= currentTime) {
                    readyQueue.addLast(it)
                    remove()
                } else return@with
            }
        }
        Collections.sort(readyQueue, readyQueueComparator)
    }

    override fun run(): Pair<List<Result>, List<Progress>> {
        while (readyQueue.isNotEmpty() || processes.isNotEmpty()) {
            val process = choose()
            val result = results[process.pId - 1]
            val prevCurrentTime = currentTime
            result.waitingTime += currentTime - process.arrivalTime
            if (result.responseTime == -1.0) result.responseTime = currentTime

            var adjustedWaitingTime = (currentTime - process.arrivalTime) / (predicatedWaitingTime*2)
            if(adjustedWaitingTime > 1) adjustedWaitingTime = 1.0
            val waitingTime = currentTime - process.arrivalTime
            predicatedWaitingTime = alpha * (if(waitingTime > predicatedWaitingTime * 5) predicatedWaitingTime * 5 else waitingTime) + (1 - alpha) * (predicatedWaitingTime)

            if (quantum < process.burstTime) {
                process.burstTime -= quantum
                currentTime += quantum.toDouble()
                process.arrivalTime = currentTime

                readyQueue.add(((0.5 * (1-adjustedWaitingTime) +0.5 *(process.priority / 32.0)) * readyQueue.size).toInt(), process)

            } else {
                currentTime += process.burstTime
                addRange()
                result.turnaroundTime = (currentTime) - processList[process.pId - 1].arrivalTime
                process.burstTime = 0.0
            }
            progresses.add(Progress(process.pId, prevCurrentTime, currentTime))
            recompute()
            if (counter++ == loopCounter) {
                Collections.sort(readyQueue, readyQueueComparator)
                counter = 0
            }
        }
        setTotalTime()
        return Pair(results, progresses)
    }

    private fun recompute() {
        with(processes.iterator()) {
            forEach {
                if (it.arrivalTime <= currentTime) {
                    readyQueue.addLast(it)
                    remove()
                } else return@with
            }
        }
    }

    override fun choose(): Process {
        var process = readyQueue.removeFirstOrNull()
        if(process == null && processes.isNotEmpty()) {
            process = processes.removeFirst()
            idleTime += process.arrivalTime - currentTime
            currentTime = process.arrivalTime
        }
        return process!!
    }

}