package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class StringUtils {
    public static final String TERMS_DELIMITER = ",";
    public static final String MULTI_ARGUMENTS_DELIMITER = "\\|";
    private static String regex = "\\(?\\b((ht|f)tp(s?)://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
    private final static MessageDigest HASH_METHOD;
    private static Integer contentLength = 200;

    static {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            md5 = null;
        }

        HASH_METHOD = md5;
    }
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }


    public static boolean isNumeric(CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        } else {
            int sz = cs.length();

            for(int i = 0; i < sz; ++i) {
                if (!Character.isDigit(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static String removeURLs(String text) {
        return text.replaceAll(regex, "");
    }

    public static synchronized List<List<String>> gatherProps(String terms) {
        List<List<String>> res = new ArrayList<>();
        if (terms == null || terms.isEmpty()) return res;

        for (String bigTerms : terms.split(MULTI_ARGUMENTS_DELIMITER)) {
            if (Strings.isNullOrEmpty(bigTerms)) continue;
            res.add(Arrays.asList(bigTerms.split(TERMS_DELIMITER)));
        }
        return res;
    }

    public static boolean startsWithCapital(String word) {
        return word.length() > 0 && Character.isUpperCase(word.charAt(0));
    }

    public static synchronized String cleanws(String text) {
        return text.replaceAll("\\s+", " ");
    }

    public static String toJson(Object map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getStringHash(String string) {
        byte[] mdbytes = HASH_METHOD.digest(string.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte mdbyte : mdbytes) {
            hexString.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
        }

        return hexString.toString();
    }


    public static String splitContentBySize(String content) {
        if (content.length() <= contentLength) return content;

        try {
            if (content.substring(contentLength).contains("."))
                return content.substring(0, content.indexOf(".", contentLength)).trim()
                        .replace("\n", "").replace("\t", "") + "...";
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        return content;
    }

}
