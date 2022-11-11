package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import ru.netology.nmedia.Post
import ru.netology.nmedia.PostViewModel
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener

//здесь у нас класс для взаимодействия с постом
//лайки, шары, редактирование и т.п.
open class PostInteraction(private val vm: PostViewModel, private val view: View) :
    OnInteractionListener {
    override fun like(post: Post) {
        vm.likeById(post.id)
    }

    override fun share(post: Post) {
        vm.shareById(post.id)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, post.content)
            type = "text/plain"
        }
        val shareIntent =
            Intent.createChooser(intent, view.resources.getString(R.string.share_post))
        view.context.startActivity(shareIntent)
    }

    override fun edit(post: Post) {
        vm.edit(post)
        view.findNavController().navigate(R.id.action_feedFragment_to_newPostFragment,
            Bundle().apply { textArg = post.content })
    }

    override fun remove(post: Post) {
        vm.removeById(post.id)
    }

    override fun playVideo(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val playIntent =
            Intent.createChooser(intent, view.resources.getString(R.string.share_post))
        view.context.startActivity(playIntent)
    }

    override fun openPost(post: Post) {
        view.findNavController().navigate(R.id.action_feedFragment_to_viewPostFragment,
            Bundle().apply { textArg = post.id.toString() })
    }
}