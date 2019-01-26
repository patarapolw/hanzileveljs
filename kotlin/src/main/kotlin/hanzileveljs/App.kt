package hanzileveljs

import com.google.gson.Gson
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark
import spark.Spark.path
import spark.kotlin.port
import spark.kotlin.post

class App {
    private val frontendPort = Config.env["TS_PORT"] ?: "3000"
    private val corsHeaders = mapOf(
            "Access-Control-Allow-Origin" to "http://localhost:$frontendPort"
    )
    private val gson = Gson()

    private fun applyRest() {
        Spark.after(Filter { _: Request, response: Response ->
            corsHeaders.forEach { k, v -> response.header(k, v) }
            response.type("application/json")
        })
    }

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
        applyRest()

        path("/api") {
            post("/cedict") {
                val entryRequest = gson.fromJson(this.request.body(), EntryRequest::class.java)
                entryRequest.entries?.let { entries ->
                    gson.toJson(mapOf(
                            "entries" to entries.map { cedict[it] }
                    ))
                } ?: {
                    gson.toJson(mapOf(
                            "entries" to cedict[entryRequest.entry!!]
                    ))
                }
            }

            post("/cjkrad") {
                val entryRequest = gson.fromJson(this.request.body(), EntryRequest::class.java)
                gson.toJson(cjkrad[entryRequest.entry!!])
            }

            post("/tatoeba") {
                val entryRequest = gson.fromJson(this.request.body(), EntryRequest::class.java)
                gson.toJson(mapOf(
                        "entries" to tatoeba[entryRequest.entry!!]
                ))
            }
        }
    }
}

fun main(args: Array<String>) {
    App().serve((Config.env["PORT"] ?: "5000").toInt())
}
