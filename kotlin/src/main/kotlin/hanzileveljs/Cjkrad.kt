package hanzileveljs

class Cjkrad {
    data class CjkradEntry internal constructor(
            val sub: List<String>,
            val `super`: List<String>,
            val variant: List<String>
    )

    private data class Entry (val x: String)

    private fun getComponent(s: String, part: String): List<String> {
        return Config.db.open().createQuery("""
            SELECT s.entry AS x
            FROM token AS c
            INNER JOIN $part AS p ON p.token_id = c.id
            INNER JOIN token AS s ON p.${part}_id = s.id
            WHERE c.entry = :s
            ORDER BY s.frequency ASC
        """.trimIndent())
                .addParameter("s", s)
                .executeAndFetch(Entry::class.java)
                .map { it.x }
    }

    operator fun get(s: String): CjkradEntry {
        return CjkradEntry(
                sub = getComponent(s, "sub"),
                `super` = getComponent(s, "super"),
                variant = getComponent(s, "variant")
        )
    }
}