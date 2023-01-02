package ru.netology.nmedia

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface PostApiService{
    @GET("posts")
    fun getAll(): Call<List<Post>>

    @POST("posts/{postId}/likes")
    fun likeById(@Path("postId") id: Long): Call<Post>

    @DELETE("posts/{postId}/likes")
    fun dislikeById(@Path("postId") id: Long): Call<Post>

    @POST("posts")
    fun save(@Body post: Post): Call<Post>

    @DELETE("posts/{postId}")
    fun removeById(@Path("postId") id: Long): Call<Unit>
}

object PostsApi {

    private const val BASE_URL = "${BuildConfig.BASE_URL}"

    private val logging = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG){
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    val retrofitService: PostApiService by lazy{
        retrofit.create(PostApiService::class.java)
    }
}