package com.vikinghelmet.dnd.dpr.util

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.spells.AlwaysPreparedSpells
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

    suspend fun getRequest(url: String): String {
        val response = getHttpClient().get(url)
        return response.bodyAsText() ?: "{}"
    }

    fun getCharacterId(arg: String): String? {
        return if (arg.contains("/")) arg.substringAfterLast("/")
        else if (arg.contains(":")) arg.split(":")[1]
        else if (arg.toIntOrNull() != null) arg
        else null
    }

    suspend fun getRemoteCharacterByUrl(url: String): Pair<String, Character> {
        val json = getRequest(url)
        val character: Character = Json.Default.decodeFromString(json)
        val alwaysPrepared = getAlwaysPreparedSpellList(character)
        logger.info { "# alwaysPrepared: $alwaysPrepared" }
        character.alwaysPrepared = alwaysPrepared?.data ?: mutableListOf()
        return Pair(json, character)
    }

    suspend fun getAlwaysPreparedSpellList(character: Character): AlwaysPreparedSpells? {
        val params = character.getApiRequestParameters()

        if (params.isIncomplete()) return null

        val url = StringBuilder("$characterUrlPrefix/game-data/always-prepared-spells?")
            .append("campaignId=${params.campaignId}")
            .append("&sharingSetting=2")
            .append("&classId=${params.classId}")
            .append("&classLevel=${params.classLevel}")
            .append("&backgroundId=${params.backgroundId}")
            .toString()

        return Json.Default.decodeFromString(getRequest(url))
    }

    fun getCharacterApiURL(id: String): String {
        return "$characterUrlPrefix/character/$id"
    }

}