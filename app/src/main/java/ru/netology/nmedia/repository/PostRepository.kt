package ru.netology.nmedia.repository

import ru.netology.nmedia.Post


interface PostRepository {
    //    fun shareById(id: Long)

    fun getAllAsync(callback: MyCallback<List<Post>>)
    fun likeByIdAsync(id: Long, isLiked: Boolean, callback: MyCallback<Post>)
    fun saveAsync(post: Post, callback: MyCallback<Post>)
    fun removeByIdAsync(id: Long, callback: MyCallback<Unit>)


    interface MyCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Exception)
    }


}