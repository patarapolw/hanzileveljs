package hanzileveljs

import io.github.cdimascio.dotenv.dotenv
import org.sql2o.Sql2o

object Config {
    val env = dotenv {
        directory = ".."
        ignoreIfMissing = true
    }

    val db = Sql2o("jdbc:sqlite::resource:data.db", null, null)
}