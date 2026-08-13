package com.angelvazquez.csia.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void verificaLaContrasenaCorrecta() {
        String hash = hasher.hash("Password123".toCharArray());
        assertTrue(hasher.verify("Password123".toCharArray(), hash));
    }

    @Test
    void rechazaUnaContrasenaIncorrecta() {
        String hash = hasher.hash("Password123".toCharArray());
        assertFalse(hasher.verify("OtraPassword".toCharArray(), hash));
    }

    @Test
    void laSalAleatoriaProduceHashesDiferentes() {
        String hash1 = hasher.hash("Password123".toCharArray());
        String hash2 = hasher.hash("Password123".toCharArray());
        assertNotEquals(hash1, hash2);
        assertTrue(hasher.verify("Password123".toCharArray(), hash1));
        assertTrue(hasher.verify("Password123".toCharArray(), hash2));
    }
}
