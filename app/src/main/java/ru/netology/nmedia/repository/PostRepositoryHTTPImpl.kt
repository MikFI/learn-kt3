package ru.netology.nmedia.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.Post
import java.io.IOException
import java.util.concurrent.TimeUnit

class PostRepositoryHTTPImpl(
    context: Context
) : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    //Constructs a new type literal. Derives represented class from type parameter.
    //Clients create an empty anonymous subclass.
    // Doing so embeds the type parameter in the anonymous class's type hierarchy so we can reconstitute it at runtime despite erasure.
    private val typeToken = object : TypeToken<List<Post>>() {}
    private val sharedPrefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAllAsync(callback: PostRepository.MyCallback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .build()

        client.newCall(request)
            //ставит запрос в очередь вместо немедленного (execute) исполнения
            //требует в себя callback, который по результату исполнения и будет докладывать
            //об успехе (onResponse) или неудаче (onFailure) оного
            //в отличие от execute не блокирует поток (асинхронный метод)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string()
                            ?: throw RuntimeException("Server response body is null")
                        callback.onSuccess(gson.fromJson(body, typeToken.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

    override fun likeByIdAsync(
        id: Long,
        isLiked: Boolean,
        callback: PostRepository.MyCallback<Post>
    ) {
        val url = "$BASE_URL/api/slow/posts/$id/likes"
        val request: Request = if (!isLiked) {
            Request.Builder()
                .url(url)
                .post(gson.toJson(id).toRequestBody(jsonType))
                .build()
        } else {
            Request.Builder()
                .url(url)
                .delete()
                .build()
        }

        //выполняем запрос и десериализуем то, что нам сервер отдал в качестве ответа
        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string()
                            ?: throw RuntimeException("Server response body is null")
                        callback.onSuccess(gson.fromJson(body, Post::class.java))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }


//    override fun shareById(id: Long) {
//        TODO("Not yet implemented")
//    }

    override fun saveAsync(post: Post, callback: PostRepository.MyCallback<Post>) {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string()
                            ?: throw RuntimeException("Server response body is null")
                        callback.onSuccess(gson.fromJson(body, Post::class.java))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.MyCallback<Unit>) {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/slow/posts/$id")
            .delete()
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        callback.onSuccess(Unit)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }
}