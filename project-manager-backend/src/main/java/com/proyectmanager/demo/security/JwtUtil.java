package com.proyectmanager.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Utilidad para todo lo relacionado con JWT:
 * - Generar tokens
 * - Validar tokens
 * - Extraer información (claims) de los tokens
 *
 * Un JWT tiene 3 partes separadas por puntos:
 * header.payload.signature
 * El payload contiene los "claims" — datos que metemos adentro del token.
 */
@Component
public class JwtUtil {

    /**
     * Clave secreta para firmar el token.
     * Se lee del application.properties — nunca se escribe directo en el código.
     * Debe tener mínimo 32 caracteres para el algoritmo HS256.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Tiempo de expiración en milisegundos.
     * 86400000 ms = 24 horas.
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Convierte el string secreto en una clave criptográfica
     * que JJWT puede usar para firmar y verificar tokens.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera el TOKEN TEMPORAL después del login.
     * Solo contiene el userId — aún no tiene workspace ni rol.
     * Su único propósito es identificar al usuario para el segundo paso.
     */
    public String generateTempToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())          // quién es el usuario
                .claim("type", "TEMP")               // marcamos que es temporal
                .issuedAt(new Date())                // cuándo se generó
                .expiration(new Date(System.currentTimeMillis() + 300000)) // expira en 5 min
                .signWith(getSigningKey())            // firmamos con nuestra clave
                .compact();                          // construimos el string final
    }

    /**
     * Genera el TOKEN FINAL después de seleccionar workspace.
     * Este es el token completo — contiene userId, workspaceId y rol.
     * Es el que se usa en todos los requests posteriores.
     */
    public String generateToken(UUID userId, UUID workspaceId, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("workspaceId", workspaceId.toString())
                .claim("role", role)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae todos los claims (datos) del token.
     * Si el token está manipulado o expirado, JJWT lanza una excepción automáticamente.
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // verificamos la firma
                .build()
                .parseSignedClaims(token)      // parseamos el token
                .getPayload();                 // obtenemos el payload
    }

    // ── Métodos de conveniencia para extraer claims específicos ──

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractWorkspaceId(String token) {
        return extractClaims(token).get("workspaceId", String.class);
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String extractType(String token) {
        return extractClaims(token).get("type", String.class);
    }

    /**
     * Verifica si el token es válido:
     * - Que la firma sea correcta
     * - Que no haya expirado
     * - Que sea del tipo esperado (TEMP o ACCESS)
     */
    public boolean isTokenValid(String token, String expectedType) {
        try {
            Claims claims = extractClaims(token);
            boolean notExpired = claims.getExpiration().after(new Date());
            boolean correctType = expectedType.equals(claims.get("type", String.class));
            return notExpired && correctType;
        } catch (Exception e) {
            // Cualquier problema con el token retorna false
            return false;
        }
    }
}