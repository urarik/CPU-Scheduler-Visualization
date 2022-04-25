package SchedulingAlgorithms

import Result
import Process
import Progress
import java.util.*
import kotlin.Comparator

class NonPreemptiveSJF(processList: MutableList<Process>, val timeUnit: Int) : Scheduler(processList.size, timeUnit, processList) {
    override var readyQueueComparator = Comparator<Process> { process1, process2 ->
        when {
            process1.burstTime < process2.burstTime -> -1
            process1.burstTime > process2.burstTime -> 1
            else -> 0
        }
    }
    override var readyQueue: Queue<Process> = PriorityQueue(n, readyQueueComparator)

    init {
        processes.addAll(processList)
        Collections.sort(processes, comparator)

        with(processes.iterator()) {
            forEach {
                if (it.arrivalTime <= currentTime) {
                    readyQueue.add(it)
                    remove()
                } else return@with
            }
        }
    }

    @ExperimentalStdlibApi
    override fun run(): Pair<List<Result>, List<Progress>> {
        while (readyQueue.isNotEmpty() || processes.isNotEmpty()) {
            val process = choose()
            with(process) {
                val waitingTime = currentTime - arrivalTime
                val turnaroundTime = currentTime + burstTime - arrivalTime
                val responseTime = waitingTime

                results.add(Result(pId, burstTime, waitingTime, turnaroundTime, responseTime))
                progresses.add(Progress(pId, currentTime, currentTime + burstTime))
                currentTime += burstTime

                addRange()
                recompute()
            }
        }
        setTotalTime()

        return Pair(results, progresses)
    }

    private fun recompute() {
        with(processes.iterator()) {
            forEach {
                if (it.arrivalTime <= currentTime) {
                    readyQueue.add(it)
                    remove()
                } else return@with
            }
        }
    }
    @ExperimentalStdlibApi
    override fun choose(): Process {
        var target = readyQueue.peek()
        if(target == null && processes.size != 0) {
            target = processes.removeFirst()
            idleTime += target.arrivalTime - currentTime
            currentTime = target.arrivalTime
        }
        else {
            for (process in readyQueue) {
                if (target.burstTime == process.burstTime && target.arrivalTime > process.arrivalTime)
                    target = process
            }
            readyQueue.remove(target)
        }
        return target
    }
}