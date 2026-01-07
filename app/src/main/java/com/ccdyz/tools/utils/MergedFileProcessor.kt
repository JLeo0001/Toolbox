// app/src/main/java/com/ccdyz/tools/utils/MergedFileProcessor.kt
package com.ccdyz.tools.utils

import android.content.Context
import android.net.Uri
import com.ccdyz.tools.ui.filemerger.OperationResult
import com.ccdyz.tools.ui.filemerger.OutputFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * MERGEDv3格式文件处理器 - 完全兼容网页版
 *
 * 文件格式结构：
 * [视频数据][附件数据][文件名长度4字节][文件名][视频大小8字节][附件大小8字节][魔术字节"MERGEDv3"8字节]
 */
class MergedFileProcessor {

    companion object {
        // 网页版兼容的格式定义
        private const val MAGIC_BYTES_V3 = "MERGEDv3"
        private const val MAGIC_LENGTH = 8
        private const val SIZE_LENGTH = 8  // 使用8字节长整型
        private const val FILENAME_LENGTH_SIZE = 4  // 文件名长度字段4字节
        private const val CHUNK_SIZE = 1024 * 1024  // 1MB 分块大小
        private const val MAX_FILENAME_LENGTH = 255

        // 最小文件大小检查
        private val MIN_FILE_SIZE = MAGIC_LENGTH + SIZE_LENGTH * 2 + FILENAME_LENGTH_SIZE + 1
    }

    /**
     * 内存模式合并文件 - 兼容网页版格式
     */
    suspend fun mergeFilesMemory(
        context: Context,
        videoUri: Uri,
        attachUri: Uri,
        onProgress: (Float, String, Long) -> Unit,
        onDebug: (String) -> Unit
    ): OperationResult = withContext(Dispatchers.IO) {
        try {
            onDebug("🚀 开始内存模式合并（网页版兼容格式）")

            // 读取文件
            onProgress(0.1f, "读取视频文件...", 0L)
            val videoData = FileUtils.readUriBytes(context, videoUri)
            onDebug("✅ 视频文件读取完成: ${videoData.size} 字节")

            onProgress(0.4f, "读取附件文件...", videoData.size.toLong())
            val attachData = FileUtils.readUriBytes(context, attachUri)
            onDebug("✅ 附件文件读取完成: ${attachData.size} 字节")

            // 获取并清理文件名
            val originalFilename = FileUtils.getFileNameSafe(context, attachUri)
            val cleanedFilename = cleanFilename(originalFilename)
            val filenameBytes = cleanedFilename.toByteArray(Charsets.UTF_8)

            onDebug("📝 文件名处理: \"$originalFilename\" -> \"$cleanedFilename\"")

            if (filenameBytes.size > MAX_FILENAME_LENGTH) {
                throw Exception("文件名过长: ${filenameBytes.size} > $MAX_FILENAME_LENGTH")
            }

            onProgress(0.7f, "创建MERGEDv3格式...", (videoData.size + attachData.size).toLong())

            // 计算总大小 - 网页版格式
            val metadataSize = FILENAME_LENGTH_SIZE + filenameBytes.size + SIZE_LENGTH * 2 + MAGIC_LENGTH
            val totalSize = videoData.size + attachData.size + metadataSize

            onDebug("📊 格式信息:")
            onDebug("  - 视频数据: ${videoData.size} 字节")
            onDebug("  - 附件数据: ${attachData.size} 字节")
            onDebug("  - 文件名: \"$cleanedFilename\" (${filenameBytes.size} 字节)")
            onDebug("  - 元数据: $metadataSize 字节")
            onDebug("  - 总大小: $totalSize 字节")

            // 创建输出缓冲区
            val outputStream = ByteArrayOutputStream(totalSize)

            // 网页版格式写入顺序：
            // 1. 视频数据
            outputStream.write(videoData)
            onDebug("✅ 写入视频数据: ${videoData.size} 字节")

            // 2. 附件数据
            outputStream.write(attachData)
            onDebug("✅ 写入附件数据: ${attachData.size} 字节")

            // 3. 文件名长度（4字节，小端序）
            val filenameLengthBuffer = ByteBuffer.allocate(FILENAME_LENGTH_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(filenameBytes.size)
                .array()
            outputStream.write(filenameLengthBuffer)
            onDebug("✅ 写入文件名长度: ${filenameBytes.size}")

            // 4. 文件名
            outputStream.write(filenameBytes)
            onDebug("✅ 写入文件名: \"$cleanedFilename\"")

            // 5. 视频大小（8字节，小端序）
            val videoSizeBuffer = ByteBuffer.allocate(SIZE_LENGTH)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(videoData.size.toLong())
                .array()
            outputStream.write(videoSizeBuffer)
            onDebug("✅ 写入视频大小: ${videoData.size}")

            // 6. 附件大小（8字节，小端序）
            val attachSizeBuffer = ByteBuffer.allocate(SIZE_LENGTH)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(attachData.size.toLong())
                .array()
            outputStream.write(attachSizeBuffer)
            onDebug("✅ 写入附件大小: ${attachData.size}")

            // 7. 魔术字节
            val magicBytes = MAGIC_BYTES_V3.toByteArray(Charsets.UTF_8)
            outputStream.write(magicBytes)
            onDebug("✅ 写入魔术字节: \"$MAGIC_BYTES_V3\"")

            onProgress(0.9f, "保存合并文件...", totalSize.toLong())

            // 保存文件
            val outputFileName = generateOutputFileName(context, videoUri, "_merged_v3")
            val (outputUri, folderPath) = FileUtils.saveToDownloads(context, outputStream.toByteArray(), outputFileName)

            onProgress(1.0f, "合并完成！", totalSize.toLong())
            onDebug("🎉 内存模式合并成功完成")
            onDebug("📁 输出文件夹: $folderPath")

            OperationResult(
                success = true,
                message = "文件合并成功！兼容网页版MERGEDv3格式",
                outputFiles = listOf(
                    OutputFile(
                        name = outputFileName,
                        size = totalSize.toLong(),
                        uri = outputUri,
                        description = "MERGEDv3格式合并文件",
                        folderPath = folderPath
                    )
                )
            )

        } catch (e: Exception) {
            onDebug("❌ 内存模式合并失败: ${e.message}")
            OperationResult(
                success = false,
                message = "合并失败: ${e.message}",
                errorDetails = e.stackTraceToString()
            )
        }
    }

    /**
     * 流式模式合并文件 - 兼容网页版格式
     */
    suspend fun mergeFilesStream(
        context: Context,
        videoUri: Uri,
        attachUri: Uri,
        onProgress: (Float, String, Long) -> Unit,
        onDebug: (String) -> Unit
    ): OperationResult = withContext(Dispatchers.IO) {
        try {
            onDebug("🚀 开始流式模式合并（网页版兼容格式）")

            val videoSize = FileUtils.getFileSize(context, videoUri)
            val attachSize = FileUtils.getFileSize(context, attachUri)

            // 获取并清理文件名
            val originalFilename = FileUtils.getFileNameSafe(context, attachUri)
            val cleanedFilename = cleanFilename(originalFilename)
            val filenameBytes = cleanedFilename.toByteArray(Charsets.UTF_8)

            onDebug("📊 流式处理信息:")
            onDebug("  - 视频文件: $videoSize 字节")
            onDebug("  - 附件文件: $attachSize 字节")
            onDebug("  - 文件名: \"$cleanedFilename\" (${filenameBytes.size} 字节)")

            if (filenameBytes.size > MAX_FILENAME_LENGTH) {
                throw Exception("文件名过长: ${filenameBytes.size} > $MAX_FILENAME_LENGTH")
            }

            val metadataSize = FILENAME_LENGTH_SIZE + filenameBytes.size + SIZE_LENGTH * 2 + MAGIC_LENGTH
            val totalSize = videoSize + attachSize + metadataSize

            // 创建临时文件用于流式写入
            val tempFile = File.createTempFile("merge_", ".tmp", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            try {
                var processedSize = 0L

                // 1. 流式复制视频文件
                onProgress(0.1f, "流式处理视频文件...", processedSize)
                processedSize += copyFileStream(context, videoUri, outputStream) { processed ->
                    onProgress(0.1f + (processed.toFloat() / totalSize) * 0.4f,
                        "处理视频文件...", processedSize + processed)
                }
                onDebug("✅ 视频文件流式写入完成")

                // 2. 流式复制附件文件
                onProgress(0.5f, "流式处理附件文件...", processedSize)
                val attachProcessed = copyFileStream(context, attachUri, outputStream) { processed ->
                    onProgress(0.5f + (processed.toFloat() / totalSize) * 0.3f,
                        "处理附件文件...", processedSize + processed)
                }
                processedSize += attachProcessed
                onDebug("✅ 附件文件流式写入完成")

                // 3. 写入元数据（网页版格式）
                onProgress(0.8f, "写入格式元数据...", processedSize)

                // 文件名长度（4字节，小端序）
                val filenameLengthBuffer = ByteBuffer.allocate(FILENAME_LENGTH_SIZE)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(filenameBytes.size)
                    .array()
                outputStream.write(filenameLengthBuffer)

                // 文件名
                outputStream.write(filenameBytes)

                // 视频大小（8字节，小端序）
                val videoSizeBuffer = ByteBuffer.allocate(SIZE_LENGTH)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putLong(videoSize)
                    .array()
                outputStream.write(videoSizeBuffer)

                // 附件大小（8字节，小端序）
                val attachSizeBuffer = ByteBuffer.allocate(SIZE_LENGTH)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putLong(attachSize)
                    .array()
                outputStream.write(attachSizeBuffer)

                // 魔术字节
                val magicBytes = MAGIC_BYTES_V3.toByteArray(Charsets.UTF_8)
                outputStream.write(magicBytes)

                onDebug("✅ 元数据写入完成")

                outputStream.flush()
                outputStream.close()

                onProgress(0.9f, "保存最终文件...", totalSize)

                // 移动到Downloads目录
                val outputFileName = generateOutputFileName(context, videoUri, "_merged_v3")
                val finalBytes = tempFile.readBytes()
                val (outputUri, folderPath) = FileUtils.saveToDownloads(context, finalBytes, outputFileName)

                onProgress(1.0f, "流式合并完成！", totalSize)
                onDebug("🎉 流式模式合并成功完成")
                onDebug("📁 输出文件夹: $folderPath")

                OperationResult(
                    success = true,
                    message = "流式合并成功！兼容网页版MERGEDv3格式",
                    outputFiles = listOf(
                        OutputFile(
                            name = outputFileName,
                            size = totalSize,
                            uri = outputUri,
                            description = "MERGEDv3格式合并文件（流式处理）",
                            folderPath = folderPath
                        )
                    )
                )

            } finally {
                try { outputStream.close() } catch (e: Exception) { }
                tempFile.delete()
            }

        } catch (e: Exception) {
            onDebug("❌ 流式模式合并失败: ${e.message}")
            OperationResult(
                success = false,
                message = "流式合并失败: ${e.message}",
                errorDetails = e.stackTraceToString()
            )
        }
    }

    /**
     * 拆分MERGEDv3格式文件 - 兼容网页版格式
     */
    suspend fun splitMergedFile(
        context: Context,
        mergedUri: Uri,
        onProgress: (Float, String, Long) -> Unit,
        onDebug: (String) -> Unit
    ): OperationResult = withContext(Dispatchers.IO) {
        try {
            onDebug("🔧 开始拆分MERGEDv3格式文件（网页版兼容）")

            val fileSize = FileUtils.getFileSize(context, mergedUri)
            onDebug("📊 文件大小: $fileSize 字节")

            onProgress(0.1f, "验证文件格式...", 0L)

            // 1. 验证最小文件大小
            if (fileSize < MIN_FILE_SIZE) {
                throw Exception("文件太小，不是有效的MERGEDv3格式: $fileSize < $MIN_FILE_SIZE")
            }

            // 读取整个文件到内存（用于固定位置解析）
            val allData = FileUtils.readUriBytes(context, mergedUri)
            onDebug("✅ 文件读取完成，开始解析")

            // 2. 读取魔术字节（文件末尾8字节）
            onProgress(0.2f, "读取格式标识...", 0L)
            val magicStartPos = allData.size - MAGIC_LENGTH
            val magicBytes = allData.sliceArray(magicStartPos until allData.size)
            val magicString = String(magicBytes, Charsets.UTF_8)

            onDebug("🎯 魔术字节检测: \"$magicString\"")

            if (magicString != MAGIC_BYTES_V3) {
                throw Exception("魔术字节验证失败: 期望\"$MAGIC_BYTES_V3\", 实际\"$magicString\"")
            }

            // 3. 读取附件大小（魔术字节前8字节）
            onProgress(0.3f, "读取文件大小信息...", 0L)
            val attachSizeStartPos = magicStartPos - SIZE_LENGTH
            val attachSizeBytes = allData.sliceArray(attachSizeStartPos until magicStartPos)
            val attachSize = ByteBuffer.wrap(attachSizeBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .long

            onDebug("📎 附件大小: $attachSize 字节")

            // 4. 读取视频大小（附件大小前8字节）
            val videoSizeStartPos = attachSizeStartPos - SIZE_LENGTH
            val videoSizeBytes = allData.sliceArray(videoSizeStartPos until attachSizeStartPos)
            val videoSize = ByteBuffer.wrap(videoSizeBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .long

            onDebug("🎬 视频大小: $videoSize 字节")

            // 5. 验证大小合理性
            if (videoSize <= 0 || attachSize <= 0) {
                throw Exception("文件大小异常: 视频=$videoSize, 附件=$attachSize")
            }

            if (videoSize + attachSize >= fileSize) {
                throw Exception("文件大小验证失败: 数据大小超过文件大小")
            }

            // 6. 读取文件名信息
            onProgress(0.4f, "读取文件名信息...", 0L)
            val metadataStart = (videoSize + attachSize).toInt()

            // 读取文件名长度
            val filenameLengthBytes = allData.sliceArray(metadataStart until metadataStart + FILENAME_LENGTH_SIZE)
            val filenameLength = ByteBuffer.wrap(filenameLengthBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .int

            onDebug("📝 文件名长度: $filenameLength")

            if (filenameLength <= 0 || filenameLength > MAX_FILENAME_LENGTH) {
                throw Exception("文件名长度异常: $filenameLength")
            }

            // 读取文件名
            val filenameStartPos = metadataStart + FILENAME_LENGTH_SIZE
            val filenameBytes = allData.sliceArray(filenameStartPos until filenameStartPos + filenameLength)
            val filename = String(filenameBytes, Charsets.UTF_8)

            onDebug("📝 文件名: \"$filename\"")

            // 7. 验证整体文件结构
            val expectedSize = videoSize + attachSize + FILENAME_LENGTH_SIZE +
                    filenameLength + SIZE_LENGTH * 2 + MAGIC_LENGTH
            if (expectedSize != fileSize) {
                throw Exception("文件结构验证失败: 期望$expectedSize, 实际$fileSize")
            }

            onProgress(0.5f, "提取视频文件...", 0L)

            // 8. 提取视频文件
            val videoData = allData.sliceArray(0 until videoSize.toInt())
            onDebug("✅ 视频文件提取完成: ${videoData.size} 字节")

            onProgress(0.7f, "提取附件文件...", videoSize)

            // 9. 提取附件文件
            val attachData = allData.sliceArray(videoSize.toInt() until (videoSize + attachSize).toInt())
            onDebug("✅ 附件文件提取完成: ${attachData.size} 字节")

            onProgress(0.9f, "保存提取的文件...", videoSize + attachSize)

            // 10. 保存提取的文件
            val originalVideoName = FileUtils.getFileNameWithoutExtension(context, mergedUri)
            val videoExtension = getVideoExtension(originalVideoName)
            val videoFileName = "${originalVideoName.replace("_merged_v3", "")}$videoExtension"

            val (videoUri, videoFolderPath) = FileUtils.saveToDownloads(context, videoData, videoFileName)
            val (attachUri, attachFolderPath) = FileUtils.saveToDownloads(context, attachData, filename)

            onProgress(1.0f, "拆分完成！", videoSize + attachSize)
            onDebug("🎉 MERGEDv3格式拆分成功完成")
            onDebug("📁 视频输出文件夹: $videoFolderPath")
            onDebug("📁 附件输出文件夹: $attachFolderPath")

            OperationResult(
                success = true,
                message = "MERGEDv3格式拆分成功！兼容网页版",
                outputFiles = listOf(
                    OutputFile(
                        name = videoFileName,
                        size = videoSize,
                        uri = videoUri,
                        description = "原始视频文件",
                        folderPath = videoFolderPath
                    ),
                    OutputFile(
                        name = filename,
                        size = attachSize,
                        uri = attachUri,
                        description = "隐藏的附件文件",
                        folderPath = attachFolderPath
                    )
                )
            )

        } catch (e: Exception) {
            onDebug("❌ MERGEDv3拆分失败: ${e.message}")
            OperationResult(
                success = false,
                message = "拆分失败: ${e.message}",
                errorDetails = e.stackTraceToString()
            )
        }
    }

    /**
     * 检测是否为MERGEDv3格式 - 兼容网页版
     */
    suspend fun detectMergedFormat(
        context: Context,
        uri: Uri,
        onDebug: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val fileSize = FileUtils.getFileSize(context, uri)
            onDebug("🔍 格式检测: 文件大小=$fileSize")

            if (fileSize < MAGIC_LENGTH) {
                onDebug("❌ 文件太小，无法包含魔术字节")
                return@withContext false
            }

            // 读取文件末尾的魔术字节
            val allData = FileUtils.readUriBytes(context, uri)

            val magicStartPos = allData.size - MAGIC_LENGTH
            val magicBytes = allData.sliceArray(magicStartPos until allData.size)
            val magicString = String(magicBytes, Charsets.UTF_8)

            val isValid = magicString == MAGIC_BYTES_V3
            onDebug("🎯 魔术字节: \"$magicString\" => ${if (isValid) "✅ 有效" else "❌ 无效"}")

            isValid
        } catch (e: Exception) {
            onDebug("❌ 格式检测失败: ${e.message}")
            false
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 清理文件名，移除非法字符
     */
    private fun cleanFilename(filename: String): String {
        if (filename.isBlank()) return "unknown_file.bin"

        return filename
            .replace(Regex("[<>:\"/\\\\|?*\\x00-\\x1f]"), "_") // 替换非法字符
            .removePrefix(".") // 移除开头的点
            .take(MAX_FILENAME_LENGTH) // 限制长度
            .ifBlank { "unknown_file.bin" }
    }

    /**
     * 流式复制文件
     */
    private fun copyFileStream(
        context: Context,
        sourceUri: Uri,
        outputStream: OutputStream,
        onProgress: (Long) -> Unit
    ): Long {
        val inputStream = context.contentResolver.openInputStream(sourceUri)!!
        val buffer = ByteArray(CHUNK_SIZE)
        var totalCopied = 0L

        try {
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalCopied += bytesRead
                onProgress(totalCopied)
            }
        } finally {
            inputStream.close()
        }

        return totalCopied
    }

    /**
     * 生成输出文件名
     */
    private fun generateOutputFileName(context: Context, videoUri: Uri, suffix: String): String {
        val originalName = FileUtils.getFileNameWithoutExtension(context, videoUri)
        val extension = FileUtils.getFileExtension(context, videoUri)
        return "$originalName$suffix$extension"
    }

    /**
     * 获取视频文件扩展名
     */
    private fun getVideoExtension(filename: String): String {
        val commonVideoExts = listOf(".mp4", ".mkv", ".avi", ".mov", ".wmv", ".webm")
        for (ext in commonVideoExts) {
            if (filename.lowercase().endsWith(ext)) {
                return ext
            }
        }
        return ".mp4" // 默认扩展名
    }
}