package SchedulingAlgorithms

import Result
import java.util.*
import Process
import Progress

class FCFS(processList: MutableList<Process>, val timeUnit: Int): Scheduler(processList.size, timeUnit, processList) {
    override var readyQueueComparator = comparator
    override val readyQueue = PriorityQueue(processList.size, readyQueueComparator)

    init {
        processList.forEach {
            readyQueue.add(it)
        }
    }

    override fun run(): Pair<List<Result>, List<Progress>> {
        while (readyQueue.isNotEmpty()) {
            val process = choose()
            with(process) {
                val waitingTime = currentTime - arrivalTime
                val turnaroundTime = currentTime + burstTime - arrivalTime
                val responseTime = waitingTime

                results.add(Result(pId, burstTime, waitingTime, turnaroundTime, responseTime))
                progresses.add(Progress(pId, currentTime, currentTime + burstTime))
                currentTime += burstTime

                addRange()
            }
        }

        setTotalTime()

        return Pair(results, progresses)
    }

    override fun choose(): Process {
        val process = readyQueue.poll()
        if(process.arrivalTime > currentTime) {
            idleTime += process.arrivalTime - currentTime
            currentTime = process.arrivalTime
        }

        return process
    }
}