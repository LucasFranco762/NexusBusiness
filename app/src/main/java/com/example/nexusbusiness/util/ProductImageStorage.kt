package com.example.nexusbusiness.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max

object ProductImageStorage {
    private const val PRODUCT_IMAGES_DIRECTORY = "product_images"
    private const val CAMERA_CACHE_DIRECTORY = "product_camera"
    private const val MAX_STORED_DIMENSION = 1600
    private const val JPEG_QUALITY = 88
    private const val ORPHAN_GRACE_PERIOD_MS = 24 * 60 * 60 * 1000L

    fun createCameraFile(context: Context): File {
        val directory = File(context.cacheDir, CAMERA_CACHE_DIRECTORY).apply { mkdirs() }
        return File(directory, "capture_${UUID.randomUUID()}.jpg")
    }

    suspend fun persistCapturedPhoto(context: Context, sourceFile: File): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val bitmap = decodeOrientedBitmap(
                    path = sourceFile.absolutePath,
                    requestedWidth = MAX_STORED_DIMENSION,
                    requestedHeight = MAX_STORED_DIMENSION
                ) ?: return@runCatching null

                val imagesDirectory = File(context.filesDir, PRODUCT_IMAGES_DIRECTORY).apply { mkdirs() }
                val destination = File(imagesDirectory, "product_${UUID.randomUUID()}.jpg")
                FileOutputStream(destination).use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output))
                }
                bitmap.recycle()
                destination.absolutePath
            }.getOrNull().also {
                sourceFile.delete()
            }
        }

    fun decodeOrientedBitmap(path: String, requestedWidth: Int, requestedHeight: Int): Bitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        if (!file.isFile) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds, requestedWidth, requestedHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = runCatching { BitmapFactory.decodeFile(path, options) }.getOrNull() ?: return null
        val orientation = runCatching {
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    postScale(-1f, 1f)
                    postRotate(270f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    postScale(-1f, 1f)
                    postRotate(90f)
                }
            }
        }
        val oriented = if (matrix.isIdentity) {
            decoded
        } else runCatching {
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        }.getOrNull()?.also { rotated ->
            if (rotated !== decoded) decoded.recycle()
        } ?: decoded

        val largestDimension = max(oriented.width, oriented.height)
        val requestedDimension = max(requestedWidth, requestedHeight)
        if (largestDimension <= requestedDimension) return oriented

        val scale = requestedDimension.toFloat() / largestDimension
        val scaled = Bitmap.createScaledBitmap(
            oriented,
            (oriented.width * scale).toInt().coerceAtLeast(1),
            (oriented.height * scale).toInt().coerceAtLeast(1),
            true
        )
        if (scaled !== oriented) oriented.recycle()
        return scaled
    }

    fun deleteManagedPhoto(context: Context, path: String) {
        if (path.isBlank()) return
        runCatching {
            val imagesDirectory = File(context.filesDir, PRODUCT_IMAGES_DIRECTORY).canonicalFile
            val photo = File(path).canonicalFile
            if (photo.parentFile == imagesDirectory && photo.isFile) {
                photo.delete()
            }
        }
    }

    fun deleteCameraFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf(File::isFile)?.delete() }
    }

    fun cleanupOrphans(context: Context, referencedPaths: Set<String>) {
        val now = System.currentTimeMillis()
        val canonicalReferences = referencedPaths.mapNotNull { path ->
            runCatching { File(path).canonicalPath }.getOrNull()
        }.toSet()

        File(context.filesDir, PRODUCT_IMAGES_DIRECTORY).listFiles()?.forEach { photo ->
            val isOldEnough = now - photo.lastModified() >= ORPHAN_GRACE_PERIOD_MS
            if (photo.isFile && isOldEnough && photo.canonicalPath !in canonicalReferences) {
                photo.delete()
            }
        }
        File(context.cacheDir, CAMERA_CACHE_DIRECTORY).listFiles()?.forEach { capture ->
            if (capture.isFile && now - capture.lastModified() >= ORPHAN_GRACE_PERIOD_MS) {
                capture.delete()
            }
        }
    }

    private fun calculateSampleSize(
        options: BitmapFactory.Options,
        requestedWidth: Int,
        requestedHeight: Int
    ): Int {
        var sampleSize = 1
        val sourceWidth = options.outWidth
        val sourceHeight = options.outHeight
        while (max(sourceWidth / sampleSize, sourceHeight / sampleSize) >
            max(requestedWidth, requestedHeight) * 2
        ) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
