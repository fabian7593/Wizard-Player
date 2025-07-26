package com.example.vlc.utils

object LanguageMatcher {

    private val accessibleCodeSuffixes = listOf("cc", "sdh", "impaired",  "sordos", "hearing", "for hearing", "subtitles for the deaf")

    private val languageAliases = mapOf(
        "es" to listOf(
            "es-mx", "latino", "espanol latino", "es-lat", "es-la", "latam", "latin", "latin american", "spanish latin",
            "es", "es-es", "spanish", "español", "esp", "espa", "castellano"
        ),
        "es-es" to listOf(
            "es-es", "españa", "castellano", "espanol españa", "spanish spain"
        ),
        "es-mx" to listOf(
            "es-mx", "latino", "latam", "espanol latino", "spanish latin", "latin american"
        ),
        "en" to listOf(
            "en", "english", "eng", "en-us", "en-gb", "ingles", "anglès"
        ),
        "fr" to listOf(
            "fr", "french", "français", "francais", "france", "fr-be", "fr-ca"
        ),
        "pt" to listOf(
            "pt", "portuguese", "português", "por", "pt-br", "portugues", "brasileiro"
        ),
        "de" to listOf(
            "de", "german", "deutsch", "alemán"
        ),
        "it" to listOf(
            "it", "italian", "italiano"
        ),
        "ja" to listOf(
            "ja", "japanese", "日本語", "japonés"
        ),
        "ko" to listOf(
            "ko", "korean", "한국어", "coreano"
        ),
        "zh" to listOf(
            "zh", "chinese", "中文", "mandarín", "mandarin", "zh-cn", "zh-hk"
        )
    )

    fun matchesLanguage(trackName: String, preference: String): Boolean {
        val normalizedName = trackName.lowercase()
        val aliases = languageAliases.entries
            .firstOrNull { preference.lowercase().startsWith(it.key) }
            ?.value ?: return false

        return aliases.any { normalizedName.contains(it) }
    }


    fun detectLanguageCode(trackName: String, preference: String = ""): String? {
        val normalizedName = trackName.lowercase()
        val words = Regex("""\w+""").findAll(normalizedName).map { it.value }.toList()

        // Paso 1: buscar preferencia exacta
        val preferred = languageAliases.entries.firstOrNull { (code, aliases) ->
            preference.lowercase().startsWith(code) &&
                    aliases.any { it.lowercase() in words }
        }?.key

        if (preferred != null) return preferred

        // Fallbacks con coincidencia exacta por palabras
        val fallbackLang = listOf("en", "es", "pt").firstOrNull { langCode ->
            languageAliases[langCode]?.any { it.lowercase() in words } == true
        }

        return fallbackLang
    }


    fun detectSubtitleCode(trackName: String): String? {
        val normalizedName = trackName.lowercase()
        val isAccessible = accessibleCodeSuffixes.any { normalizedName.contains(it) }

        // Normaliza y divide el nombre del track en palabras separadas
        val words = Regex("""\w+""").findAll(normalizedName).map { it.value }.toList()

        // Buscamos un alias exacto en las palabras (sin substring false match)
        val matchedLang = languageAliases.entries.firstOrNull { (_, aliases) ->
            aliases.any { alias ->
                alias.lowercase() in words
            }
        }?.key

        return when {
            matchedLang != null && isAccessible -> "$matchedLang-cc"
            matchedLang != null -> matchedLang
            isAccessible -> "unknown-cc"
            else -> "unknown"
        }
    }

    fun isSubtitleAllowed(name: String): Boolean {
        val normalized = name.lowercase()
        val bannedKeywords = listOf("forced", "force", "shc", "sign", "only", "music", "lyrics", "sdh")

        return bannedKeywords.none { normalized.contains(it) }
    }
}