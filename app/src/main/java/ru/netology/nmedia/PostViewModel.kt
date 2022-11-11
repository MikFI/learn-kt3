package ru.netology.nmedia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositorySQLiteImpl

//шаблон для создания нового поста
//если ничего, кроме content не было передано - пост именно в таком виде и
//улетит в хранилище (которое уже само бует разбираться, чего с этим делать)
private val emptyPost: Post = Post(
    id = 0,
    author = "Me",
    content = "",
    published = "now"
)

//viewModel связывает UI с репозиторием(хранилищем) и отвечает за обработку того, что будет отрисовано в UI
//UI просит данные для отрисовки из хранилища через этот класс
//class PostViewModel : ViewModel() {
class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositorySQLiteImpl(
        AppDb.getInstance(application).postDao,
        application
    )
    val data = repository.getAll()
    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)

    //tempPost - "несуществующий", временный пост, пользователь его не видит,
    //выступает обёрткой над действиями с постами
    private var tempPost = emptyPost

    //костыль для перематывания списка постов вверх при добавлении нового
    //при переходе между фрагментами адаптер клинит, и он не выдаёт актуальные
    //данные в adapter.currentList.size, в результате чего либо список перематывается
    //наверх при любом действии с постом, либо не перематывается никогда
    //(в зависимости от реализации)
    val postsInFeed = MutableLiveData<Int>()

    //суём во временный пост то, что нам отправили на редактирование
    //потом оно (не)будет изменено через changeContent--sendPost
    //(в зависимости от того, внесены ли какие-то изменения или нет)
    fun edit(post: Post) {
        tempPost = post
    }

    fun removeById(id: Long) {
        repository.removeById(id)
    }

    //создаём/обновляем пост (посылаем временный пост в репозиторий - он сам разберётся),
    fun sendPost() {
        tempPost.let {
            repository.save(it)
        }
        if (tempPost.id == 0L) {
            postsInFeed.value = data.value?.size
        }
        //зануляем пост после сохранения, чтобы в следующий пост,
        //прошедший через эту функцию, не просочились поля из текущего поста
        tempPost = emptyPost
    }

    //обновляем текст нашего временного поста
    //если он отличается от того, что там лежит в данный момент
    fun changeContent(text: String) {
        val newText = text.trim()
        if (newText == tempPost.content) {
            return
        }
        tempPost = tempPost.copy(content = newText)
    }
}
