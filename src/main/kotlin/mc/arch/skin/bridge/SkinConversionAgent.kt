package mc.arch.skin.bridge

import mc.arch.skin.cache.CacheKey
import mc.arch.skin.cache.CacheProvider
import mc.arch.skin.cache.CachedSkin
import mc.arch.skin.cache.RedisCacheProvider
import net.evilblock.cubed.serializers.Serializers
import org.mineskin.Java11RequestHandler
import org.mineskin.MineSkinClient
import org.mineskin.data.Visibility
import org.mineskin.exception.MineSkinRequestException
import org.mineskin.request.GenerateRequest
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import javax.imageio.ImageIO

class SkinConversionAgent(
    private val config: SkinConversionConfig,
    private val cacheProvider: CacheProvider = RedisCacheProvider
)
{
    private val mineSkinClient: MineSkinClient = MineSkinClient.builder()
        .requestHandler(::Java11RequestHandler)
        .userAgent(config.userAgent)
        .apiKey(config.mineSkinApiKey)
        .build()

    fun convertAndUploadToMineSkin(
        base64Data: String,
        visibility: Visibility = Visibility.PUBLIC,
        width: Int = config.defaultWidth,
        height: Int = config.defaultHeight
    ): CompletableFuture<SkinUploadResult>
    {
        return CompletableFuture.supplyAsync {
            try
            {
                val hash = generateSkinHash(base64Data)
                val pngData = convertABGR8ToPNGBytes(
                    base64Data = base64Data,
                    width = width,
                    height = height
                ) ?: throw RuntimeException("Failed to convert ABGR8 to PNG")

                val request = GenerateRequest
                    .upload(ByteArrayInputStream(pngData))
                    .name("archmc-${hash.take(7)}")
                    .visibility(visibility)

                val skinInfo = mineSkinClient.queue().submit(request)
                    .thenCompose { queueResponse ->
                        val job = queueResponse.job
                        job.waitForCompletion(mineSkinClient)
                    }
                    .thenCompose { jobResponse ->
                        jobResponse.getOrLoadSkin(mineSkinClient)
                    }
                    .join()

                val textureData = skinInfo.texture().data()
                val textureValue = textureData.value()
                val textureSignature = textureData.signature()
                val skinUuid = skinInfo.uuid()

                if (config.cacheEnabled)
                {
                    cacheSkinResponse(hash, textureValue, textureSignature)
                }

                SkinUploadResult(
                    skinInfo = skinInfo,
                    textureValue = textureValue,
                    textureSignature = textureSignature,
                    skinUuid = skinUuid,
                    error = null
                )
            } catch (exception: Exception)
            {
                println("Error during MineSkin upload: ${exception.message}")
                exception.printStackTrace()

                var errorMessage = exception.message ?: "Unknown error"
                if (exception is CompletionException && exception.cause is MineSkinRequestException)
                {
                    val requestException = exception.cause as MineSkinRequestException
                    val response = requestException.response
                    val errorDetails = response.errorOrMessage
                    if (errorDetails.isPresent)
                    {
                        val details = errorDetails.get()
                        errorMessage = "${details.code()}: ${details.message()}"
                    }
                }

                SkinUploadResult(
                    skinInfo = null,
                    textureValue = null,
                    textureSignature = null,
                    skinUuid = null,
                    error = errorMessage
                )
            }
        }
    }

    private fun cacheSkinResponse(skinHash: String, textureValue: String, textureSignature: String)
    {
        val jsonData = Serializers.gson.toJson(
            CachedSkin(
                textureValue,
                textureSignature
            )
        )
        cacheProvider.put(CacheKey.SKIN, skinHash, jsonData.toByteArray(Charsets.UTF_8), config.skinCacheTtlMinutes)
    }

    fun getCachedSkinResponse(skinHash: String): CachedSkin?
    {
        val cachedData = cacheProvider.get(CacheKey.SKIN, skinHash) ?: return null

        val jsonData = String(cachedData, Charsets.UTF_8)
        return Serializers.gson.fromJson(jsonData, CachedSkin::class.java)
    }

    fun generateSkinHash(base64Data: String): String
    {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(base64Data.toByteArray(Charsets.UTF_8))
        return  hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun convertABGR8ToPNGBytes(
        base64Data: String,
        width: Int = config.defaultWidth,
        height: Int = config.defaultHeight
    ): ByteArray?
    {
        return generatePNGBytes(base64Data, width, height)
    }

    private fun generatePNGBytes(base64Data: String, width: Int, height: Int): ByteArray?
    {
        val decodedBytes = Base64.getDecoder().decode(base64Data)

        val expectedSize = width * height * 4
        if (decodedBytes.size != expectedSize)
        {
            println("expected $expectedSize bytes, got ${decodedBytes.size} bytes")
        }

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        var byteIndex = 0
        for (y in 0 until height)
        {
            for (x in 0 until width)
            {
                if (byteIndex + 3 < decodedBytes.size)
                {
                    // ABGR8 format: Alpha, Blue, Green, Red (each 8 bits)
                    val a = decodedBytes[byteIndex].toInt() and 0xFF
                    val b = decodedBytes[byteIndex + 1].toInt() and 0xFF
                    val g = decodedBytes[byteIndex + 2].toInt() and 0xFF
                    val r = decodedBytes[byteIndex + 3].toInt() and 0xFF

                    // Convert to ARGB format for BufferedImage
                    val argb = (a shl 24) or (r shl 16) or (g shl 8) or b

                    image.setRGB(x, y, argb)
                    byteIndex += 4
                }
            }
        }

        // Convert to PNG bytes
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "PNG", outputStream)
        return outputStream.toByteArray()
    }

    fun close()
    {
        cacheProvider.close()
    }
}
