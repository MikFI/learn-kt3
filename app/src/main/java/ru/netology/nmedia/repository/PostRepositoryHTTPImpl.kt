package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.Post
import ru.netology.nmedia.PostsApi

class PostRepositoryHTTPImpl : PostRepository {

    //дженерик для всех методов работы в постами, что описаны ниже
    //(раз уж они возвражают одно и тоже, только в разных типах)
    open class PostCallback<T>(private val callback: PostRepository.MyCallback<T>) : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            try {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }
                callback.onSuccess(
                    response.body()
                        ?: throw RuntimeException("Server response body is null")
                )
            } catch (e: Exception) {
                callback.onError(e)
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            callback.onError(Exception(t))
        }
    }

    override fun getAllAsync(callback: PostRepository.MyCallback<List<Post>>) {
        PostsApi.retrofitService.getAll()
            .enqueue(object : PostCallback<List<Post>>(callback){})
    }

    override fun likeByIdAsync(
        id: Long,
        isLiked: Boolean,
        callback: PostRepository.MyCallback<Post>
    ) {
        if (!isLiked) {
            PostsApi.retrofitService.likeById(id)
                .enqueue(object : PostCallback<Post>(callback) {})
        } else {
            PostsApi.retrofitService.dislikeById(id)
                .enqueue(object : PostCallback<Post>(callback) {})
        }
    }

//    override fun shareById(id: Long) {
//        TODO("Not yet implemented")
//    }

    override fun saveAsync(post: Post, callback: PostRepository.MyCallback<Post>) {
        PostsApi.retrofitService.save(post)
            .enqueue(object : PostCallback<Post>(callback) {})
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.MyCallback<Unit>) {
        PostsApi.retrofitService.removeById(id)
            .enqueue(object : PostCallback<Unit>(callback) {})
    }
}