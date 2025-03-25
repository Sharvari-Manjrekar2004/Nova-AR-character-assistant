package com.xperiencelabs.astronaut.screens
//
//data class InstantAnswerResponse(
//    val Abstract: String?,
//    val AbstractSource: String?,
//    val AbstractText: String?,
//    val AbstractURL: String?,
//    val Answer: String?,
//    val AnswerType: String?,
//    val Definition: String?,
//    val DefinitionSource: String?,
//    val DefinitionURL: String?,
//    val Entity: String?,
//    val Heading: String?,
//    val Image: String?,
//    val ImageHeight: Int?,
//    val ImageIsLogo: Int?,
//    val ImageWidth: Int?,
//    val Infobox: String?,
//    val Redirect: String?,
//    val RelatedTopics: List<RelatedTopic>?
//)
//
//data class RelatedTopic(
//    val FirstURL: String?,
//    val Icon: Icon?,
//    val Result: String?,
//    val Text: String?
//)
//
//data class Icon(
//    val Height: String?,
//    val URL: String?,
//    val Width: String?
//)



data class DuckDuckGoResponse(
    val Abstract: String,
    val AbstractSource: String,
    val AbstractText: String,
    val AbstractURL: String,
    val Answer: String,
    val AnswerType: String,
    val Definition: String,
    val DefinitionSource: String,
    val DefinitionURL: String,
    val Entity: String,
    val Heading: String,
    val Image: String,
    val ImageHeight: Int,
    val ImageWidth: Int,
    val Infobox: String,
    val Redirect: String,
    val RelatedTopics: List<RelatedTopic>
)

data class RelatedTopic(
    val FirstURL: String,
    val Icon: Icon?,
    val Result: String,
    val Text: String
)

data class Icon(
    val Height: String?,
    val URL: String?,
    val Width: String?
)

fun processDuckDuckGoResponse(response: DuckDuckGoResponse) {
    if (response.AbstractText.isNotEmpty()) {
        // If AbstractText exists, use it as the main answer
        val answer = response.AbstractText
        // Handle Text-to-Speech or other processing here
        println("Answer: $answer")
    } else if (response.RelatedTopics.isNotEmpty()) {
        // If no AbstractText, return related topics as answers
        for (topic in response.RelatedTopics) {
            println("Related Topic: ${topic.Text} - ${topic.FirstURL}")
            // You can also return or process the URL and text for further interaction
        }
    } else {
        println("No relevant information found.")
    }
}
