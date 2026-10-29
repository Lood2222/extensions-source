package eu.kanade.tachiyomi.extension.es.nartag

import android.content.SharedPreferences
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.annotation.Source
import keiyoushi.network.rateLimit
import keiyoushi.utils.getPreferences
import keiyoushi.utils.parseAs
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.seconds

@Source
abstract class Rncalation :
    HttpSource(),
    ConfigurableSource {

    override val supportsLatest = true

    override val client = network.client.newBuilder()
        .rateLimit(2, 1.seconds)
        .build()

    private val preferences: SharedPreferences = getPreferences()

    private val proxyUrl: String?
        get() = preferences.getString(PROXY_URL_PREF, null)
            ?.trim()
            ?.trimEnd('/')
            ?.takeIf { it.isNotEmpty() }

    /**
     * The proxy can hold a request up to its own deadline (180s by default, a manual unlock up to
     * 60s), so it gets a longer read timeout than the site client.
     */
    private val proxyClient by lazy {
        client.newBuilder()
            .connectTimeout(10.seconds)
            .readTimeout(PROXY_READ_TIMEOUT)
            .build()
    }

    override fun headersBuilder() = super.headersBuilder()
        .add("Referer", "$baseUrl/")

    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/library?sort=views&page=$page", headers)

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()
        val mangas = document.select(".lib-grid a.comic-card").mapNotNull { element ->
            val type = element.selectFirst("span.absolute.top-2.left-2")?.text()
            if (type != null && type.contains("Novel", ignoreCase = true)) {
                return@mapNotNull null
            }
            SManga.create().apply {
                setUrlWithoutDomain(element.absUrl("href"))
                title = element.selectFirst("p.leading-snug")!!.text()
                thumbnail_url = element.selectFirst("img")?.attr("abs:src")
            }
        }
        val hasNextPage = document.selectFirst("a.lib-page-btn--nav:last-child") != null
        return MangasPage(mangas, hasNextPage)
    }

    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/library?sort=updated&page=$page", headers)

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/library".toHttpUrl().newBuilder().apply {
            addQueryParameter("page", page.toString())
            if (query.isNotEmpty()) {
                addQueryParameter("q", query)
            }
            filters.forEach { filter ->
                when (filter) {
                    is SortFilter -> {
                        addQueryParameter("sort", sortOptions[filter.state].value)
                    }
                    is TypeFilter -> {
                        if (filter.state > 0) {
                            addQueryParameter("type", filter.values[filter.state])
                        }
                    }
                    is StatusFilter -> {
                        if (filter.state > 0) {
                            addQueryParameter("status", filter.values[filter.state])
                        }
                    }
                    is GenreFilter -> {
                        if (filter.state > 0) {
                            addQueryParameter("genre", filter.values[filter.state])
                        }
                    }
                    else -> {}
                }
            }
        }.build()
        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response): MangasPage = popularMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga {
        val document = response.asJsoup()
        return SManga.create().apply {
            description = document.selectFirst("div.comic-page-wrap p[class*=text-][^data-astro-cid]")?.text() ?: ""

            val badges = document.select("span.inline-flex.items-center.rounded").map { it.text().lowercase() }
            status = when {
                badges.any { it.contains("emisión") || it.contains("curso") || it.contains("ongoing") } -> SManga.ONGOING
                badges.any { it.contains("completado") || it.contains("completed") } -> SManga.COMPLETED
                badges.any { it.contains("pausa") || it.contains("hiatus") } -> SManga.ON_HIATUS
                badges.any { it.contains("cancelado") || it.contains("cancelled") } -> SManga.CANCELLED
                else -> SManga.UNKNOWN
            }

            genre = document.select("span.inline-flex.items-center.rounded")
                .filter { it.text().lowercase() !in listOf("emisión", "completado", "pausa", "cancelado") }
                .joinToString(", ") { it.text() }

            val groupName = document.selectFirst("a[href^='/groups/']")?.text()
            if (!groupName.isNullOrEmpty()) {
                author = groupName
                artist = groupName
            }

            document.select(".flex.items-baseline.justify-between.gap-2").forEach { row ->
                val label = row.selectFirst("span.text-\\[var\\(--color-text3\\)\\]")?.text()
                val value = row.selectFirst("span.text-\\[var\\(--color-text2\\)\\]")?.text()
                if (label == "Autor") author = value
                if (label == "Arte") artist = value
            }
        }
    }

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> = Observable.fromCallable {
        val slug = manga.url.removeSuffix("/").substringAfterLast("/")
        val allChapters = mutableListOf<SChapter>()
        var page = 1
        do {
            val response = client.newCall(chapterListRequest(slug, page)).execute()
            val chapters = chapterListParse(response)
            if (chapters.isEmpty()) break
            allChapters.addAll(chapters)

            val currentPage = response.header("x-page")?.toIntOrNull() ?: break
            val totalPages = response.header("x-pages")?.toIntOrNull() ?: break

            page++
        } while (currentPage < totalPages)

        return@fromCallable allChapters.toList()
    }

    private fun chapterListRequest(slug: String, page: Int) = GET("$baseUrl/comics/$slug/chapters?page=$page", headers)

    override fun chapterListParse(response: Response): List<SChapter> = response.asJsoup().select("a[data-chapter-id]").mapIndexed { num, it ->
        SChapter.create().apply {
            setUrlWithoutDomain(it.attr("href"))
            chapter_number = it.attr("data-chapter-num").toFloatOrNull() ?: num.toFloat()
            name = it.attr("data-chapter-label").trim().ifEmpty { "Capítulo ${chapter_number.toInt()}" }
            date_upload = it.selectFirst(".text-\\[0\\.65rem\\]")?.let { parseDate(it.text()) } ?: 0L
        }
    }

    override fun fetchPageList(chapter: SChapter): Observable<List<Page>> = Observable.fromCallable {
        val proxy = proxyUrl
        if (proxy != null) {
            proxyPageList(proxy, chapter)
        } else {
            client.newCall(pageListRequest(chapter)).execute().use { pageListParse(it) }
        }
    }

    override fun pageListParse(response: Response): List<Page> {
        val hash = hashFromUrl(response.request.url.toString())
        val document = response.asJsoup()

        if (document.selectFirst("[data-cuty-modal]") != null) {
            throw Exception(lockedChapterMessage(hash))
        }

        val pages = document.select("img.page-img, .page-wrap img").mapIndexed { index, element ->
            val imageUrl = element.attr("abs:data-src").ifEmpty { element.attr("abs:src") }
            Page(index, imageUrl = imageUrl)
        }.filter { !it.imageUrl.isNullOrEmpty() }

        if (pages.isEmpty()) {
            throw Exception(lockedChapterMessage(hash))
        }

        return pages
    }

    private fun proxyPageList(proxy: String, chapter: SChapter): List<Page> {
        val hash = hashFromUrl(chapter.url)

        val requestUrl = try {
            "$proxy/chapter/$hash".toHttpUrl()
        } catch (e: IllegalArgumentException) {
            throw Exception(
                "La URL del proxy no es válida: $proxy. Escríbela completa, empezando por http:// o https:// " +
                    "(Ajustes de la extensión → Proxy URL).",
                e,
            )
        }

        val response = try {
            proxyClient.newCall(GET(requestUrl, headers)).execute()
        } catch (e: SocketTimeoutException) {
            throw Exception(
                "El proxy está tardando demasiado en responder ($proxy). Desbloquear un capítulo puede " +
                    "tardar varios minutos: espera un poco y vuelve a intentarlo.",
                e,
            )
        } catch (e: ConnectException) {
            throw Exception(
                "El proxy no está arrancado o no se puede conectar en $proxy. " +
                    "Arráncalo y comprueba la URL en los ajustes de la extensión (Proxy URL).",
                e,
            )
        } catch (e: UnknownHostException) {
            throw Exception(
                "No se encuentra el proxy en $proxy (nombre de host desconocido). " +
                    "Revisa la URL en los ajustes de la extensión (Proxy URL).",
                e,
            )
        } catch (e: IOException) {
            throw Exception(
                "El proxy no responde en $proxy. Comprueba que está arrancado y que la URL es correcta " +
                    "(Ajustes de la extensión → Proxy URL). Detalle: ${e.message}",
                e,
            )
        }

        response.use {
            val body = it.body.string()
            val error = runCatching { body.parseAs<ProxyErrorDto>(proxyJson) }.getOrNull()

            if (it.code == HTTP_CONFLICT) {
                throw Exception(lockedChapterMessage(hash, proxy, error))
            }

            if (!it.isSuccessful) {
                throw Exception(proxyErrorMessage(hash, proxy, it.code, error))
            }

            val dto = try {
                body.parseAs<ProxyChapterDto>(proxyJson)
            } catch (e: Exception) {
                throw Exception(
                    "El proxy ($proxy) devolvió una respuesta que no se pudo leer para el capítulo $hash. " +
                        "¿Es la URL de un proxy compatible?",
                    e,
                )
            }

            if (dto.pages.isEmpty()) {
                throw Exception(
                    if (dto.unlocked) {
                        "El proxy ($proxy) dice que el capítulo $hash está desbloqueado pero no devolvió " +
                            "ninguna página. Vuelve a intentarlo; si sigue igual, revisa el proxy."
                    } else {
                        "El proxy ($proxy) no devolvió ninguna página para el capítulo $hash y lo da por " +
                            "bloqueado. Desbloquéalo con la herramienta del proxy y vuelve a intentarlo."
                    },
                )
            }

            return dto.pages.mapIndexed { index, imageUrl -> Page(index, imageUrl = imageUrl) }
        }
    }

    private fun hashFromUrl(url: String): String {
        val hash = url.substringBefore('?')
            .substringBefore('#')
            .trimEnd('/')
            .substringAfterLast('/')

        if (hash.isBlank()) {
            throw Exception("No se pudo obtener el hash del capítulo a partir de la URL: $url")
        }

        return hash
    }

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        EditTextPreference(screen.context).apply {
            key = PROXY_URL_PREF
            title = "Proxy URL"
            summary = "URL del proxy de desbloqueo (ej. http://192.168.1.50:8787). " +
                "Vacío = desactivado: los capítulos bloqueados no se podrán leer."
            setDefaultValue("")
            dialogTitle = "Proxy URL"
            dialogMessage = "Escribe la URL del proxy sin la barra final."
        }.also { screen.addPreference(it) }
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    override fun getFilterList(): FilterList = FilterList(
        SortFilter(),
        TypeFilter(),
        StatusFilter(),
        GenreFilter(genresList),
    )

    companion object {
        private const val PROXY_URL_PREF = "proxy_url"

        private const val HTTP_CONFLICT = 409

        // The proxy holds a request up to its own 180s deadline (a manual unlock gets 60s).
        private val PROXY_READ_TIMEOUT = 200.seconds

        private val proxyJson = Json { ignoreUnknownKeys = true }
    }
}
