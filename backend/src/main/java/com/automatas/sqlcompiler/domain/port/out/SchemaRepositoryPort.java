package com.automatas.sqlcompiler.domain.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SchemaRepositoryPort {

    Map<String, Map<String, Object>> loadSchema();

    boolean tableExists(String table);

    boolean columnExists(String table, String column);

    Optional<String> getColumnType(String table, String column);

    List<String> getColumns(String table);
}
