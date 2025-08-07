package mc.arch.skin.bridge.cache

interface CacheProvider {
    /**
     * Store a value with an optional TTL
     */
    fun put(key: CacheKey, id: String, value: ByteArray, ttlMinutes: Long? = null): Boolean

    /**
     * Retrieve a value by key
     */
    fun get(key: CacheKey, id: String): ByteArray?

    /**
     * Check if a key exists
     */
    fun exists(key: CacheKey, id: String): Boolean

    /**
     * Delete a key
     */
    fun delete(key: CacheKey, id: String): Boolean

    /**
     * Clear all cached data
     */
    fun clear(): Boolean

    /**
     * Close/cleanup resources
     */
    fun close()
}
