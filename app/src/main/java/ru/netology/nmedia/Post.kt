package ru.netology.nmedia

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likes: Int = 999,
    val shares: Int = 100,
    val views: Int = 100,
    val likedByMe: Boolean = false,
)