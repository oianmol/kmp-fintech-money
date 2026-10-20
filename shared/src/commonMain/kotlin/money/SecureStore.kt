package money

/**
 * "Share the decisions, keep the trust boundary native."
 *
 * Common code decides *what* to store and *when*. How it's protected is the platform's job:
 * - Android: Android Keystore (hardware-backed), with biometric binding where needed
 * - iOS: Keychain with an explicit accessibility class and access control flags
 *
 * The implementations belong in the native apps (or androidMain / iosMain) and are injected at startup.
 * Deliberately no "portable" implementation here: don't roll your own crypto in commonMain.
 */
interface SecureStore {
    suspend fun put(key: String, value: ByteArray)
    suspend fun get(key: String): ByteArray?
    suspend fun remove(key: String)
}
