import XRegExp from "xregexp";

import db from "./data";

const baseQuery = `
SELECT * FROM vocab
INNER JOIN token ON token.entry = vocab.simplified
ORDER BY frequency DESC`;
const chineseRegex = XRegExp("\\p{Han}");

interface ICedict {
    id: number;
    simplified: string;
    traditional: string;
    pinyin: string;
    english: string;
    frequency: number;
}

interface ICedictMap {
    [key: string]: ICedict;
}

function _searchOne(s: string, col: string): Promise<ICedictMap> {
    return new Promise((resolve, reject) => {
        db.all(`
        ${baseQuery}
        WHERE ${col} LIKE ?`, [`%${s}%`], (err, rows) => { err ? reject(err) : resolve(rows.reduce((o, el) => {
            const {entry, ...cedict} = el;
            o[cedict.frequency.toString()] = cedict as ICedict;
            return o;
        }, {})); });
    });
}

export function searchChinese(s: string): Promise<ICedict[]> {
    return new Promise((resolve, reject) => {
        db.all(`
        ${baseQuery}
        WHERE simplified LIKE ? OR traditional LIKE ?`,
        [`%${s}%`, `%${s}%`], (err, rows) => { err ? reject(err) : resolve(rows.map((el) => {
            const {entry, ...cedict} = el;
            return cedict as ICedict;
        })); });
    });
}

export function searchChineseMatch(s: string): Promise<ICedict[]> {
    return new Promise((resolve, reject) => {
        db.all(`
        ${baseQuery}
        WHERE simplified = ? OR traditional = ?`,
        [s, s], (err, rows) => { err ? reject(err) : resolve(rows.map((el) => {
            const {entry, ...cedict} = el;
            return cedict as ICedict;
        })); });
    });
}

export async function searchEnglish(s: string): Promise<ICedict[]> {
    const map = await _searchOne(s, "english");
    return Object.keys(map).map((f) => parseFloat(f)).sort().map((f) => map[f]);
}

export async function searchPinyin(s: string): Promise<ICedict[]> {
    const map = await _searchOne(s, "pinyin");
    return Object.keys(map).map((f) => parseFloat(f)).sort().map((f) => map[f]);
}

export async function _searchNonChinese(s: string): Promise<ICedict[]> {
    const map = {
        ...await _searchOne(s, "english"),
        ...await _searchOne(s, "pinyin")
    };
    return Object.keys(map).map((f) => parseFloat(f)).sort().map((f) => map[f]);
}

export default function searchDict(s: string): Promise<ICedict[]> {
    return chineseRegex.test(s) ? searchChinese(s) : _searchNonChinese(s);
}
