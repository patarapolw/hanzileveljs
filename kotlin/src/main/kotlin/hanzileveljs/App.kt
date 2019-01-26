package hanzileveljs

import com.google.gson.Gson
import spark.Spark.path
import spark.kotlin.port
import spark.kotlin.post
import kotlin.random.Random

class App {
    private val gson = Gson()

    private data class EntryRequest (
            val entry: String? = null,
            val entries: List<String>? = null,
            val type: String? = null
    )

    private val cedict = Cedict()
    private val cjkrad = Cjkrad()
    private val tatoeba = Tatoeba()

    fun serve(_port: Int) {
        port(_port)

        path("/api") {
            path("/cedict") {
                post("/") {
                    val entryRequest = gson.fromJson(this.request.body(), EntryRequest::class.java)
                    entryRequest.entries?.let { entries ->
                        gson.toJson(entries.flatMap { cedict[it].filterIndexed { i, _ -> i < 10 } }.distinctBy { it.id })
                    } ?: {
                        gson.toJson(cedict[entryRequest.entry!!].filterIndexed { i, _ -> i < 10 })
                    }
                }

                post("/match") {
                    val entryRequest = gson.fromJson(this.request.body(), EntryRequest::class.java)
                    entryRequest.entries?.let { entries ->
                        gson.toJson(entries.flatMap { cedict.searchChineseMatch(it) })
                    } ?: {
                        gson.toJson(cedict.searchChineseMatch(entryRequest.entry!!))
                    }
                }

                post("/random") {
                    val entryRequest = gson.fromJson(this.request.body(), EntryRequest::class.java)

                    if (entryRequest.entries.isNullOrEmpty()) {
                        gson.toJson(cedict.random())
                    } else {
                        var result = cedict[entryRequest.entries[0]]
                        entryRequest.entries.subList(1, entryRequest.entries.size).forEach { vocab ->
                            result = result.filter { it.simplified.contains(vocab) || it.traditional?.contains(vocab) ?: false }
                        }

                        if (result.isNotEmpty())gson.toJson( result[Random.nextInt(result.size)]) else "{}"
                    }
                }
            }

            path("/cjkrad") {
                post("/") {
                    val entryRequest = gson.fromJson(this.request.body(), EntryRequest::class.java)
                    gson.toJson(cjkrad[entryRequest.entry!!])
                }
            }

            path("/tatoeba") {
                post("/") {
                    val entryRequest = gson.fromJson(this.request.body(), EntryRequest::class.java)
                    gson.toJson(tatoeba[entryRequest.entry!!].filterIndexed { i, _ -> i < 10 })
                }

                post("/random") {
                    val entryRequest = gson.fromJson(this.request.body(), EntryRequest::class.java)

                    if (entryRequest.entries.isNullOrEmpty()) {
                        gson.toJson(tatoeba.random())
                    } else {
                        var result = tatoeba[entryRequest.entries[0]]
                        entryRequest.entries.subList(1, entryRequest.entries.size).forEach { c ->
                            result = result.filter { it.chinese.contains(c) }
                        };

                        if (result.isNotEmpty())gson.toJson( result[Random.nextInt(result.size)]) else "{}"
                    }
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    App().serve((Config.env["PORT"] ?: System.getenv("PORT") ?: "5000").toInt())
}
