import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import java.security.Key
import java.util.*

class Auth(private val dbHandler: DBHandler) {
    private val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private val refreshKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    private val json = Json { ignoreUnknownKeys = true }

    fun checkAuth(authorization: String): User {
        val token_user = (json.decodeFromString(User.serializer(), authorization))
        return dbHandler.idc(token_user)
    }
}
