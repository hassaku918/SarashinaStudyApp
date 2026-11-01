package com.sarashina.study.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfHelper {

    suspend fun extractTextFromPdf(
        context: Context,
        uri: Uri,
        maxPages: Int = 10,
        onProgress: ((Int, Int) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalStateException("PDFを開けませんでした")

        val renderer = PdfRenderer(pfd)
        val sb = StringBuilder()
        val pageCount = minOf(renderer.pageCount, maxPages)

        try {
            for (i in 0 until pageCount) {
                onProgress?.invoke(i + 1, pageCount)
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val text = try {
                    OcrHelper.recognizeFromBitmap(bitmap)
                } catch (e: Exception) {
                    ""
                }
                if (text.isNotBlank()) {
                    sb.appendLine("--- ページ ${i + 1} ---")
                    sb.appendLine(text)
                    sb.appendLine()
                }
                bitmap.recycle()
            }
        } finally {
            renderer.close()
            pfd.close()
        }
        sb.toString().trim()
    }

    fun copyToCache(context: Context, uri: Uri, name: String = "temp.pdf"): File {
        val file = File(context.cacheDir, name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}
