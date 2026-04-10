package top.xjunz.tasker.task.ocr

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * ML Kit 离线 OCR 引擎（bundled 中文模型，不依赖 GMS）
 */
class MlKitOcrProvider : OcrProvider {

    override val name: String = "ML Kit (Offline)"

    private var recognizerInitialized = false

    private val recognizer: TextRecognizer by lazy {
        recognizerInitialized = true
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    override fun isAvailable(): Boolean = true // bundled 模型始终可用

    override suspend fun recognize(bitmap: Bitmap, region: Rect?): OcrResult? {
        val startTime = System.currentTimeMillis()
        val targetBitmap = if (region != null) {
            cropBitmap(bitmap, region) ?: return null
        } else {
            bitmap
        }
        return try {
            val inputImage = InputImage.fromBitmap(targetBitmap, 0)
            val text = suspendCancellableCoroutine<Text?> { cont ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { result -> cont.resume(result) }
                    .addOnFailureListener { _ -> cont.resume(null) }
            } ?: return null

            val blocks = text.textBlocks.map { block ->
                TextBlock(
                    text = block.text,
                    boundingBox = block.boundingBox ?: Rect(),
                    confidence = block.lines.firstOrNull()?.confidence ?: 0f
                )
            }
            OcrResult(
                fullText = text.text,
                blocks = blocks,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            null
        } finally {
            if (region != null && targetBitmap !== bitmap) {
                targetBitmap.recycle()
            }
        }
    }

    override fun release() {
        if (recognizerInitialized) {
            recognizer.close()
        }
    }

    /**
     * 裁剪 Bitmap 到指定区域，边界安全
     */
    internal fun cropBitmap(bitmap: Bitmap, region: Rect): Bitmap? {
        val x = region.left.coerceIn(0, bitmap.width)
        val y = region.top.coerceIn(0, bitmap.height)
        val w = (region.width()).coerceIn(1, bitmap.width - x)
        val h = (region.height()).coerceIn(1, bitmap.height - y)
        return try {
            Bitmap.createBitmap(bitmap, x, y, w, h)
        } catch (e: Exception) {
            null
        }
    }
}
