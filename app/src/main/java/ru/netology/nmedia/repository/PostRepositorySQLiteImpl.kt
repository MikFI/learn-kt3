package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.Transformations
import ru.netology.nmedia.Post
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.db.PostEntity

class PostRepositorySQLiteImpl(
    private val dao: PostDao,
    context: Context
) : PostRepository {
    private val sharedPrefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)

    init {
        //если это первый запуск (значения "firstRun" в SharedPreferences нет),
        //то напихиваем в репозиторий стартовый набор постов из PostDemoSet
        if (sharedPrefs.getBoolean("firstRun", true)) {
            val posts = PostDemoSet.posts.reversed()
            posts.forEach {
                dao.save(PostEntity.fromDataToDB(it))
            }
            sharedPrefs.edit().apply() {
                putBoolean("firstRun", false)
                apply()
            }
        }
    }

    //livedata возвращается библиотекой room, и поэтому нам тут, в репозитории, не требуется
    //в виде отдельной переменной - вместо этого обращаемся к livedata из room, пользуясь
    //классом Transformations, создающим новую livedata из уже существующей,
    //переводя посты из объектов бд в объекты нашей программы
    //именно эту функцию вызовет viewmodel, через которую активити (или фрагмент) и будет
    //следить (observe) за всеми изменениями для отображения их на экране
    override fun getAll() = Transformations.map(dao.getAll()) { posts -> posts.map { it.fromDBtoData() } }
    //то же самое, только записано чуть короче - livedata имеет функцию расширения map,
    //вызывающую внутри Transformations.map
//    override fun getAll() = dao.getAll().map { posts -> posts.map { it.fromDBtoData() } }

    //выше мы привязались к livedata, которую отдаёт room
    //и поэтому все функции, описанные ниже, вносят изменения только в бд
    //изменения в ней автоматически отражаются на экране
    override fun save(post: Post) = dao.save(PostEntity.fromDataToDB(post))

    override fun likeById(id: Long) = dao.likeById(id)

    override fun shareById(id: Long) = dao.shareById(id)

    override fun removeById(id: Long) = dao.removeById(id)
}

