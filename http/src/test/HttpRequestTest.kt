import com.alpsbte.alpslib.http.HttpRequest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HttpRequestTest {
    companion object {
        @JvmStatic
        fun httpRequestsWithoutAuth(): Stream<String> {
            return Stream.of(
                "https://api.opentopodata.org/v1/test-dataset?locations=56,123")
        }
    }

    @ParameterizedTest
    @MethodSource("httpRequestsWithoutAuth")
    fun testHttpGet(request: String) {
        val response : HttpRequest.HttpResponse = HttpRequest.getJSON(request, null)
        assertEquals(200, response.responseCode)

        assertNotNull(response.jsonElement)
        assertNull(response.jsonObject)
    }

    @Test
    fun testHttpGetWithAuth() {
        val response : HttpRequest.HttpResponse = HttpRequest.getJSON("https://alps-bte.com/plotapi/v1/get_plots",
            null, "API_KEY")
        assertEquals(200, response.responseCode)

        assertNotNull(response.jsonElement)
        assertNull(response.jsonObject)
    }
}