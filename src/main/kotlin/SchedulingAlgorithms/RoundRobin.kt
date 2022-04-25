package SchedulingAlgorithms

import Result
import Progress
import Process
import java.util.*

@ExperimentalStdlibApi
class RoundRobin(val processList: MutableList<Process>, val timeUnit: Int, val quantum: Int) :
    Scheduler(processList.size, timeUnit, processList) {
    override val readyQueue = LinkedList<Process>()

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
            val result = results[process!!.pId - 1]
            val prevCurrentTime = currentTime
            result.waitingTime += currentTime - process.arrivalTime
            if (result.responseTime == -1.0) result.responseTime = currentTime

            if (quantum < process.burstTime) {
                process.burstTime -= quantum
                currentTime += quantum.toDouble()
                process.arrivalTime = currentTime
                readyQueue.addLast(process)

            } else {
                currentTime += process.burstTime
                addRange()
                result.turnaroundTime = (currentTime) - processList[process.pId - 1].arrivalTime
                process.burstTime = 0.0
            }
            progresses.add(Progress(process.pId, prevCurrentTime, currentTime))
            recompute()
        }
        setTotalTime()
        return Pair(results, progresses)
    }

    fun recompute() {
        with(processes.iterator()) {
            forEach {
                if (it.arrivalTime <= currentTime) {
                    readyQueue.addLast(it)
                    remove()
                } else return@with
            }
        }
    }

    override fun choose(): Process? {
        var process = readyQueue.removeFirstOrNull()
        if(process == null && processes.isNotEmpty()) {
            process = processes.removeFirst()
            idleTime += process.arrivalTime - currentTime
            currentTime = process.arrivalTime
        }
        return process
    }

}