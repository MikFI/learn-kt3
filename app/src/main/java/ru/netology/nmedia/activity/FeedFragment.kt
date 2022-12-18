package ru.netology.nmedia.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nmedia.FirebaseMsgSvc
import ru.netology.nmedia.PostViewModel
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        //создаём ссылку на viewModel, в которой будут производиться все действия с данными
        //здесь же, в activity, мы их только отображаем
        val viewModel: PostViewModel by viewModels(
            ownerProducer = { this.requireParentFragment() }
        )

        //втыкаем взаимодействие с постом (лайк, шара, редактирование) через класс PostInteraction,
        //в котором всё это описано
        val adapter = PostAdapter(listener = PostInteraction(vm = viewModel, view = binding.root))

        //получаем список постов (или сообщение о том, что список пуст)
        //или отображаем ошибку получения
        //или отображаем анимацию загрузки
        binding.postList.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.errorGroup.isVisible = state.error
            binding.loadPostsGroup.isVisible = state.loading
            binding.emptyFeed.isVisible = state.empty
        }

        //вешаем на кноку "повторить" функцию загрузки списка постов
        binding.retryFeedButton.setOnClickListener {
            viewModel.loadPosts()
        }

        //переход между фрагментами по нажатию кнопки добавления поста
        binding.addPostButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        //получаем созданный ранее токен firebase (если по какой-то причине его потеряли),
        //чтобы не переустанавливать приложение ради получения нового токена
        val fcmTokenTask = FirebaseMessaging.getInstance().token.addOnCompleteListener {}
        if (fcmTokenTask.isSuccessful) {
            Log.d(FirebaseMsgSvc.TAG, "Your firebase token is: ${fcmTokenTask.result}")
        }

        //обновляем список постов (догружаем с сервера) свайпом вниз
        //сразу же отключаем встроенную анимамацию вместе с, собственно, обновлением
        //они у нас свои прописаны
        binding.feedLayout.setOnRefreshListener {
            binding.loadPostsGroup.visibility = View.VISIBLE
            binding.feedLayout.isRefreshing = false
            viewModel.loadPosts()
        }

        return binding.root
    }
}