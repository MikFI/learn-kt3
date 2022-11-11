package ru.netology.nmedia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.activity.AppActivity
import kotlin.random.Random

class FirebaseMsgSvc : FirebaseMessagingService() {
    private val channelId = "main"

    override fun onCreate() {
        super.onCreate()
        //регистрируем канал для уведомлений, если версия android >= Oreo (8, API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        println(Gson().toJson(remoteMessage))

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        remoteMessage.data["action"]?.let { actionType ->
            //если пришедший к нам action есть в списке, то обрабатываем его,
            //в противном же случае выводим заглушку вместо обработки уведомления
            //(хотя, пожалуй, можно было бы просто игнорировать)
            if (Action.values().map { it.name }.contains(actionType)) {
                when (Action.valueOf(actionType)) {
                    //десериализуем полученную строку content в датакласс Like
                    Action.LIKE -> handleLike(
                        Gson().fromJson(
                            remoteMessage.data["content"],
                            NotificationLike::class.java
                        )
                    )
                    Action.NEWPOST -> handleNewPost(
                        Gson().fromJson(
                            remoteMessage.data["content"],
                            NotificationPost::class.java
                        )
                    )
                }
            } else {
                handleUnknownNotification()
            }
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    //вызывается ТОЛЬКО при получении нового токена
    override fun onNewToken(token: String) {
        Log.d(TAG, "New firebase token: $token")
    }

    //обрабатываем уведомление о лайке
    private fun handleLike(like: NotificationLike) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notification_like, like.userName, like.postAuthor))
            .build()

        //выводим уведомление в шторку
        //случайный номер - id уведомления, чтобы не наклыдывались друг на друга, если номер будет одинаковый
        NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
    }

    //обрабатываем уведомление о новом сообщении
    private fun handleNewPost(post: NotificationPost) {
        //создаём интент на открытие приложения (по тыку на уведомление)
        val intent = Intent(this, AppActivity::class.java)
        //запаковываем его в pendingIntent
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        //добываем иконку из ресурсов для отображения в шторке
        //подгонять ей размер не требуется - API делает это самостоятельно
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_netology_48dp)?.toBitmap()

        //тут выставлялся размер вручную в 48dp
//        val iconSize = (48 * resources.displayMetrics.density).toInt()
//        val scaledIcon = Bitmap.createScaledBitmap(icon!!, iconSize, iconSize, true)

        val notification = NotificationCompat.Builder(this, channelId)
            //крохотная иконка в поле уведомлений вверху экрана
            .setSmallIcon(R.drawable.ic_netology_48dp)
            //заголовок уведомления
            .setContentTitle(getString(R.string.notification_newpost, post.postAuthor))
            //краткий текст уведомления (показывается, пока уведомление в свёрнутом виде)
            .setContentText(post.postContent.take(40) + "...")
            //иконка внутри уведомления
            .setLargeIcon(icon)
            //полный текст + многострочник для разворачивания уведомления
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(post.postContent)
            )
            //"запакованный" интент, выполняемый при тыке на уведомление
            .setContentIntent(pendingIntent)
            //автоматическое закрытие уведомления после тыка на нём
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
    }

    //заглушка на некорректное уведомление
    private fun handleUnknownNotification() {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notification_unknown))
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
    }

    companion object {
        const val TAG = "FirebaseMsgSvc"
    }
}

enum class Action {
    LIKE,
    NEWPOST
}

data class NotificationLike(
    val userId: Int,
    val userName: String,
    val postId: Int,
    val postAuthor: String,
)

data class NotificationPost(
    val postAuthor: String,
    val postContent: String,
)