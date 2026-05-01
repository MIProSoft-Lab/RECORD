package es.uib.record.backend.security.open

import es.uib.record.backend.auth.domain.Token
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value($$"${application.security.jwt.secret-key}")
    private val jwtSecretKey: String,
    @Value($$"${application.security.jwt.expiration}")
    private val jwtExpiration: Long,
    @Value($$"${application.security.jwt.refresh-token.expiration}")
    private val jwtRefreshExpiration: Long
) {
    private val signInKey: SecretKey
        get() = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretKey))

    fun generateToken(email: String): String {
        return this.buildToken(email, jwtExpiration)
    }

    fun generateRefreshToken(email: String): String {
        return this.buildToken(email, jwtRefreshExpiration)
    }

    private fun buildToken(email: String, expiration: Long): String {
        return Jwts.builder()
            .subject(email)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(signInKey)
            .compact()
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val email = this.extractEmail(token)

        return (email == userDetails.username) && !this.isTokenExpired(token)
    }
    fun isTokenValid(token: String): Boolean {
        return !this.isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return this.extractExpiration(token).before(Date())
    }

    fun extractEmail(token: String): String? {
        val jwtToken = Jwts.parser()
            .verifyWith(signInKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return jwtToken.subject
    }

    private fun extractExpiration(token: String): Date {
        val jwtToken = Jwts.parser()
            .verifyWith(signInKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return jwtToken.expiration
    }
}