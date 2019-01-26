package hanzileveljs


class Tatoeba {
    private val qBase = """
        SELECT
            s1."text" AS chinese, s2."text" AS english
        FROM
            sentence AS s1
            INNER JOIN "link"          ON s1.id = "link".sentence_id
            INNER JOIN sentence AS s2  ON "link".translation_id = s2.id
        WHERE s1.lang = :lang1 AND s2.lang = :lang2
    """.trimIndent()

    data class TatoebaEntry internal constructor(
            val chinese: String,
            val english: String
    )

    operator fun get(s: String): List<TatoebaEntry> {
        return Config.db.open().createQuery("""
            $qBase AND s1."text" LIKE :t
        """.trimIndent())
                .addParameter("t", "%$s%")
                .addParameter("lang1", "cmn")
                .addParameter("lang2", "eng")
                .executeAndFetch(TatoebaEntry::class.java)
    }

    fun random(): TatoebaEntry {
        return Config.db.open().createQuery("""
            $qBase
            ORDER BY RANDOM() LIMIT 1
        """.trimIndent())
                .addParameter("lang1", "cmn")
                .addParameter("lang2", "eng")
                .executeAndFetchFirst(TatoebaEntry::class.java)
    }
}