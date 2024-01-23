package irt.web.bean;

import java.util.Base64;
import java.util.Optional;

public class Encoder{

	public static String encode(String toEncode) {
		return Optional.ofNullable(toEncode).map(s->Base64.getEncoder().encodeToString(s.getBytes())).orElse(null);
    }

	public static String decode(String toDecode) {
		return Optional.ofNullable(toDecode).map(s->new String(Base64.getDecoder().decode(s))).orElse(null);
    }

}
