package com.angelvazquez.csia.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Genera y verifica hashes de contraseñas usando PBKDF2-HMAC-SHA256.
 *
 * Formato almacenado:
 * pbkdf2-sha256$iteraciones$salBase64$hashBase64
 */
public final class PasswordHasher {

    private static final String PREFIX = "pbkdf2-sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    // Parámetros de los hashes nuevos; verify recupera los del hash almacenado.
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private final SecureRandom secureRandom;

    public PasswordHasher() {
        this(new SecureRandom());
    }

    PasswordHasher(SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom);
    }

    /**
     * Genera un hash con una sal aleatoria nueva y los parámetros de esta clase.
     * La contraseña solo debe ser no nula y no vacía; la longitud mínima de
     * registro se valida en AuthService. Este método no borra el array recibido.
     *
     * @param password contraseña cuyo contenido debe limpiar el llamador
     * @return hash con algoritmo, iteraciones, sal y clave derivada en el formato documentado
     * @throws NullPointerException si la contraseña es nula
     * @throws IllegalArgumentException si la contraseña está vacía
     * @throws IllegalStateException si no se puede realizar la derivación PBKDF2
     */
    public String hash(char[] password) {
        validatePassword(password);

        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);

        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derived);
    }

    /**
     * Verifica usando las iteraciones, la sal y la longitud de clave del hash
     * almacenado, sin regenerarlo ni actualizar sus parámetros.
     * No modifica ni borra el array de contraseña recibido.
     *
     * @param password contraseña que se comprueba; puede ser nula
     * @param encodedHash hash en el formato documentado; puede ser nulo
     * @return {@code true} si coincide; {@code false} para entradas nulas, hash
     *         en blanco, formato o parámetros rechazados, o contraseña distinta
     * @throws IllegalStateException si falla la operación criptográfica; este
     *         fallo no se convierte en un resultado de credenciales incorrectas
     */
    public boolean verify(char[] password, String encodedHash) {
        if (password == null || encodedHash == null || encodedHash.isBlank()) {
            return false;
        }

        try {
            String[] parts = encodedHash.split("\\$", -1);
            if (parts.length != 4 || !PREFIX.equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            if (iterations <= 0) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            // Se conserva la longitud del hash guardado para verificarlo con sus parámetros.
            byte[] actual = derive(password, salt, iterations, expected.length * 8);
            // Compara los bytes derivados mediante la utilidad de comparación de digests.
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private byte[] derive(char[] password, byte[] salt, int iterations) {
        return derive(password, salt, iterations, KEY_BITS);
    }

    private byte[] derive(char[] password, byte[] salt, int iterations, int keyBits) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM)
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se puede generar el hash PBKDF2.", ex);
        } finally {
            // Borra la copia de PBEKeySpec; el array original sigue siendo responsabilidad del llamador.
            spec.clearPassword();
        }
    }

    private void validatePassword(char[] password) {
        Objects.requireNonNull(password, "La contraseña no puede ser null.");
        if (password.length == 0) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
    }
}
