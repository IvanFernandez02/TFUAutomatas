package com.automatas.sqlcompiler.domain.port.out;

public interface OllamaPort {

    boolean isAvailable();

    String getAvailabilityMessage();

    String generate(String prompt);
}
