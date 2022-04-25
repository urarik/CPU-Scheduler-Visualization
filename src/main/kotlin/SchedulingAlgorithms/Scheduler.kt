package SchedulingAlgorithms

import Result
import Process
import Progress
import kotlin.Comparator

abstract class Scheduler(protected val n: Int, private val timeUnit: Int, private val processList: List<Process>) {

    var totalExecutionTime: Double = 0.0
    var averageWaitingTime: Double = 0.0
    var averageTurnaroundTime: Double = 0.0
    var averageResponseTime: Double = 0.0
    var averageWeightedWaitingTime: Double = 0.0
    var currentTime: Double = 0.0
    var throughput: Double = 0.0
    var cpuUtilization: Double = 0.0
    var idleTime: Double = 0.0
    val comparator = Comparator<Process> {
        process1, process2 ->
        when {
            process1.arrivalTime < process2.arrivalTime -> -1
            process1.arrivalTime > process2.arrivalTime -> 1
            else -> 0
        }
    }
    open val readyQueueComparator: Comparator<Process> = comparator
    abstract val readyQueue: Collection<Process>
    val progresses = mutableListOf<Progress>()
    open val processes = mutableListOf<Process>()
    val results = mutableListOf<Result>()
    val completedProcesses = mutableListOf<Int>()
    abstract fun run(): Pair<List<Result>, List<Progress>>
    abstract fun choose(): Process?
    fun setTotalTime() {
        results.forEach {
            averageResponseTime += it.responseTime
            averageTurnaroundTime += it.turnaroundTime
            averageWaitingTime += it.waitingTime
            //totalExecutionTime += it.executionTime
            averageWeightedWaitingTime += (32 - processList[it.pId - 1].priority) * it.waitingTime
        }

        totalExecutionTime = currentTime
        averageWaitingTime /= n
        averageResponseTime /= n
        averageTurnaroundTime /= n
        averageWeightedWaitingTime /= n
        cpuUtilization = ((totalExecutionTime - idleTime)/totalExecutionTime) * 100

        var sum = 0.0
        completedProcesses.forEach { sum += it }
        println(sum)
        throughput = sum/completedProcesses.size

    }
    fun addRange() {
        val tempSection = currentTime / timeUnit
        val completeSection = tempSection.toInt() - if((tempSection - tempSection.toInt()) == 0.0) 1 else 0
        if(completeSection >= completedProcesses.size)
            for(i in 0..(completeSection - completedProcesses.size + 1))
                completedProcesses.add(0)
        completedProcesses[completeSection]++
    }
}