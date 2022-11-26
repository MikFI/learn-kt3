package ru.netology.nmedia.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.Post
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

//    init {
//        //если это первый запуск (значения "firstRun" в SharedPreferences нет),
//        //то напихиваем в репозиторий стартовый набор постов из PostDemoSet
//        if (sharedPrefs.getBoolean("firstRun", true)) {
//            val posts = PostDemoSet.posts.reversed()
//            posts.forEach {
//                save(it)
//            }
//            sharedPrefs.edit().apply() {
//                putBoolean("firstRun", false)
//                apply()
//            }
//        }
//    }

    override fun getAll(): List<Post> {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .build()

        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let { gson.fromJson(it, typeToken.type) }
    }

    override fun likeById(id: Long, isLiked: Boolean): Post {
        val url = "$BASE_URL/api/posts/$id/likes"
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
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("Server response body is null") }
            .let { gson.fromJson(it, Post::class.java) }
    }

//    override fun shareById(id: Long) {
//        TODO("Not yet implemented")
//    }

    override fun save(post: Post): Post {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("Server response body is null") }
            .let { gson.fromJson(it, Post::class.java) }
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/slow/posts/$id")
            .delete()
            .build()

        client.newCall(request).execute()
    }
}