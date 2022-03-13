import com.alpsbte.alpslib.http.HttpRequest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class HttpRequestTest {
    companion object {
        @JvmStatic
        fun httpRequestsWithoutAuth(): Stream<String> {
            return Stream.of(
                "https://api.opentopodata.org/v1/test-dataset?locations=56,123")
        }    }

    @ParameterizedTest
    @MethodSource("httpRequestsWithoutAuth")
    fun testHttpGet(request: String) {
        val response : HttpRequest.HttpResponse = HttpRequest.getJSON(request)
        assertEquals(200, response.responseCode)
    }

    fun testHttpGetWithAuth() {
        val response : HttpRequest.HttpResponse = HttpRequest.getJSON("https://alps-bte.com/plotapi/v1/get_plots",
            "API_KEY")
        assertEquals(200, response.responseCode)
    }
}