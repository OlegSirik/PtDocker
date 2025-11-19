package ru.pt.auth.service.token

import org.springframework.stereotype.Component
import java.util.Base64

@Component
class TokenParser {

    fun parseBasicToken(token: String): Pair<String, String>? {
        return try {
            val base64Credentials = token.removePrefix("Basic ").trim()
            val credentials = String(Base64.getDecoder().decode(base64Credentials))
            val parts = credentials.split(":", limit = 2)

            if (parts.size == 2) {
                Pair(parts[0], parts[1])
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}