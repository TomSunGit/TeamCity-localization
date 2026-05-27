package org.jb.tom.tc.localization

import jetbrains.buildServer.web.CsrfCheck
import javax.servlet.http.HttpServletRequest

/**
 * Allow same-origin POST to our /_record endpoint without a CSRF token.
 *
 * The endpoint is read-only-ish: it appends to a server-side file used by
 * the plugin author to discover untranslated strings. There's no privileged
 * action a malicious cross-site post could trigger via it.
 */
class LocalizationCsrfCheck : CsrfCheck {

    override fun isSafe(request: HttpServletRequest): CsrfCheck.CheckResult {
        val uri = request.requestURI ?: return CsrfCheck.UNKNOWN
        return if (uri.endsWith("/app/localization/_record")) {
            CsrfCheck.CheckResult.safe()
        } else {
            CsrfCheck.UNKNOWN
        }
    }

    override fun describe(verbose: Boolean): String =
        "TC-localization: allow POST to /app/localization/_record (translation recording)"
}
