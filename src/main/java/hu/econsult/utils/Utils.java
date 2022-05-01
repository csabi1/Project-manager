package hu.econsult.utils;

import java.util.Base64;

import org.springframework.util.Base64Utils;

public class Utils {

	public static String stringToBase64(String str) {
		return Base64.getEncoder().encodeToString(str.getBytes());
	}

	public static String base64ToString(String base64) {
		byte[] decodedBytes = Base64.getDecoder().decode(base64);
		return new String(decodedBytes);
	}
	
	public static String removeLastChars(String str, int chars) {
		return str.substring(0, str.length() - chars);
	}

	public static String byteArrayToBase64(byte[] byteArray) {
		return Base64Utils.encodeToString(byteArray);
	}

	public static byte[] base64ToByteArray(String str) {
		return Base64Utils.decodeFromString(str);
	}

	public static String firstNCharacters(String str, Integer n) {
		return str.substring(0, n);

	}
}