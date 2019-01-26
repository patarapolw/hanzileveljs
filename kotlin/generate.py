import sqlite3
import requests
import regex


class CreateDatabase:
    srcRoot = "/Users/patarapolw/GitHubProjects/zhdiary/zhdiary_kotlin/src/main/resources/"
    srcDb = {
        "cedict": sqlite3.connect(srcRoot + "cedict.db"),
        "cjkrad": sqlite3.connect(srcRoot + "cjkrad4j.db"),
        "tatoeba": sqlite3.connect(srcRoot + "tatoeba.db")
    }
    dstDb = sqlite3.connect("/Users/patarapolw/GitHubProjects/hanzileveljs/kotlin/src/main/resources/data.db")

    def __init__(self):
        for v in self.srcDb.values():
            v.row_factory = sqlite3.Row

        self.dstDb.execute("""
        CREATE TABLE IF NOT EXISTS vocab (
            id          INTEGER PRIMARY KEY AUTOINCREMENT,
            simplified  VARCHAR NOT NULL,
            traditional VARCHAR,
            pinyin      VARCHAR,
            english     VARCHAR,
            UNIQUE (simplified, traditional, pinyin)
        );
        """)

        self.dstDb.execute("""
        CREATE TABLE IF NOT EXISTS token (
            id          INTEGER PRIMARY KEY AUTOINCREMENT,
            entry       VARCHAR NOT NULL UNIQUE,
            frequency   FLOAT
        );
        """)

        self.dstDb.execute("""
        CREATE TABLE IF NOT EXISTS sub (
            token_id    INT REFERENCES token(id),
            sub_id      INT REFERENCES token(id),
            PRIMARY KEY (token_id, sub_id)
        );
        """)

        self.dstDb.execute("""
        CREATE TABLE IF NOT EXISTS super (
            token_id    INT REFERENCES token(id),
            super_id      INT REFERENCES token(id),
            PRIMARY KEY (token_id, super_id)
        );
        """)

        self.dstDb.execute("""
        CREATE TABLE IF NOT EXISTS variant (
            token_id    INT REFERENCES token(id),
            variant_id      INT REFERENCES token(id),
            PRIMARY KEY (token_id, variant_id)
        );
        """)

        self.dstDb.execute("""
        CREATE TABLE IF NOT EXISTS sentence (
            id      INTEGER PRIMARY KEY,
            lang    TEXT NOT NULL,
            "text"  TEXT NOT NULL UNIQUE
        );
        """)

        self.dstDb.execute("""
        CREATE TABLE IF NOT EXISTS "link" (
            sentence_id     INTEGER NOT NULL,
            translation_id  INTEGER NOT NULL,
            PRIMARY KEY (sentence_id, translation_id),
            FOREIGN KEY (sentence_id) REFERENCES sentences(id),
            FOREIGN KEY (translation_id) REFERENCES sentences(id)
        );
        """)
    
    def cedict(self):
        c = self.srcDb["cedict"].execute("SELECT * FROM cedict")
        for r in c:
            self.dstDb.execute("""
            INSERT INTO vocab (simplified, traditional, pinyin, english)
            VALUES (?, ?, ?, ?)
            """, (r["simplified"], r["traditional"], r["pinyin"], r["english"]))
        
        self.dstDb.commit()
    
    def tatoeba(self):
        tatoeba = self.srcDb["tatoeba"]

        c = tatoeba.execute("SELECT * FROM sentences")
        for r in c:
            try:
                self.dstDb.execute("""
                INSERT INTO sentence (lang, "text")
                VALUES (?, ?)
                """, (r["lang"], r["text"]))
            except sqlite3.IntegrityError:
                pass
        
        self.dstDb.commit()

        c = tatoeba.execute("SELECT * FROM 'links'")
        for r in c:
            self.dstDb.execute("""
            INSERT INTO "link" (sentence_id, translation_id)
            VALUES (?, ?)
            """, (r["sentence_id"], r["translation_id"]))
        
        self.dstDb.commit()
    
    def cjkrad(self):
        cjkrad = self.srcDb["cjkrad"]

        c = cjkrad.execute("SELECT * FROM character")
        for r in c:
            self.dstDb.execute("""
            INSERT INTO token (entry)
            VALUES (?)
            """, (r["character"],))
        
        self.dstDb.commit()

        c = cjkrad.execute("SELECT * FROM character_sub")
        for r in c:
            self.dstDb.execute("""
            INSERT INTO sub (token_id, sub_id)
            VALUES (?, ?)
            """, (r["character_id"], r["subRadical_id"]))
        
        c = cjkrad.execute("SELECT * FROM character_super")
        for r in c:
            self.dstDb.execute("""
            INSERT INTO super (token_id, super_id)
            VALUES (?, ?)
            """, (r["character_id"], r["superRadical_id"]))
        
        c = cjkrad.execute("SELECT * FROM character_variant")
        for r in c:
            self.dstDb.execute("""
            INSERT INTO variant (token_id, variant_id)
            VALUES (?, ?)
            """, (r["character_id"], r["variant_id"]))
        
        self.dstDb.commit()
    
    def add_frequency(self, url="http://corpus.leeds.ac.uk/frqc/lcmc.num"):
        r = requests.get(url)

        for row in r.text.split("\n"):
            if regex.search(r"\p{IsHan}", row):
                content = row.split(" ")
                cursor = self.dstDb.execute("""
                UPDATE token SET frequency = ? WHERE entry = ?
                """, (float(content[1]), content[2]))

                if cursor.rowcount == 0:
                    self.dstDb.execute("""
                    INSERT INTO token (frequency, entry)
                    VALUES (?, ?)
                    """, (float(content[1]), content[2]))
        
        self.dstDb.commit()


if __name__ == "__main__":
    CreateDatabase().add_frequency()
