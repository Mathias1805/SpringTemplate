package Template.Api.JWT;

import Template.Api.DTO.DTO_UserFrontend;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.UserFrontend.Usuariofrontend;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class JWTservice {

    private static final Logger log = LoggerFactory.getLogger(JWTservice.class);

    @Autowired
    private UsuarioFrontendRepository usuarioFrontendRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${app.password.salt}")
    private String passwordSalt;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user);
    }

    public String getToken(UserDetails user, DTO_UserFrontend userFrontendDTO) {
        try {
            Map<String, Object> claims = new HashMap<>();
            Optional<Usuariofrontend> usuariofrontend = usuarioFrontendRepository
                    .findByUsuario(userFrontendDTO.getUsuario());
            if (usuariofrontend.isEmpty()) {
                throw new RuntimeException("Usuario frontend no encontrado");
            }
            claims.put("Id", usuariofrontend.get().getId());
            claims.put("UserFrontend", usuariofrontend.get().getUsuario());
            claims.put("Dni", usuariofrontend.get().getDni());
            claims.put("Email", usuariofrontend.get().getEmail());
            claims.put("Telefono", usuariofrontend.get().getTelefono());
            claims.put("Nombre", usuariofrontend.get().getNombre());
            claims.put("Paterno", usuariofrontend.get().getPaterno());
            claims.put("Materno", usuariofrontend.get().getMaterno());
            return getToken(claims, user);
        } catch (Exception e) {
            log.error("Error generando token para usuario frontend: {}", e.getMessage());
            throw new RuntimeException("Error generando token: " + e.getMessage());
        }
    }

    public String getToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String refreshToken(String token, UserDetails userDetails) {
        Claims claims = getAllClaims(token);
        Map<String, Object> carryoverClaims = new HashMap<>(claims);
        // Remover claims reservados de JWT — serán re-establecidos por getToken()
        carryoverClaims.remove("sub");
        carryoverClaims.remove("iat");
        carryoverClaims.remove("exp");
        return getToken(carryoverClaims, userDetails);
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsuarioFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String usuario = getUsuarioFromToken(token);
        return (usuario.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public String Cod(String password) {
        try {
            String passwordWithSalt = password + passwordSalt;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(passwordWithSalt.getBytes());
            byte[] byteData = md.digest();
            return Hex.encodeHexString(byteData);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error en hash de contraseña: {}", e.getMessage());
            throw new RuntimeException("Error al procesar contraseña");
        }
    }
}
