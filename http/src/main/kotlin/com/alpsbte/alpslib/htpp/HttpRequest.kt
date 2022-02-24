package com.alpsbte.alpslib.htpp

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

abstract class HttpRequest {
    companion object {
        @JvmStatic
        fun getJSON(url: String, classObj: Class<Any>? = null): HttpResponse {
            return getJSON(url, classObj,null)
        }

        @JvmStatic
        fun getJSON(url: String, classObj: Class<Any>? = null, apiKeyValue: String?): HttpResponse {
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

            return HttpResponse(classObj, content, con.responseCode)
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

    class HttpResponse(jsonClass: Class<Any>?, data: String?, responseCode: Int) {
        var responseCode: Int = responseCode
            private set
        var jsonObject: JsonObject? = null
        var jsonElement: JsonElement? = null

        init {
            if (data != null) {
                val gson = Gson()
                if (jsonClass != null) {
                    jsonObject = gson.fromJson(data, jsonClass) as JsonObject
                } else jsonElement = JsonParser().parse(data)
            }
        }
    }
}