package ru.netology.nmedia

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String = "",
    val content: String,
    val published: String,
    val likes: Int = 999,
//    val shares: Int = 100,
//    val views: Int = 100,
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
)

data class Attachment(
    val url: String,
    val description: String,
    val type: AttachmentType = AttachmentType.IMAGE
)

enum class AttachmentType{
    IMAGE
}