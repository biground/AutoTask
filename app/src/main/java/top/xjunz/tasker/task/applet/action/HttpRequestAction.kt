/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.action

import top.xjunz.tasker.engine.applet.action.ArgumentAction
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.task.applet.flow.ref.HttpResponseReferent
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * 执行 HTTP 请求的动作。
 *
 * 参数顺序（由 Registry 的 withValueArgument 声明顺序决定）：
 *   args[0] — URL (String)
 *   args[1] — Method (Int: 0=GET, 1=POST)
 *   args[2] — Headers (String?, JSON 格式, 可选)
 *   args[3] — Body (String?, 可选)
 */
class HttpRequestAction : ArgumentAction() {

    companion object {
        private const val CONNECT_TIMEOUT = 30_000
        private const val READ_TIMEOUT = 30_000
        /** 最大读取 1 MB */
        private const val MAX_RESPONSE_SIZE = 1024 * 1024

        private const val METHOD_GET = 0
        private const val METHOD_POST = 1

        private val ALLOWED_SCHEMES = setOf("http", "https")
    }

    override suspend fun doAction(args: Array<Any?>, runtime: TaskRuntime): AppletResult {
        val url = args[0] as? String ?: return AppletResult.EMPTY_FAILURE
        val methodInt = args[1] as? Int ?: METHOD_GET
        val headersJson = args[2] as? String
        val body = args[3] as? String

        // URL scheme 安全校验：仅允许 http / https
        val parsedUrl = try {
            URL(url)
        } catch (_: Exception) {
            return AppletResult.EMPTY_FAILURE
        }
        if (parsedUrl.protocol?.lowercase() !in ALLOWED_SCHEMES) {
            return AppletResult.EMPTY_FAILURE
        }

        // SSRF 防护：禁止访问私有网络地址
        val host = parsedUrl.host ?: return AppletResult.EMPTY_FAILURE
        val addr = try {
            java.net.InetAddress.getByName(host)
        } catch (_: Exception) {
            return AppletResult.EMPTY_FAILURE
        }
        if (addr.isLoopbackAddress || addr.isLinkLocalAddress || addr.isSiteLocalAddress) {
            return AppletResult.EMPTY_FAILURE
        }

        return try {
            val connection = parsedUrl.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = CONNECT_TIMEOUT
                connection.readTimeout = READ_TIMEOUT
                connection.requestMethod = if (methodInt == METHOD_POST) "POST" else "GET"

                // 设置请求头
                if (!headersJson.isNullOrBlank()) {
                    try {
                        val json = JSONObject(headersJson)
                        json.keys().forEach { key ->
                            connection.setRequestProperty(key, json.getString(key))
                        }
                    } catch (_: Exception) {
                        // Headers 解析失败，忽略
                    }
                }

                // 写入请求体
                if (methodInt == METHOD_POST && !body.isNullOrBlank()) {
                    connection.doOutput = true
                    connection.outputStream.use { os ->
                        os.write(body.toByteArray(Charsets.UTF_8))
                    }
                }

                val statusCode = connection.responseCode

                // 读取响应体，限制大小
                val responseBody = readResponseBody(connection)

                val referent = HttpResponseReferent(statusCode, responseBody)
                referent.asResult()
            } finally {
                connection.disconnect()
            }
        } catch (_: IOException) {
            AppletResult.EMPTY_FAILURE
        }
    }

    /**
     * 读取响应体，最多读取 [MAX_RESPONSE_SIZE] 字节。
     */
    private fun readResponseBody(connection: HttpURLConnection): String {
        val inputStream = try {
            connection.inputStream
        } catch (_: IOException) {
            connection.errorStream
        } ?: return ""

        val sb = StringBuilder()
        var totalBytes = 0
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val lineBytes = line!!.toByteArray(Charsets.UTF_8).size + 1 // +1 换行符
                if (totalBytes + lineBytes > MAX_RESPONSE_SIZE) {
                    // 截取到限制处
                    val remaining = MAX_RESPONSE_SIZE - totalBytes
                    if (remaining > 0) {
                        sb.append(line!!.substring(0, minOf(remaining, line!!.length)))
                    }
                    break
                }
                if (sb.isNotEmpty()) sb.append('\n')
                sb.append(line)
                totalBytes += lineBytes
            }
        }
        return sb.toString()
    }
}
