package ru.netology.nmedia.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.Post

//класс-прослойка для передачи значений между бд и программой
//(поскольку имена и типы конкретных данных могут отличаться там и там)
@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likes: Int = 999,
    val shares: Int = 100,
    val views: Int = 100,
    val likedByMe: Boolean = false,
) {
    //конвертация элемента бд в объект поста
    fun fromDBtoData(): Post {
        return (Post(
            id = id,
            author = author,
            content = content,
            published = published,
            likes = likes,
            shares = shares,
            views = views,
            likedByMe = likedByMe
        ))
    }

    //аналог статической функции из java
    //чтобы можно было вызвать из любого другого места кода, не создавая при этом
    //экземпляр класса - т.е., в данном случае, можно вызвать fromDataToDB() напрямую из
    //PostEntity, передав в неё пост
    companion object {
        //конвертация объекта поста в элемент бд
        fun fromDataToDB(post: Post): PostEntity {
            return (PostEntity(
                id = post.id,
                author = post.author,
                content = post.content,
                published = post.published,
                likes = post.likes,
                shares = post.shares,
                views = post.views,
                likedByMe = post.likedByMe
            ))
        }
    }
}