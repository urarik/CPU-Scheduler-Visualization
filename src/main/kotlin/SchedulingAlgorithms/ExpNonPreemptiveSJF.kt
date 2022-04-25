package SchedulingAlgorithms

import Result
import Process
import Progress
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

@ExperimentalStdlibApi
class ExpNonPreemptiveSJF(processList: MutableList<Process>, private val timeUnit: Int, tau0: Double, val alpha: Double) : Scheduler(processList.size, timeUnit, processList) {
    override var readyQueueComparator: Comparator<Process> = Comparator { process1, process2 ->
        when {
            predicatedBurstTime[process1.pId - 1] < predicatedBurstTime[process2.pId - 1] -> -1
            predicatedBurstTime[process1.pId - 1] > predicatedBurstTime[process2.pId - 1] -> 1
            else -> 0
        }
    }
    override var readyQueue = PriorityQueue(n, readyQueueComparator)
    private val predicatedBurstTime: MutableList<Double> = ArrayList(n)

    init {
        processes.addAll(processList)
        for(i in processList.indices)
            predicatedBurstTime.add(tau0)

        with(processes.iterator()) {
            forEach {
                if (it.arrivalTime <= currentTime) {
                    readyQueue.add(it)
                    remove()
                }
            }
        }
    }

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

    fun recompute() {
        with(processes.iterator()) {
            forEach {
                if (it.arrivalTime <= currentTime) {
                    readyQueue.add(it)
                    remove()
                } else return@with
            }
        }
    }

    override fun choose(): Process {
        var target = readyQueue.peek()
        if(target == null && processes.isNotEmpty()) {
            target = processes.removeFirst()
            idleTime += target.arrivalTime - currentTime
            currentTime = target.arrivalTime
        } else {
            for (process in readyQueue) {
                if (predicatedBurstTime[target.pId - 1] == predicatedBurstTime[process.pId - 1] && target.arrivalTime > process.arrivalTime)
                    target = process
            }
            predicatedBurstTime[target.pId - 1] =
                alpha * (target.burstTime) + (1 - alpha) * (predicatedBurstTime[target.pId - 1])
            readyQueue.remove(target)

            val tempList = mutableListOf<Process>()
            with(readyQueue.iterator()) {
                forEach {
                    if (target.pId == it.pId) {
                        remove()
                        tempList.add(it)
                    }
                }
            }
            tempList.forEach {
                readyQueue.add(it)
            }
        }

        return target
    }
}