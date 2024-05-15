package fr.cuber.fitcube.utils

import fr.cuber.fitcube.workout.session.SessionStatus

fun showPrediction(prediction: List<Double>, msg: Boolean = false) = if(prediction.isNotEmpty()) { prediction.joinToString(separator = "lbs, ", postfix = "lbs") } else if (msg) { "Empty prediction" } else { "" }

fun parsePrediction(prediction: String): List<Double> {
    val arr = prediction.split(" ")
    var result = emptyArray<Double>()
    for(i in arr) {
        if(i.isEmpty()) continue
        val arri = i.split("x")
        result = result.plus(List(arri[0].toInt()) { arri[1].toDouble() })
    }
    return result.asList()
}

fun parseTimer(timer: Int): String {
    val minutes = (timer / 60).toString().padStart(2, '0')
    val seconds = (timer % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

fun parseDuration(timer: Long): String {
    val t = timer / (1000 * 60)
    val hours = (t / 60).toString().padStart(2, '0')
    val minutes = (t % 60).toString().padStart(2, '0').substring(0, 2)
    return "${hours}h ${minutes}m"
}

fun parseStatus(status: SessionStatus): String {
    return when(status) {
        SessionStatus.REST -> "Waiting start..."
        SessionStatus.EXERCISE -> "Working out"
        SessionStatus.TIMING -> "Resting"
        SessionStatus.DONE -> "Workout done"
    }
}