package com.sarashina.study.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object OcrHelper {

    private val japaneseRecognizer by lazy {
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    }

    suspend fun recognizeFromUri(context: Context, uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        return processImage(image)
    }

    suspend fun recognizeFromBitmap(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return processImage(image)
    }

    private suspend fun processImage(image: InputImage): String =
        suspendCancellableCoroutine { cont ->
            japaneseRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    cont.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    fun decodeBitmap(context: Context, uri: Uri, maxSize: Int = 2048): Bitmap? {
        return try {
            val input: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(input, null, options)
            input?.close()

            var sampleSize = 1
            val maxDim = maxOf(options.outWidth, options.outHeight)
            while (maxDim / sampleSize > maxSize) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }
        } catch (e: Exception) {
            null
        }
    }
}
