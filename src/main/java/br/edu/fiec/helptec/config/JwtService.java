package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Chave secreta configurada no application.properties
    // Exemplo: api.security.token.secret=404E635266556A586E3272357538782F413F4428472B4B6250655368566D5971
    @Value("${api.security.token.secret:404E635266556A586E3272357538782F413F4428472B4B6250655368566D5971}")
    private String secretKey;

    @Value("${api.security.token.expiration-ms:86400000}") // 24 horas em milissegundos
    private long jwtExpirationMs;

    // Extrai o username (e-mail) do token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrai qualquer informação específica (claim) do token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Gera um token para um usuário sem dados adicionais
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Gera um token com informações customizadas
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey())
                .compact();
    }

    // Valida se o token pertence ao usuário informado e se não está expirado
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String generateTokenWithClaims(UsuarioEntity usuario) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", usuario.getId().toString()); // Adiciona o ID do usuário nas claims

        return generateToken(extraClaims, usuario);
    }

    // Gera o token a partir do OAuth2User retornado pelo Google, combinado com o
    // UsuarioEntity já resolvido/criado no banco (o "subject" do JWT continua sendo
    // o e-mail, igual ao login tradicional, pra manter compatível com o JwtAuthenticationFilter).
    public String generateTokenFromOAuth2User(OAuth2User oAuth2User, UsuarioEntity usuario) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", usuario.getId().toString());
        extraClaims.put("name", oAuth2User.getAttribute("name"));
        extraClaims.put("pictureUrl", oAuth2User.getAttribute("picture"));

        return generateToken(extraClaims, usuario);
    }

}
