package ru.netology.nmedia.repository

import ru.netology.nmedia.Post

interface PostRepository {
    fun getAll(): List<Post>
    //fun likeById(id: Long, isLiked: Boolean): Post
    fun likeById(id: Long, isLiked: Boolean): Post
    //    fun shareById(id: Long)
    fun save(post: Post): Post
    fun removeById(id: Long)
}