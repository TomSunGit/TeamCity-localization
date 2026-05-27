package org.example.localization

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import java.util.Properties
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class I18nController(
    webControllerManager: WebControllerManager,
) : BaseController() {

    init {
        webControllerManager.registerController(URL_PATTERN, this)
    }

    override fun doHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView? {
        val uri = request.requestURI
        val fileName = uri.substringAfterLast('/')
        val locale = fileName.removeSuffix(".json")

        if (!locale.matches(LOCALE_PATTERN)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid locale")
            return null
        }

        val props = loadTranslations(locale)
        val json = toJson(props)

        response.contentType = "application/json; charset=UTF-8"
        response.characterEncoding = "UTF-8"
        response.setHeader("Cache-Control", "public, max-age=300")
        response.writer.write(json)
        return null
    }

    private fun loadTranslations(locale: String): Properties {
        val props = Properties()
        val resourcePath = "/translations/$locale.properties"
        val stream = javaClass.getResourceAsStream(resourcePath)
        if (stream == null) {
            Loggers.SERVER.warn("[TC-i18n] no translations file for locale '$locale' (expected $resourcePath)")
            return props
        }
        stream.use { props.load(it.reader(Charsets.UTF_8)) }
        return props
    }

    private fun toJson(props: Properties): String {
        val sb = StringBuilder(props.size * 32)
        sb.append('{')
        var first = true
        for ((k, v) in props) {
            if (!first) sb.append(',')
            first = false
            sb.append('"').append(escape(k.toString())).append('"')
            sb.append(':')
            sb.append('"').append(escape(v.toString())).append('"')
        }
        sb.append('}')
        return sb.toString()
    }

    private fun escape(s: String): String {
        val sb = StringBuilder(s.length + 8)
        for (ch in s) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '\b' -> sb.append("\\b")
                '' -> sb.append("\\f")
                else -> if (ch.code < 0x20) sb.append("\\u%04x".format(ch.code)) else sb.append(ch)
            }
        }
        return sb.toString()
    }

    companion object {
        private const val URL_PATTERN = "/app/localization/**"
        private val LOCALE_PATTERN = Regex("[a-zA-Z]{2,3}(_[A-Za-z0-9]+)*")
    }
}
