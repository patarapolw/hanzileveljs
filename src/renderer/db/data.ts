import sqlite3 from "sqlite3";
import path from "path";

export default new sqlite3.Database(path.join(__dirname, "database/data.db"), sqlite3.OPEN_READONLY);
