package com.vikinghelmet.dnd.dpr.util

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.spells.AlwaysPreparedSpells
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpellRemote
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

object CharacterAPI {
    val logger = LoggerFactory.get(CharacterAPI::class.simpleName ?: "no simpleName")

    const val characterUrlPrefix = "https://character-service.dndbeyond.com/character/v5"

    var client: HttpClient? = null

    fun getHttpClient(): HttpClient {
        if (client == null) {
            client = HttpClient()
        }
        return client!!
    }

    fun closeHttpClient() {
        if (client != null) client!!.close()
    }

    fun getFlatHeaderMap(headers: Headers): String {
        val headerMap = mutableMapOf<String, Any>()
        headers.forEach { key, value -> headerMap[key] = value }
        return headerMap.toString()
    }

    suspend fun getRequest(url: String): String {
        val response = getHttpClient().get(url)
        logger.info { "GET, response headers: ${ getFlatHeaderMap(response.headers) }" }
        return response.bodyAsText() ?: "{}"
    }

    suspend fun postRequest(url: String, payload: String): String {
        val response = getHttpClient().post(url) {
            contentType(ContentType.Text.Plain)
            setBody(payload)
        }

        logger.info { "POST, response headers: ${ getFlatHeaderMap(response.headers) }" }
        response.headers
/*
        val response: HttpResponse = getHttpClient().submitFormWithBinaryData(
            url = url,
            formData = formData {
                append("description", "File upload test")
                append("file", payload.toByteArray(), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"attack.csv\"")
                })
            })
*/
        return response.bodyAsText()
    }

    fun getCharacterId(arg: String): String? {
        return if (arg.contains("/")) arg.substringAfterLast("/")
        else if (arg.contains(":")) arg.split(":")[1]
        else if (arg.toIntOrNull() != null) arg
        else null
    }

    suspend fun getRemoteCharacterByUrl(url: String): Pair<String, Character> {
        val json = getRequest(url)
        return Pair(json, getRemoteCharacterFromJson(json))
    }

    suspend fun getRemoteCharacterFromJson(json: String): Character {
        val character: Character = Json.Default.decodeFromString(json)
        character.alwaysPrepared = getAlwaysPreparedSpellList(character)
        return character
    }

    suspend fun updateAlwaysPrepared(character: Character) {
        character.alwaysPrepared = getAlwaysPreparedSpellList(character)
    }

    // this method is needed to support a 2014 Cleric; not clear yet if it has wider utility
    suspend fun getAlwaysPreparedSpellList(character: Character): List<PreparedSpellRemote> {
        val params = character.getApiRequestParameters()

        if (params.isIncomplete()) return mutableListOf()

        val url = StringBuilder("$characterUrlPrefix/game-data/always-prepared-spells?")
            .append("campaignId=${params.campaignId}")
            .append("&sharingSetting=2")
            .append("&classId=${params.classId}")
            .append("&classLevel=${params.classLevel}")
            .append("&backgroundId=${params.backgroundId}")
            .toString()

        val alwaysPrepared: AlwaysPreparedSpells? = Json.Default.decodeFromString(getRequest(url))
        logger.info { "# alwaysPrepared: $alwaysPrepared" }
        return alwaysPrepared?.data ?: mutableListOf()
    }

    fun getCharacterApiURL(urlOrId: String): String? {
        var remoteId: String? = getCharacterId(urlOrId) ?: return null

        return if (remoteId == urlOrId || urlOrId.contains("dndbeyond")) {
            "$characterUrlPrefix/character/$remoteId"
        }
        else urlOrId
    }
}