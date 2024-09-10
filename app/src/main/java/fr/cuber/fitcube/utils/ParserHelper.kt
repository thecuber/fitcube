package fr.cuber.fitcube.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun showPrediction(prediction: List<Double>, msg: Boolean = false) = if(prediction.isNotEmpty()) { prediction.joinToString(separator = "kgs, ", postfix = "kgs") } else if (msg) { "Empty prediction" } else { "" }

fun boldPrediction(prediction: List<Double>, index: Int): String {
    return prediction.mapIndexed { i, value ->
        if (i == index) "<b>${value}kgs</b>" else "${value}kgs"
    }.joinToString(separator = ", ")
}

fun textBoldPrediction(prediction: List<Double>, index: Int): AnnotatedString {
   return buildAnnotatedString {
        prediction.forEachIndexed { i, value ->
            val text =  "${value}kgs"
            withStyle(style = SpanStyle(fontWeight = if(i == index) FontWeight.Bold else FontWeight.Normal)) {
                append(text)
            }
            if(i < prediction.size - 1) append(", ")
        }
    }
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

