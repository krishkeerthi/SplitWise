package com.example.splitwise.api

import android.content.ContentValues
import android.util.Log
import com.example.splitwise.model.UnSplashResponse
import com.example.splitwise.model.UnsplashPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

const val BASE_URL = "https://api.unsplash.com/"
const val CLIENT_ID = "9KN9w3c0qYgiIo4cKDWVjIMN1qiSnQa-7OuWSaTtcCs"

object UnsplashApi {
    suspend fun searchPhotos(query: String, page: Int, per_page: Int): UnSplashResponse {
        return withContext(Dispatchers.IO) {

            val url = URL(
                "${BASE_URL}search/photos/?client_id=${CLIENT_ID}" +
                        "&query=${query}&page=${page}&per_page=${per_page}"
            )
            val httpUrlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection

            var unSplashResponse = UnSplashResponse(mutableListOf())

            try {
                val responseCode = httpUrlConnection.responseCode

                Log.d(ContentValues.TAG, "performNetworkRequest: response code: $responseCode")
                if (responseCode != 200) {
                    throw IOException("The error from server is $responseCode")
                }

                val bufferedReader = BufferedReader(
                    InputStreamReader(
                        httpUrlConnection.inputStream
                    )
                )

                var responseString = ""

                while (true) {
                    val line = bufferedReader.readLine() ?: break
                    responseString += line
                }

                Log.d(ContentValues.TAG, "performNetworkRequest: $responseString")

//                    val userProfileResponse = Gson().fromJson(responseString, UserProfileResponse::class.java)

                unSplashResponse = parseJsonStringToObject(responseString)
            } catch (e: Exception) {
                Log.d(ContentValues.TAG, "performNetworkRequest: exception ${e.message}")
            } finally {
                httpUrlConnection.disconnect()
            }

            unSplashResponse
        }
    }

    //With the JSONTokener, we can parse a JSON string into an object.
    private fun parseJsonStringToObject(jsonString: String): UnSplashResponse {

        val unsplashPhotos = mutableListOf<UnsplashPhoto>()

        val jsonObject = JSONTokener(jsonString).nextValue() as JSONObject

        val results = jsonObject.getJSONArray("results")

        for (i in 0 until results.length()) {
            val result = results.getJSONObject(i)

            val id = result.getString("id")
            val description = result.getString("description")

            val urlsObject = result.getJSONObject("urls")
            val unsplashPhotoUrls = UnsplashPhoto.UnsplashPhotoUrls(
                urlsObject.getString("raw"),
                urlsObject.getString("full"),
                urlsObject.getString("regular"),
                urlsObject.getString("small"),
                urlsObject.getString("thumb"),
            )

            val userObject = result.getJSONObject("user")
            val unsplashUser = UnsplashPhoto.UnsplashUser(
                userObject.getString("name"),
                userObject.getString("username")
            )

            unsplashPhotos.add(
                UnsplashPhoto(
                    id,
                    description,
                    unsplashPhotoUrls,
                    unsplashUser
                )
            )
        }

        return UnSplashResponse(unsplashPhotos)
    }
}