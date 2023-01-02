package ru.netology.nmedia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryHTTPImpl
import ru.netology.nmedia.util.SingleLiveEvent

//шаблон для создания нового поста
//если ничего, кроме content не было передано - пост именно в таком виде и
//улетит в хранилище (которое уже само бует разбираться, чего с этим делать)
private val emptyPost: Post = Post(
    id = 0,
    author = "Me",
    content = "",
    published = ""
)

//viewModel связывает UI с репозиторием(хранилищем) и отвечает за обработку того, что будет отрисовано в UI
//UI просит данные для отрисовки из хранилища через этот класс
class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryHTTPImpl()

    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel> = _data

    //событие создания поста
    //unit в данном случае передаётся в качестве "заглушки"
    //(во внутреннюю MutableLiveData нужно что-то передать)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    //tempPost - "несуществующий", временный пост, пользователь его не видит,
    //выступает обёрткой над действиями с постами
    private var tempPost = emptyPost

    //для обновления списка постов (чтобы не подгружать его с сервера каждый раз)
    //при добавлении/изменении поста сюда будут записываться изменения,
    //на которые будет реагировать соответствующая следилка
    private val _renewedPost = SingleLiveEvent<Post>()
    val renewedPost: LiveData<Post> = _renewedPost

    //для обработки ошибки, если какое-то действие с постом (создание/редактирование/удаление)
    //пошло не так, как хотелось бы
    private val _postError = SingleLiveEvent<Boolean>()
    val postError: LiveData<Boolean> = _postError

    init {
        loadPosts()
    }

    fun loadPosts() {
        //postValue внутри себя вызывает postToMainThread, который
        //отдаёт в главный поток какое-то действие, а тот, в свою очередь, выполняет setValue
        //(мы внутри отдельного потока вызвать setValue не можем - именно потому что это отдельный поток)
        //если мы отдадим несколько штук postValue до того, как хотя бы одно из них будет исполнено,
        //то выполнится только последнее из них
        _data.postValue(FeedModel(loading = true))
        //передаём анонимный объект, реализующий интерфейс репозитория, типизированный списком постов
        //и в случае успеха возвращающий этот список в feedmodel
        repository.getAllAsync(object : PostRepository.MyCallback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.postValue(FeedModel(posts = result, empty = result.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    //добавляем/обновляем пост в локальном списке, сразу же после сохранения поста
    //без повторной загрузки с сервера всего списка целиком
    fun addPostLocally(post: Post) {
        val filtered = _data.value?.posts.orEmpty().filter {
            it.id == post.id
        }
        val emptyData = _data.value?.posts?.size == 0

        //если это новый пост (такого у нас в списке ещё не было)
        //то либо создаём список из одного поста (в случае, если список был пуст),
        //заодно посылая сигнал о том, что список уже не пуст,
        //либо добавляем весь наш список к новому посту (чтобы он сверху висел)
        if (filtered.isEmpty()) {
            if (emptyData) {
                _data.postValue(FeedModel(posts = listOf(post), empty = false))
            } else {
                _data.postValue(
                    _data.value?.copy(posts = listOf(post) + _data.value!!.posts)
                )
            }
            //если такой пост у нас уже был - значит имело место редактирование, а значит просто
            //выводим тот же список постов, изменив только содержимое того, который нам прислали
        } else {
            _data.postValue(
                _data.value?.copy(posts =
                _data.value!!.posts.map {
                    if (it.id == post.id) it.copy(
                        content = post.content,
                    ) else it
                })
            )
        }
    }

    //ставим/снимаем лайк посту с указанным id и заодно
    //изменяем livedata, чтобы подписчики поймали событие лайка
    //(и изменили в разметке цвет иконки и число лайков без перезагрузки списка постов с сервера)
    //сперва локальные изменения, которые будут применены до тех пор, пока мы ждём сервер
    //следом "честный" запрос и обработка ответа от сервера, когда лайк проставится
    fun likeById(id: Long, isLiked: Boolean) {
        val likeAction = if (isLiked) -1 else 1
        _data.postValue(
            _data.value?.copy(posts =
            _data.value?.posts.orEmpty().map {
                if (it.id == id) it.copy(
                    likedByMe = !isLiked,
                    likes = it.likes + likeAction,
                ) else it
            })
        )
        repository.likeByIdAsync(id, isLiked, object : PostRepository.MyCallback<Post> {
            override fun onSuccess(result: Post) {
                _data.postValue(
                    _data.value?.copy(posts =
                    _data.value?.posts.orEmpty().map {
                        //изменяем число лайков в ответе чисто ради теста, что ответ пришёл и
                        //то, что было локальными изменениями, станет "официальными",
                        //изменившись ещё раз при необходимости (если, например, посту натыкали ещё
                        //лайков, пока мы ждали ответа)
                        if (it.id == id) result.copy(likes = 123) else it
                    })
                )
            }

            override fun onError(e: Exception) {
                _postError.postValue(true)
            }
        })
    }


//    fun shareById(id: Long) = repository.shareById(id)

    fun removeById(id: Long) {
        val oldPosts = _data.value
        //сразу удаляем пост из нашего отображаемого списка
        val filtered = _data.value?.posts.orEmpty().filter { it.id != id }
        _data.postValue(
            _data.value?.copy(
                posts = filtered,
                empty = filtered.isEmpty()
            )
        )

        //удаляем пост из репозитория (на что требуется время)
        repository.removeByIdAsync(id, object : PostRepository.MyCallback<Unit> {
            override fun onSuccess(result: Unit) {
                //тут сервер нам ничего полезного не отдаёт в случае успеха
            }

            override fun onError(e: Exception) {
                _data.postValue(oldPosts)
                _postError.postValue(true)
            }
        })
    }

    //суём во временный пост то, что нам отправили на редактирование
    //потом оно (не)будет изменено через changeContent--sendPost
    //(в зависимости от того, внесены ли какие-то изменения или нет)
    fun edit(post: Post) {
        tempPost = post
    }

    //создаём/обновляем пост (посылаем временный пост в репозиторий - он сам разберётся),
    fun sendPost() {
        repository.saveAsync(tempPost, object : PostRepository.MyCallback<Post> {
            override fun onSuccess(result: Post) {
                _renewedPost.postValue(result)
                _postCreated.postValue(Unit)

                //зануляем пост после сохранения, чтобы в следующий пост,
                //прошедший через эту функцию, не просочились поля из текущего поста
                tempPost = emptyPost
            }

            override fun onError(e: Exception) {
                _postError.postValue(true)
            }
        })
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
