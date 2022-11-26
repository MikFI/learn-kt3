package ru.netology.nmedia

//описывает текущее состояние экрана со списком постов
//включает в себя сам список и перечень состояний
data class FeedModel (
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val error: Boolean = false,
    val empty: Boolean = false,
)