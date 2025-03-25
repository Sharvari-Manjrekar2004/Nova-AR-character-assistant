data class SearchResult(
    val AbstractText: String?, // Main answer text
    val Heading: String?,      // Answer title
    val RelatedTopics: List<RelatedTopic>?
)

data class RelatedTopic(
    val Text: String,
    val FirstURL: String
)
