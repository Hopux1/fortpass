package com.sociallab.appgestion;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Clase de utilidad para la encriptación y desencriptación de datos.
 * Utiliza el algoritmo AES (Advanced Encryption Standard) para garantizar la seguridad.
 */
public class EncryptionUtil {

    // Constante que define el tipo de cifrado utilizado
    private static final String AES = "AES";

    /**
     * Genera una clave de encriptación basada en el userId.
     *
     * @param userId El identificador único del usuario.
     * @return Una clave de 16 bytes en formato Base64.
     * @throws Exception Si ocurre algún error al generar el hash.
     */
    public static String generateKey(String userId) throws Exception {
        // Crear un hash SHA-256 del userId
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(userId.getBytes(StandardCharsets.UTF_8));

        // Tomar solo los primeros 16 bytes del hash y codificarlos en Base64
        return Base64.encodeToString(Arrays.copyOf(key, 16), Base64.NO_WRAP);
    }

    /**
     * Encripta un texto utilizando una clave especificada.
     *
     * @param data El texto a encriptar.
     * @param key La clave de encriptación (debe ser de 16 bytes).
     * @return El texto encriptado en formato Base64.
     * @throws Exception Si ocurre algún error durante el proceso de encriptación.
     */
    public static String encrypt(String data, String key) throws Exception {
        // Crear la especificación de clave secreta
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES);

        // Configurar el cifrador en modo ENCRYPT_MODE
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        // Encriptar los datos y codificarlos en Base64
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
    }

    /**
     * Desencripta un texto previamente encriptado utilizando una clave especificada.
     *
     * @param encryptedData El texto encriptado en formato Base64.
     * @param key La clave de desencriptación (debe ser de 16 bytes).
     * @return El texto desencriptado.
     * @throws Exception Si ocurre algún error durante el proceso de desencriptación.
     */
    public static String decrypt(String encryptedData, String key) throws Exception {
        // Crear la especificación de clave secreta
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES);

        // Configurar el cifrador en modo DECRYPT_MODE
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        // Decodificar los datos en Base64 y desencriptarlos
        byte[] decodedData = Base64.decode(encryptedData, Base64.NO_WRAP);
        return new String(cipher.doFinal(decodedData), StandardCharsets.UTF_8);
    }
}
