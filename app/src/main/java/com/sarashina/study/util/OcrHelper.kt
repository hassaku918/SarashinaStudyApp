package com.sarashina.study.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min

object OcrHelper {

    private val japaneseRecognizer by lazy {
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    }
    private val latinRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeFromUri(context: Context, uri: Uri): String {
        val original = decodeBitmap(context, uri, maxSize = 2400) ?: run {
            return processImage(InputImage.fromFilePath(context, uri)).text
        }
        val enhanced = enhanceForExamScan(original)
        return recognizeEnhanced(enhanced)
    }

    suspend fun recognizeFromBitmap(bitmap: Bitmap): String {
        return recognizeEnhanced(enhanceForExamScan(bitmap))
    }

    private suspend fun recognizeEnhanced(bitmap: Bitmap): String = coroutineScope {
        val image = InputImage.fromBitmap(bitmap, 0)
        val jpDeferred = async { processImage(image) }
        val laDeferred = async {
            try {
                processLatin(image)
            } catch (_: Exception) {
                null
            }
        }
        val jp = jpDeferred.await()
        val la = laDeferred.await()

        val withUnderlines = annotateUnderlines(bitmap, jp)
        val latinText = la?.text?.trim().orEmpty()

        buildString {
            append(withUnderlines)
            if (latinText.length > 20 && !withUnderlines.contains(latinText.take(30))) {
                append("\n\n【英字OCR補足】\n")
                append(latinText)
            }
        }.trim()
    }

    private suspend fun processImage(image: InputImage): Text =
        suspendCancellableCoroutine { cont ->
            japaneseRecognizer.process(image)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun processLatin(image: InputImage): Text =
        suspendCancellableCoroutine { cont ->
            latinRecognizer.process(image)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private fun annotateUnderlines(bitmap: Bitmap, text: Text): String {
        val sb = StringBuilder()
        val w = bitmap.width
        val h = bitmap.height

        for (block in text.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox
                val lineText = line.text.trim()
                if (lineText.isEmpty()) continue

                val underlined = if (box != null) {
                    detectUnderlineBelow(bitmap, box.left, box.right, box.bottom, w, h)
                } else {
                    false
                }

                if (underlined) {
                    sb.append("【下線】").append(lineText)
                } else {
                    val marked = lineText.replace(Regex("""_{2,}"""), "【下線部】$0")
                    sb.append(marked)
                }
                sb.append('\n')
            }
            sb.append('\n')
        }
        val result = sb.toString().trim()
        return result.ifEmpty { text.text }
    }

    private fun detectUnderlineBelow(
        bitmap: Bitmap,
        left: Int,
        right: Int,
        bottom: Int,
        imgW: Int,
        imgH: Int
    ): Boolean {
        val l = max(0, left)
        val r = min(imgW - 1, right)
        if (r <= l) return false
        val width = r - l
        var darkRuns = 0
        var samples = 0
        for (dy in 1..5) {
            val y = bottom + dy
            if (y >= imgH) break
            var dark = 0
            var count = 0
            var x = l
            while (x <= r) {
                val px = bitmap.getPixel(x, y)
                val gray = ((px shr 16) and 0xFF) * 0.299 +
                    ((px shr 8) and 0xFF) * 0.587 +
                    (px and 0xFF) * 0.114
                count++
                if (gray < 90) dark++
                x += max(1, width / 40)
            }
            samples++
            if (count > 0 && dark.toFloat() / count > 0.35f) darkRuns++
        }
        return samples > 0 && darkRuns >= 2
    }

    fun enhanceForExamScan(src: Bitmap): Bitmap {
        val cm = ColorMatrix(
            floatArrayOf(
                1.4f, 0f, 0f, 0f, -30f,
                0f, 1.4f, 0f, 0f, -30f,
                0f, 0f, 1.4f, 0f, -30f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    fun decodeBitmap(context: Context, uri: Uri, maxSize: Int = 2048): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            var sampleSize = 1
            val maxDim = maxOf(1, max(bounds.outWidth, bounds.outHeight))
            while (maxDim / sampleSize > maxSize) sampleSize *= 2
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }
        } catch (_: Exception) {
            null
        }
    }
}
