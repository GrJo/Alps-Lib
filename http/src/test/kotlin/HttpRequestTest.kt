import com.alpsbte.alpslib.htpp2.HttpRequest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HttpRequestTest {
    companion object {
        @JvmStatic
        fun httpRequests(): Stream<String> {
            return Stream.of(
                "https://alps-bte.com/plotapi/v1/get_plots")
        }
    }

    @ParameterizedTest
    @MethodSource("httpRequests")
    fun testHttpGet(request: String) {
        val response : HttpRequest.HttpResponse = HttpRequest.getJSON(request, null)
        assertEquals(200, response.responseCode)

        assertNotNull(response.jsonElement)
        //val obj: JsonObject? = response.jsonElement?.asJsonObject
    }
}