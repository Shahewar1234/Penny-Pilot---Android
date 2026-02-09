import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {

    // Add this data class
    data class RatesResponse(
        val base: String,
        val date: String,
        val rates: Map<String, Double>    )

    // Your existing function declarations would go here, for example:
    @GET("latest")
    suspend fun getLatestRates(@Query("base") base: String): RatesResponse
}
