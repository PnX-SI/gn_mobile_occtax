package fr.geonature.occtax.ui.input.counting

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tinylog.Logger
import java.io.File
import java.util.Date

/**
 * Takes image from camera or gallery.
 *
 *  @author S. Grimault
 */
class TakePhotoLifecycleObserver(
    private val applicationContext: Context,
    private val registry: ActivityResultRegistry
) :
    DefaultLifecycleObserver {

    private val imageMaxSize = 2048
    private val imageQuality = 80
    private lateinit var takeImageResultLauncher: ActivityResultLauncher<Intent>
    private var chosenImageContinuation: CancellableContinuation<File?>? = null
    private var baseFilePath: File? = null
    private var imageUri: Uri? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        takeImageResultLauncher = registry.register(
            "take_image",
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_CANCELED -> {
                    Logger.info { "choose image cancelled by user" }

                    chosenImageContinuation?.resumeWith(Result.success(null))
                }

                AppCompatActivity.RESULT_OK -> {

                    val imageFile = (result.data?.data ?: imageUri)?.let { asFile(it) }
                    Logger.info { "image chosen by user: ${imageFile?.absolutePath}" }

                    chosenImageContinuation?.resumeWith(
                        Result.success(imageFile)
                    )
                }
            }
        }
    }

    /**
     * Takes image from camera or gallery.
     *
     * @return the local image file taken, `null` if user cancelled the operation or if no image was
     * found
     */
    suspend operator fun invoke(from: ImagePicker, basePath: String) =
        suspendCancellableCoroutine { continuation ->
            Logger.info { "choose image from ${from.name.lowercase()}…" }

            chosenImageContinuation = continuation

            baseFilePath = File(basePath).apply {
                mkdirs()
            }

            takeImageResultLauncher.launch(
                if (from == ImagePicker.GALLERY) Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) else Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(
                            applicationContext,
                            "${applicationContext.packageName}.file.provider",
                            File(
                                baseFilePath,
                                "${Date().time}.jpg"
                            )
                        )
                            .also {
                                imageUri = it
                            }
                    )
                }
            )

            continuation.invokeOnCancellation {
                chosenImageContinuation = null
            }
        }

    private fun asFile(uri: Uri): File {
        val filename = (uri.lastPathSegment ?: "${Date().time}").let {
            "${it.substringBeforeLast(".")}.jpg"
        }

        if (!File(
                baseFilePath,
                filename
            ).exists()
        ) {
            applicationContext.contentResolver.openInputStream(uri)
                ?.use { inputStream ->
                    File(
                        baseFilePath,
                        filename
                    ).outputStream()
                        .use { outputSteam ->
                            inputStream.copyTo(outputSteam)
                            outputSteam.flush()
                        }
                }
        }

        return File(
            baseFilePath,
            filename
        ).also {
            resizeAndCompress(it)
        }
    }

    private fun resizeAndCompress(file: File, scaleTo: Int = imageMaxSize) {
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, bmOptions)
        val width = bmOptions.outWidth
        val height = bmOptions.outHeight

        // determine how much to scale down the image
        val scaleFactor = (width / scaleTo).coerceAtLeast(height / scaleTo)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        val resized = BitmapFactory.decodeFile(file.absolutePath, bmOptions) ?: return
        file.outputStream().use {
            resized.compress(Bitmap.CompressFormat.JPEG, imageQuality, it)
            resized.recycle()
            it.flush()
        }
    }

    enum class ImagePicker {
        CAMERA, GALLERY
    }
}