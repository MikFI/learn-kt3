package ru.netology.nmedia.adapter

import android.graphics.drawable.Animatable
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.Post
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import kotlin.math.floor

//создаёт вьюшку для каждого поста из набора, для отображения на экране в виде списка
//по идее, можно было затолкать в один класс с адаптером, поскольку это, в некотором роде, его дочка
class PostViewHolder(
    private val binding: CardPostBinding,
    private val listener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        updateView(post, binding)
        setListeners(binding, post)
    }

    //суём в разметку (через binding) данные, прилетевшие из post
    private fun updateView(post: Post, binding: CardPostBinding) {

        //получаем анимированный вектор из ресурсов и запускаем анимацию на нём
        //(используется в качестве заглушки на подгружаемых аватарках)
        val loadingIcon = AppCompatResources.getDrawable(binding.root.context,R.drawable.ic_loading_animated_48)
        if (loadingIcon is Animatable) loadingIcon.start()

        //загружаем аватарку поста
        val base_url = "http://10.0.2.2:9999"
        Glide.with(binding.avatar)
            .load("$base_url/avatars/${post.authorAvatar}")
            .error(R.drawable.ic_error_48)
            .placeholder(loadingIcon)
            .timeout(10_000)
            .circleCrop()
            .into(binding.avatar)

        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            postLikesIcon.text = parseCount(post.likes)
//            postSharesIcon.text = parseCount(post.shares)
//            postViewsIcon.text = parseCount(post.views)
            postLikesIcon.isChecked = post.likedByMe
            videoFrame.visibility = View.GONE

            //показываем "превью" видео, если ссылка на него обнаружится в тексте поста
            //а заодно назначаем обработчик этой превьюшке
            content.urls.forEach {
                val url = it.url
                if (url.contains("youtube.com") ||
                    url.contains("youtu.be")
                ) {
                    videoPreview.setOnClickListener { listener.playVideo(url) }
                    videoPlayButton.setOnClickListener { listener.playVideo(url) }
                    videoFrame.visibility = View.VISIBLE
                    return@forEach
                }
            }

            //показываем картинку-аттач, если таковая обнаружится в посте
            if (post.attachment != null){
                Glide.with(binding.imageAttachment)
                    .load("$base_url/images/${post.attachment.url}")
                    .error(R.drawable.ic_error_48)
                    .placeholder(loadingIcon)
                    .timeout(10_000)
                    .into(binding.imageAttachment)
                imageAttachment.visibility = View.VISIBLE
            } else imageAttachment.visibility = View.GONE
        }
    }

    //назначаем обработчики на лайк, шару и пункты меню поста
    private fun setListeners(binding: CardPostBinding, post: Post) {
        binding.apply {
            postLikesIcon.setOnClickListener {
                listener.like(post)
            }
            postSharesIcon.setOnClickListener { listener.share(post) }
            content.setOnClickListener { listener.openPost(post) }
            menuButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                listener.remove(post)
                                true
                            }
                            R.id.edit -> {
                                listener.edit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

private fun parseCount(likes: Int): String {
    var result = likes.toDouble()
    if (likes > 999) {
        if (likes > 999_999) {
            result /= 1000_000
            return if (likes > 9_999_999) Math.round(result).toString() + "M"
            else (floor(result * 10.0) / 10.0).toString() + "M"
        }
        result /= 1_000
        return if (likes > 9_999) Math.round(floor(result)).toString() + "K"
        else (floor(result * 10.0) / 10.0).toString() + "K"
    }
    return likes.toString()
}