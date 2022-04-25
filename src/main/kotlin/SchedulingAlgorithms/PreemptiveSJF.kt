package SchedulingAlgorithms

import Result
import Process
import Progress
import java.util.*
import kotlin.Comparator

@ExperimentalStdlibApi
class PreemptiveSJF(val processList: MutableList<Process>, val timeUnit: Int) :
    Scheduler(processList.size, timeUnit, processList) {
    override var readyQueueComparator: Comparator<Process> = Comparator { process1, process2 ->
        when {
            process1.burstTime < process2.burstTime -> -1
            process1.burstTime > process2.burstTime -> 1
            else -> 0
        }
    }
    override var readyQueue = PriorityQueue(n, readyQueueComparator)

    private val REMIAN = 0
    private var state = REMIAN
    private var originalCurrentTime: Double = 0.0

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
                    readyQueue.add(it)
                    remove()
                }
            }
        }
    }

    override fun run(): Pair<List<Result>, List<Progress>> {
        var process = choose() as Process
        while (readyQueue.isNotEmpty() || processes.isNotEmpty() || state-- == REMIAN) {
            val result = results[process.pId - 1]
            result.waitingTime += currentTime - process.arrivalTime

            if (result.responseTime == -1.0)
                result.responseTime = currentTime - process.arrivalTime

            val next = recompute(process)
            if (next == null) {
                currentTime = originalCurrentTime + process.burstTime
                addRange()
                result.turnaroundTime = currentTime - processList[process.pId - 1].arrivalTime
                progresses.add(Progress(process.pId, originalCurrentTime, currentTime))
                process = choose() ?: break
                if (readyQueue.isEmpty()) state = REMIAN
            } else {
                process.burstTime -= (currentTime - process.arrivalTime)
                if (process.burstTime == 0.0) {
                    result.turnaroundTime = currentTime - processList[process.pId - 1].arrivalTime
                    addRange()
                } else {
                    process.arrivalTime = currentTime
                    readyQueue.add(process)
                    progresses.add(Progress(process.pId, originalCurrentTime, currentTime))
                }
                process = next
            }
        }
        setTotalTime()
        return Pair(results, progresses)
    }

    private fun recompute(currentProcess: Process): Process? { // true if no context switching
        var isAdded = false
        originalCurrentTime = currentTime
        with(processes.iterator()) {
            forEach {
                currentTime = it.arrivalTime
                val remainBurstTime = (currentProcess.arrivalTime + currentProcess.burstTime) - currentTime
                if (it.burstTime < remainBurstTime) {
                    readyQueue.add(it)
                    remove()
                    isAdded = true
                    return@with
                } else if (it.arrivalTime <= originalCurrentTime + currentProcess.burstTime) {
                    readyQueue.add(it)
                    remove()
                } else return@with
            }
        }
        if (!isAdded) {
            return null
        }

        var next = readyQueue.peek()
        for (process in readyQueue) {
            if (next.burstTime == process.burstTime && next.arrivalTime > process.arrivalTime)
                next = process
        }
        readyQueue.remove(next)
        return next
    }

    override fun choose(): Process? {
        var target: Process? = readyQueue.peek()
        if(target == null && processes.isNotEmpty()) {
            target = processes.removeFirst()
            idleTime += target.arrivalTime - currentTime
            currentTime = target.arrivalTime
        } else {
            for (process in readyQueue) {
                if (target!!.burstTime == process.burstTime && target.arrivalTime > process.arrivalTime)
                    target = process
            }
            readyQueue.remove(target)
        }
        return target
    }

}