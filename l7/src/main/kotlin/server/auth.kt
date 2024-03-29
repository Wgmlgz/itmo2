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

    private fun checkTokenUser(jws: String, key: Key): User {
        try {
            val jwt = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws)
            val user = jwt.body["user"] as String
            return (Json.decodeFromString(UserArg.serializer(), user)).user
        } catch (e: ExpiredJwtException) {
            throw TestException(ResponseCode.LOGIN_TIMEOUT, e)
        } catch (e: SignatureException) {
            throw TestException(ResponseCode.UNAUTHORIZED, e)
        }
    }


    private fun genToken(arg: UserArg, key: Key, field: Int, amount: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(field, amount)
//        var arg = Json.decodeFromString(UserArg.serializer(), Json.encodeToString(arg))
        arg.user.refreshToken = null
        return Jwts.builder().setClaims(mapOf("user" to Json.encodeToString(arg)))
            .setExpiration(calendar.time)
            .signWith(key)
            .compact()
    }

    private fun genAccess(user: User) = genToken(UserArg(user), key, Calendar.MINUTE, 5)
    private fun genRefresh(user: User) = genToken(UserArg(user), refreshKey, Calendar.MINUTE, 30)

    fun checkAuth(authorization: String) = checkTokenUser(authorization, key)

    fun login(user: User): Pair<String, String> {
        val users = dbHandler.checkLogged(user)
        if (users.size != 1)
            throw Exception("invalid credentials")
        val trueUser = users[0]
        val token = genAccess(trueUser)
        val refresh = genRefresh(trueUser)
        dbHandler.setRefresh(trueUser, refresh)
        return Pair(token, refresh)
    }

    fun refresh(refreshToken: String): Pair<String, String> {
        val user = checkTokenUser(refreshToken, refreshKey)
        try {
            if (dbHandler.getRefresh(user) != refreshToken) throw Exception("Refresh token was expired. Please make a new sign in request")
        } catch (e: Exception) {
            throw TestException(ResponseCode.LOGIN_TIMEOUT, e)
        }
        val token = genAccess(user)
        val refresh = genRefresh(user)
        dbHandler.setRefresh(user, refresh)
        return Pair(token, refresh)
    }
}
