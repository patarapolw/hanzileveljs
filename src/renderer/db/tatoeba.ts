import pinyin from "chinese-to-pinyin";

import db from "./data";

interface ISentenceDict {
    chinese: string;
    pinyin: string;
    english: string;
}

export default function searchSentence(s: string): Promise<ISentenceDict[]> {
    return new Promise((resolve, reject) => {
        db.all(`
        SELECT
            s1."text" AS src, s2."text" AS dst
        FROM
            sentences AS s1
            INNER JOIN "links"          ON s1.id = "links".sentence_id
            INNER JOIN sentences AS s2  ON "links".translation_id = s2.id
        WHERE
            s1."text" LIKE ? AND s1.lang = ? AND s2.lang = ?`,
            [`%${s}%`, "cmn", "eng"], (err, rows) => { err ? reject(err) : resolve(rows.map((r) => {
                return {
                    chinese: r.src,
                    pinyin: pinyin(r.src),
                    english: r.dst
                } as ISentenceDict;
            })); }
        );
    });
}
