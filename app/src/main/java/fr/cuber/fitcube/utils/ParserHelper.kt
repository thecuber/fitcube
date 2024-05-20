package fr.cuber.fitcube.utils

fun showPrediction(prediction: List<Double>, msg: Boolean = false) = if(prediction.isNotEmpty()) { prediction.joinToString(separator = "lbs, ", postfix = "lbs") } else if (msg) { "Empty prediction" } else { "" }

fun boldPrediction(prediction: List<Double>, index: Int): String {
    return prediction.mapIndexed { i, value ->
        if (i == index) "<b>${value}lbs</b>" else "${value}lbs"
    }.joinToString(separator = ", ")
}
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
    val t = timer / 1000
    val seconds = (t % 60).toString().padStart(2, '0')
    val minutes = ((t / 60) % 60).toString().padStart(2, '0')
    val hours = (t / 3600).toString().padStart(2, '0')
    return "${hours}h ${minutes}m ${seconds}s"
}
