package top.xjunz.tasker.task.ocr

/**
 * 云端 OCR Provider 抽象基类
 *
 * 后续支持的云端引擎：
 * - Google Cloud Vision
 * - 腾讯云 OCR
 * - PaddleOCR Lite（本地轻量模型）
 *
 * 各实现需提供 apiKey 和 endpoint，并实现 recognize() 网络调用。
 * 当前为占位实现，不做具体网络调用。
 */
abstract class CloudOcrProvider : OcrProvider {

    /** API Key 配置 */
    abstract val apiKey: String

    /** API 端点 URL */
    abstract val endpoint: String

    override fun isAvailable(): Boolean = apiKey.isNotBlank()
}
