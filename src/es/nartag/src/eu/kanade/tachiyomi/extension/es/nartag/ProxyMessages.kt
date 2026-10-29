package eu.kanade.tachiyomi.extension.es.nartag

private const val HTTP_NOT_FOUND = 404

private const val REASON_AD_GATEWAY = "ad-gateway"

/**
 * Message shown by the app when a chapter cannot be read because the site gates it.
 *
 * [error] is the parsed 409 body: the proxy puts `next` at the top level, next to `error`.
 */
fun lockedChapterMessage(
    hash: String,
    proxy: String? = null,
    error: ProxyErrorDto? = null,
): String {
    val next = error?.next ?: error?.error?.next
    val command = next?.command?.takeIf { it.isNotBlank() }
    val unlockUrl = next?.url?.takeIf { it.isNotBlank() }
    val adGateway = next?.reason == REASON_AD_GATEWAY && unlockUrl != null

    return buildString {
        if (adGateway) {
            append("Capítulo bloqueado: el proxy ya tiene la sesión, pero la web pide pasar un captcha una vez.\n\n")
            append("Abre este enlace en el navegador y resuélvelo:\n$unlockUrl\n\n")
        } else {
            append("Capítulo bloqueado: la web no muestra las páginas hasta que lo desbloqueas una vez.\n\n")
            append("1. En el ordenador, ejecuta el comando del proxy:\n   ")
            append(command ?: "node tools/unlock.mjs --hash $hash --out session.json")
            if (unlockUrl != null) {
                append("\n\n2. Abre este enlace en el navegador y resuelve el captcha:\n   $unlockUrl")
            }
            append("\n\n")
        }

        append("Cuando termine, vuelve a intentarlo.")

        error?.error?.message?.takeIf { it.isNotBlank() }?.let { append("\n\nDetalle: $it") }
        next?.guide?.takeIf { it.isNotBlank() }?.let { append("\nGuía: $it") }

        if (proxy == null) {
            append("\n\nSi ya lo has desbloqueado, configura el proxy en los ajustes de la extensión ")
            append("(Proxy URL): la web directa no sirve para los capítulos nuevos.")
        }
    }
}

/**
 * Message for any other error status returned by the proxy.
 */
fun proxyErrorMessage(
    hash: String,
    proxy: String,
    code: Int,
    error: ProxyErrorDto? = null,
): String = buildString {
    append("El proxy ($proxy) respondió $code al pedir el capítulo $hash")

    val serverCode = error?.error?.code?.takeIf { it.isNotBlank() }
    if (serverCode != null) append(" ($serverCode)")
    append(".")

    error?.error?.message?.takeIf { it.isNotBlank() }?.let { append("\n\nDetalle: $it") }

    val retryable = error?.retryable == true || error?.error?.retryable == true || error?.next?.retryable == true
    if (retryable) {
        append("\n\nEs un fallo temporal del proxy: vuelve a intentarlo en un par de minutos.")
    } else if (code == HTTP_NOT_FOUND) {
        append("\n\nEl proxy no encuentra ese capítulo: puede que el enlace haya cambiado.")
    }
}
