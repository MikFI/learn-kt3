package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.Post
import ru.netology.nmedia.PostViewModel
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentViewPostBinding

class ViewPostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentViewPostBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by viewModels(
//            ownerProducer = ::requireParentFragment
            ownerProducer = { this.requireParentFragment() }
        )

        //используем PostViewHolder для заполнения карточки поста в этом фрагменте,
        //поскольку он это делать уже умеет, так зачем выдумывать лишние действия
        //разве что делаем пару изменений в PostInteraction, поскольку будем использовать
        //другие переходы между фрагментами для редактирования и открытия поста
        val viewHolder =
            PostViewHolder(binding.postSingle, PostInteractionSingle(viewModel, binding.root))

        //получаем postId, переданный в виде строки
        //(опять используем созданный ранее объект, которым передавали содержимое поста, поскольку
        //нафига создавать лишние сущности, если можно и так передать)
        val postId = arguments?.textArg?.toLong() ?: -1

        //ищем через livedata нужный нам пост по переданному id и заполняем шаблон карточки содержимым,
        //заодно привязывая функционал
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == postId } ?: run {
                findNavController().navigateUp()
                return@observe
            }
            viewHolder.bind(post)
        }

        return binding.root
    }
}

class PostInteractionSingle(private val vm: PostViewModel, private val view: View) :
    PostInteraction(vm, view) {
    //заменяем переход из ленты на переход из карточки поста
    override fun edit(post: Post) {
        vm.edit(post)
        view.findNavController().navigate(R.id.action_viewPostFragment_to_newPostFragment,
            Bundle().apply { textArg = post.content })
    }

    //ничего не делаем по тыку на контенте поста - мы уже перешли на фрагмент с ним
    override fun openPost(post: Post) {}
}

