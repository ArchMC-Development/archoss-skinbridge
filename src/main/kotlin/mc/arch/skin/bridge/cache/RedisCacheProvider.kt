package mc.arch.skin.bridge.cache

import gg.scala.commons.ScalaCommons
import io.lettuce.core.HSetExArgs
import java.time.Duration
import java.util.Base64

/**
 * @author Subham
 * @since 8/6/25
 */
object RedisCacheProvider : CacheProvider
{
    override fun put(key: CacheKey, id: String, value: ByteArray, ttlMinutes: Long?): Boolean
    {
        if (ttlMinutes == null)
        {
            ScalaCommons.bundle().globals()
                .redis()
                .sync()
                .hset(
                    "skinbridge:cache:$key",
                    id,
                    Base64.getEncoder().encodeToString(value)
                )
        } else
        {
            ScalaCommons.bundle().globals()
                .redis()
                .sync()
                .hsetex(
                    "skinbridge:cache:$key",
                    HSetExArgs().ex(Duration.ofMinutes(ttlMinutes)),
                    mapOf(
                        id to Base64.getEncoder().encodeToString(value)
                    )
                )
        }

        return true
    }

    override fun get(key: CacheKey, id: String): ByteArray? = ScalaCommons.bundle().globals()
        .redis()
        .sync()
        .hget("skinbridge:cache:$key", id)
        ?.let {
            Base64.getDecoder().decode(it)
        }

    override fun exists(key: CacheKey, id: String): Boolean = ScalaCommons.bundle().globals()
        .redis()
        .sync()
        .hexists("skinbridge:cache:$key", id)

    override fun delete(key: CacheKey, id: String): Boolean
    {
        ScalaCommons.bundle().globals()
            .redis()
            .sync()
            .hdel("skinbridge:cache:$key", id)
        return true
    }

    override fun clear(): Boolean
    {
        return false
    }

    override fun close()
    {

    }
}
