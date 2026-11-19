package eipi.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Contrasenas {

    private static final String PREFIJO = "pbkdf2";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Contrasenas() {
    }

    public static String cifrar(String clave) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES, BITS_HASH);
        return PREFIJO + "$" + ITERACIONES + "$" + b64(sal) + "$" + b64(hash);
    }

    public static boolean estaCifrada(String almacenado) {
        return almacenado != null && almacenado.startsWith(PREFIJO + "$");
    }

    public static boolean verificar(String clave, String almacenado) {
        if (clave == null || almacenado == null) {
            return false;
        }
        if (!estaCifrada(almacenado)) {
            return MessageDigest.isEqual(almacenado.getBytes(StandardCharsets.UTF_8),
                    clave.getBytes(StandardCharsets.UTF_8));
        }
        try {
            String[] partes = almacenado.split("\\$");
            if (partes.length != 4) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(clave, sal, iteraciones, esperado.length * 8);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones, int bits) {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, bits);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("No se pudo cifrar la contraseña: " + ex.getMessage(), ex);
        } finally {
            spec.clearPassword();
        }
    }

    private static String b64(byte[] datos) {
        return Base64.getEncoder().encodeToString(datos);
    }
}
