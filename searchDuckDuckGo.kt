import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeminiApiService {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun getAnswer(
        @Body request: GeminiRequest // Request body with the structure expected by the Gemini API
    ): Response<GeminiResponse>
}
data class GeminiResponse(
    val content: String? // Adjust this based on the actual API response structure
)


data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)
