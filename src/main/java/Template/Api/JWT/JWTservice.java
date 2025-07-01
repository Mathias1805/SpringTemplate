package Template.Api.JWT;


import Template.Api.DTO.UserFrontendDTO;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.UserFrontend.Usuariofrontend;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private UsuarioFrontendRepository usuarioFrontendRepository;
    private static final String SECRET_KEY = "PdqFzPtrNwmMx1zI5FaZhZXyOpl3MlLTkfCWucP95Wk";
    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user);
    }
    public String getToken(UserDetails user,UserFrontendDTO userFrontendDTO) {
        try{
        Map<String, Object> claims = new HashMap<>();
        Optional<Usuariofrontend>usuariofrontend = usuarioFrontendRepository
                .findByUsuario(userFrontendDTO.getUsuario());
        if(usuariofrontend.isEmpty()){
            throw new Exception("Error Inesperado");
        }
        claims.put("Id",usuariofrontend.get().getId());
        claims.put("UserFrontend",usuariofrontend.get().getUsuario());
        claims.put("Dni",usuariofrontend.get().getDni());
        claims.put("Email",usuariofrontend.get().getEmail());
        claims.put("Telefono",usuariofrontend.get().getTelefono());
        claims.put("Nombre",usuariofrontend.get().getNombre());
        claims.put("Paterno",usuariofrontend.get().getPaterno());
        claims.put("Materno",usuariofrontend.get().getMaterno());
        return getToken(claims, user);
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
    public String getToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
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

    public <T> T getClaim(String token, Function<Claims, T> ClaimsResolver) {
        final Claims claims = getAllClaims(token);
        return ClaimsResolver.apply(claims);
    }

    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public String Cod(String password) {
        try {
            // Concatenar la contraseña con la clave estática
            String passwordWithSalt = password + SECRET_KEY;

            // Crear un objeto MessageDigest con el algoritmo SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Actualizar el objeto MessageDigest con los bytes de la cadena concatenada
            md.update(passwordWithSalt.getBytes());

            // Obtener la representación de bytes del hash
            byte[] byteData = md.digest();

            // Convertir los bytes a una representación hexadecimal

            return Hex.encodeHexString(byteData);
        } catch (NoSuchAlgorithmException e) {
            // Manejar la excepción en caso de que no se encuentre el algoritmo
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

}
