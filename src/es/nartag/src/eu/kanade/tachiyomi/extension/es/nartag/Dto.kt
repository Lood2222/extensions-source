package eu.kanade.tachiyomi.extension.es.nartag

import keiyoushi.utils.tryParse
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Response of `GET {proxy}/chapter/{hash}` when the chapter is available.
 */
@Serializable
data class ProxyChapterDto(
    val hash: String? = null,
    val unlocked: Boolean = false,
    val source: String? = null,
    val cached: Boolean = false,
    val comicSlug: String? = null,
    val pages: List<String> = emptyList(),
)

/**
 * Response of `GET {proxy}/chapter/{hash}` when the chapter is locked (HTTP 409).
 *
 * The proxy puts `next` (and `details`/`retryable`) **at the top level**, next to `error`; the
 * nested `error.next` is only kept as a fallback for older shapes.
 */
@Serializable
data class ProxyErrorDto(
    val error: ProxyErrorBodyDto? = null,
    val next: ProxyNextDto? = null,
    val retryable: Boolean = false,
)

@Serializable
data class ProxyErrorBodyDto(
    val code: String? = null,
    val message: String? = null,
    val next: ProxyNextDto? = null,
    val retryable: Boolean? = null,
)

@Serializable
data class ProxyNextDto(
    val type: String? = null,
    val hash: String? = null,
    val command: String? = null,
    val importEndpoint: String? = null,
    val importCommand: String? = null,
    val reason: String? = null,
    val url: String? = null,
    val guide: String? = null,
    val retryable: Boolean? = null,
)

private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

private val nonDigit = Regex("\\D+")

fun parseDate(dateStr: String): Long? {
    val cleaned = dateStr.lowercase()
        .replace('í', 'i').replace('á', 'a')
        .replace('é', 'e').replace('ó', 'o')
        .replace('ú', 'u').replace('ñ', 'n')

    if (!cleaned.startsWith("hace")) return dateFormat.tryParse(dateStr)

    val num = cleaned.replace(nonDigit, "").toIntOrNull() ?: return null
    val cal = Calendar.getInstance()

    return when {
        cleaned.contains("hora") -> cal.apply { add(Calendar.HOUR, -num) }.timeInMillis
        cleaned.contains("sem") -> cal.apply { add(Calendar.DAY_OF_MONTH, -num * 7) }.timeInMillis
        cleaned.contains("dia") -> cal.apply { add(Calendar.DAY_OF_MONTH, -num) }.timeInMillis
        cleaned.contains("mes") -> cal.apply { add(Calendar.MONTH, -num) }.timeInMillis
        cleaned.contains("ano") -> cal.apply { add(Calendar.YEAR, -num) }.timeInMillis
        else -> null
    }
}
