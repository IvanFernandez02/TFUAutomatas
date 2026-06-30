package com.automatas.sqlcompiler.application.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class QueryNormalizer {

    private static final Pattern SPACES = Pattern.compile("\\s+");

    public String normalize(String query) {
        String text = query == null ? "" : query.trim();
        text = SPACES.matcher(text).replaceAll(" ");
        text = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return text.toUpperCase();
    }
}
