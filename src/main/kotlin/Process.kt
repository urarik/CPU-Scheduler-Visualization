data class Process(val pId: Int,
                   var arrivalTime: Double,
                   var burstTime: Double,
                   val priority: Int) {
    constructor(process: Process) : this(process.pId, process.arrivalTime, process.burstTime, process.priority)
}