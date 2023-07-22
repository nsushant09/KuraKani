import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.FileProvider
import com.neupanesushant.kurakani.model.Message
import com.neupanesushant.kurakani.model.MessageType
import com.neupanesushant.kurakani.services.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ShareService(private val context: Context?) {

    private val scope = CoroutineScope(Dispatchers.IO)
    fun share(message: Message) {
        when (message.messageType!!) {
            MessageType.IMAGE -> executeImageSharing(message.messageBody ?: "")
            MessageType.AUDIO -> Utils.showToast(context!!, "This feature will be added soon")
            else -> {}
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun executeImageSharing(url: String) {
        scope.launch {
            val bitmap = loadImageFromUrl(url)
            if (bitmap != null) {
                val intent = shareImage(bitmap)
                val chooser = Intent.createChooser(intent, "Send Image Via...")
                context?.let { ctx ->
                    if (ctx !is Activity) {
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (chooser.resolveActivity(ctx.packageManager) != null) {
                        ctx.startActivity(chooser)
                    }
                }
            }
        }
    }

    private suspend fun loadImageFromUrl(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.connect()
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    BitmapFactory.decodeStream(connection.inputStream)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun shareImage(bitmap: Bitmap): Intent {
        return withContext(Dispatchers.IO) {
            val tempFile = File(context!!.cacheDir, "${System.currentTimeMillis()}.jpeg")
            val fos = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()


            val intent = Intent(Intent.ACTION_SEND)
            intent.setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    "${context.applicationContext.packageName}.provider",
                    tempFile
                ),
                "image/jpeg"
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent
        }
    }
}
