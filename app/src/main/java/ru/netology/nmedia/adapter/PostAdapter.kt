package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.netology.nmedia.Post
import ru.netology.nmedia.databinding.CardPostBinding

interface OnInteractionListener {
    fun like(post: Post)
    fun share(post: Post)
    fun edit(post: Post)
    fun remove(post: Post)
    fun playVideo(url: String)
    fun openPost(post: Post)
}

//adapter представляет собой мост между набором данных и объектом, использующим эти данные
//также отвечает за создание View-компонента для каждой единицы данных из набора
//в нашем случае такой компонентой является PostViewHolder
class PostAdapter(
    private val listener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    //вызывается, когда recyclerview нужно создать новую вьюшку для своего списка
    //(например, для отображения следующего элемента при пролистывании списка)
    //возвращает "шаблон" вьюшки (ViewHolder), куда будут заноситься данные через onBindViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
        PostViewHolder(
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener
        )

    //заполняет шаблон разметки (в нашем случае CardPostBinding) данными
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

//класс, используемый для сравнения элементов нашего списка постов, чтобы вносить изменения
//в то, что отображается на экране
class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    //сравнивает два элемента на идентичность (напр. совпадают ли id у них обоих)
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id

    //сравнивает два идентичных элемента (т.е. вызывается после areItemsTheSame, если он вернул true)
    //на предмет содержания идентичных данных
    //если данные не совпадут - элемент на экране обновится
    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
}