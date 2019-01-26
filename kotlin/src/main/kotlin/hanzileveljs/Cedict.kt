package hanzileveljs

class Cedict {
    data class CedictEntry internal constructor(
            val id: Int,
            val simplified: String,
            val traditional: String?,
            val pinyin: String,
            val english: String,
            val frequency: Float
    )

    private val baseQuery = """
        SELECT vocab.id AS id, simplified, traditional, pinyin, english, frequency FROM vocab
        INNER JOIN token ON token.entry = vocab.simplified
    """.trimIndent()

    private fun searchOne(s: String, col: String): Map<Float, CedictEntry> {
        val o = mutableMapOf<Float, CedictEntry>()

        Config.db.open().createQuery("""
            $baseQuery
            WHERE $col LIKE :s
            ORDER BY frequency DESC
        """.trimIndent())
                .addParameter("s", "%$s%")
                .executeAndFetch(CedictEntry::class.java)
                .forEach {
                    o[it.frequency] = it
                }

        return o.toMap()
    }

    private fun searchNonChinese(s: String): List<CedictEntry> {
        val output = searchOne(s, "pinyin").toMutableMap()
        output.putAll(searchOne(s, "english"))

        return output.values.sortedBy { it.frequency }
    }

    fun searchChinese(s: String): List<CedictEntry> {
        return Config.db.open().createQuery("""
            $baseQuery
            WHERE simplified LIKE :s OR traditional LIKE :s
            ORDER BY frequency DESC
        """.trimIndent())
                .addParameter("s", "%$s%")
                .executeAndFetch(CedictEntry::class.java)
    }

    fun searchChineseMatch(s: String): List<CedictEntry> {
        return Config.db.open().createQuery("""
            $baseQuery
            WHERE simplified = :s OR traditional = :s
            ORDER BY frequency DESC
        """.trimIndent())
                .addParameter("s", s)
                .executeAndFetch(CedictEntry::class.java)
    }

    fun searchPinyin(s: String): List<CedictEntry> {
        return Config.db.open().createQuery("""
            $baseQuery
            WHERE pinyin LIKE :s
            ORDER BY frequency DESC
        """.trimIndent())
                .addParameter("s", "%$s%")
                .executeAndFetch(CedictEntry::class.java)
    }

    fun searchEnglish(s: String): List<CedictEntry> {
        return Config.db.open().createQuery("""
            $baseQuery
            WHERE english LIKE :s
            ORDER BY frequency DESC
        """.trimIndent())
                .addParameter("s", "%$s%")
                .executeAndFetch(CedictEntry::class.java)
    }

    operator fun get(s: String): List<CedictEntry> = when(Regex("\\p{IsHan}").find(s)) {
        null -> searchNonChinese(s)
        else -> searchChinese(s)
    }
}