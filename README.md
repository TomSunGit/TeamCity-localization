# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build          # Compile, test, and package
./gradlew serverPlugin   # Build deployable plugin ZIP (teamcity-localization.zip)
./gradlew deployPlugin   # Build and deploy to ~/.BuildServer/plugins/
./gradlew test           # Run tests
./gradlew clean          # Clean build outputs
```

There are no test files currently in this repository.

## Architecture

This is a JetBrains TeamCity server plugin that performs **client-side UI localization** by injecting a JavaScript translation engine into every page. The current translation data is Chinese (zh_CN).

### Request Flow

1. **`LocalizationPageExtension`** injects `localization.jsp` into every page's `<head>`.
2. The JSP sets `window.__TC_LOCALIZATION__` (locale, API URL, record URL) and loads `localization.js`.
3. **`localization.js`** fetches `GET /app/localization/{locale}.json` (served by **`I18nController`**), which loads the three `.properties` files and returns them as a JSON object with `exact`, `prefix`, and `regex` keys.
4. The JS walks the DOM applying translations, then attaches a `MutationObserver` to handle dynamically inserted nodes.
5. Untranslated strings are batched and POSTed to `/_record`. **`LocalizationCsrfCheck`** exempts this endpoint from TeamCity's CSRF token requirement. **`I18nController`** delegates to **`TranslationRecorder`**, which appends new entries to `<TeamCity Data>/localization/pending.properties`.

### Translation Strategies (applied in order)

| Type | File | Example |
|------|------|---------|
| Exact | `translations/zh_CN/exact.properties` | `"Projects"` → `"项目"` |
| Prefix | `translations/zh_CN/prefix.properties` | `"Build #"` → `"构建 #"` (rest of string appended) |
| Regex | `translations/zh_CN/regex.properties` | `"(\d+) days ago"` → `"$1 天前"` |

### Heuristics — What the JS Skips

`localization.js` intentionally skips translating: form inputs/textareas, `<code>`/`<pre>` blocks, build numbers, project/agent/user names, links to config/build/agent/user pages, `data-test` attributed elements, 2–3 letter abbreviations, URLs, emails, filesystem paths, and sentences ending with `.`.

### Spring Wiring

All beans are declared in `META-INF/build-server-plugin-localization.xml` via constructor injection. There is no annotation-based scanning — every component must be explicitly registered there.

### Plugin Descriptor

`META-INF/build-server-plugin-localization.xml` also serves as the TeamCity plugin descriptor (it contains `<bean>` definitions that TeamCity's Spring context loads). The plugin targets TeamCity 2025.11, Kotlin 2.x, JVM 21.
