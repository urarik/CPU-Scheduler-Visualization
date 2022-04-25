package SchedulingAlgorithms

import java.util.*
import Process
import Progress
import Result
import java.lang.Double.min
import kotlin.collections.ArrayDeque

@ExperimentalStdlibApi
class RoundRobinInMS(private val processList: MutableList<Process>, private val timeUnit: Int,private val quantum: Int) : Scheduler(processList.size, timeUnit, processList) {
    private val REMIAN = 0
    private var state = REMIAN
    private var originalCurrentTime: Double = 0.0
    private var preemptCounter = 0
    private var currentQuantum = quantum.toDouble()
    private val preemptRemainTime = mutableListOf<Double>()

    override val readyQueue = ArrayDeque<Process>()

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
                if (currentQuantum < process.burstTime) {
                    process.burstTime -= currentQuantum
                    currentTime = originalCurrentTime + currentQuantum
                    process.arrivalTime = currentTime
                    readyQueue.addLast(process)
                } else {
                    currentTime = originalCurrentTime + process.burstTime
                    addRange()
                    result.turnaroundTime = (currentTime) - processList[process.pId - 1].arrivalTime
                    process.burstTime = 0.0
                }
                progresses.add(Progress(process.pId, originalCurrentTime, currentTime))
                process = choose() ?: break
                if(readyQueue.isEmpty()) state = REMIAN
            } else {
                process.burstTime -= (next.arrivalTime - originalCurrentTime)
                if(process.burstTime == 0.0) {
                    result.turnaroundTime = currentTime - processList[process.pId - 1].arrivalTime
                    addRange()
                }else {
                    preemptRemainTime.add(if(process.priority < 15) quantum.toDouble() else quantum - currentTime + originalCurrentTime)
                    process.arrivalTime = currentTime
                    readyQueue.addFirst(process)
                    preemptCounter++
                    progresses.add(Progress(process.pId, originalCurrentTime, currentTime))
                }
                process = next
            }
        }
        setTotalTime()
        return Pair(results, progresses)
    }

    override fun choose(): Process? {
        var process = readyQueue.removeFirstOrNull()

        if(process == null && processes.isNotEmpty()) {
            process = processes.removeFirst()
            idleTime += process.arrivalTime - currentTime
            currentTime = process.arrivalTime
        } else {
            if (preemptCounter > 0) {
                preemptCounter--
                currentQuantum = preemptRemainTime.removeLast()
            } else currentQuantum = quantum.toDouble()
        }

        return process
    }

    private fun recompute(currentProcess: Process): Process? { // true if no context switching
        originalCurrentTime = currentTime
        var newCounter = 0
        with(processes.iterator()) {
            forEach {
                if (it.arrivalTime <= originalCurrentTime + min(currentProcess.burstTime, currentQuantum)) {
                    newCounter++
                    readyQueue.add(it)
                    currentTime = it.arrivalTime
                    remove()
                    return@with
                } else return@with
            }
        }
        if(originalCurrentTime == currentTime) {
            return null
        }

        var next = readyQueue.last()
        for (i in readyQueue.size-2 downTo readyQueue.size-newCounter) {
            if(next.priority > readyQueue[i].priority) next = readyQueue[i]
            else if (next.priority == readyQueue[i].priority) {
                if(next.arrivalTime > readyQueue[i].arrivalTime)
                    next = readyQueue[i]
            }
        }

        return if(next.priority < currentProcess.priority) {
            readyQueue.remove(next)
            next
        }else null
    }

}