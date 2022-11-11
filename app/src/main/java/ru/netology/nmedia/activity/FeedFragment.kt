package ru.netology.nmedia.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
//            ownerProducer = ::requireParentFragment
            ownerProducer = { this.requireParentFragment() }
        )

        //setContentView во фрагменте не вызывается, вместо этого onCreateView
        //возвращает вьюшку (return binding.root в конце)
        //setContentView(binding.root)

        //втыкаем взаимодействие с постом (лайк, шара, редактирование) через класс PostInteraction,
        //в котором всё это описано
        val adapter = PostAdapter(listener = PostInteraction(vm = viewModel, view = binding.root))

        //получаем список постов
        binding.postList.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }

        //перематываем на самый верх в случае, если количество постов увеличилось
        //в сравнении с прошлым обновлением (т.е. если только что добавили 1 пост)
        viewModel.postsInFeed.observe(viewLifecycleOwner) {
            binding.postList.scrollToPosition(0)
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

        return binding.root
    }
}