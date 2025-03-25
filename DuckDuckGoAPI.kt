import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface DuckDuckGoAPI {
    @GET("search")
    suspend fun getInstantAnswer(
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): Response<Map<String, Any>> // Response body type is Map<String, Any>
}
