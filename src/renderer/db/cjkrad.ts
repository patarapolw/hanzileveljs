import db from "./data";

interface ICjkrad {
    sub: Promise<string[]>;
    super: Promise<string[]>;
    variant: Promise<string[]>;
}

function _getComponent(c: string, part: string): Promise<string[]> {
    return new Promise((resolve, reject) => {
        db.all(`
        SELECT s.character AS x
        FROM character AS c
        INNER JOIN ${part} AS cs ON cs.character_id = c.id
        INNER JOIN character AS s ON cs.${part}_id = s.id
        WHERE c.character = ?
        ORDER BY s.frequency ASC`, [c], (err, rows) => { err ? reject(err) : resolve(rows.map((el) => el.x)); });
    });
}

export default function getRadical(c: string): ICjkrad {
    return {
        sub: _getComponent(c, "sub"),
        super: _getComponent(c, "super"),
        variant: _getComponent(c, "variant")
    };
}
