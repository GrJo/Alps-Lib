package com.alpsbte.alpslib.http

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

abstract class HttpRequest {
    companion object {
        @JvmStatic
        fun getJSON(url: String): HttpResponse {
            return getJSON(url,null)
        }

        @JvmStatic
        fun getJSON(url: String, apiKeyValue: String?): HttpResponse {
            val con: HttpsURLConnection = URL(url).openConnection() as HttpsURLConnection
            con.requestMethod = "GET"
            con.setRequestProperty("Content-Type", "application/json")
            con.setRequestProperty("Accept", "application/json")
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            if (apiKeyValue != null) con.setRequestProperty("Authorization", apiKeyValue)

            con.connect()
            var content: String? = null
            if (con.responseCode in 200..299) {
                content = readJSON(con.inputStream)
                con.inputStream.close()
            }

            return HttpResponse(content, con.responseCode)
        }

        private fun readJSON(stream: InputStream): String{
            val content = StringBuilder()
            BufferedReader(InputStreamReader(stream)).use { `in` ->
                var inputLine: String?
                while (`in`.readLine().also { inputLine = it } != null) {
                    content.append(inputLine)
                }
            }
            return content.toString()
        }
    }

    data class HttpResponse(var data: String?, var responseCode: Int)
}