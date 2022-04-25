import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.Comparator

class ProcessTokenizer(val file: File) {
    fun getProcesses(): List<Process> {
        val reader = FileReader(file)
        val lines = reader.readLines()
        val processes = mutableListOf<Process>()
        for(line in lines) {
            val words = line.split(" ")
            if(words[0] == "process")
                processes.add(Process(words[1].toInt(), words[2].toDouble(), words[3].toDouble(), words[4].toInt()))
        }

        val comparator = Comparator<Process> { process1, process2 ->
            when {
                process1.pId < process2.pId -> -1
                process1.pId > process2.pId -> 1
                else -> 0
            }
        }
        Collections.sort(processes, comparator)
        return processes
    }
}