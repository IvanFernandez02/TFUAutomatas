package com.automatas.semantico.infrastructure.adapter.out.persistence;

import com.automatas.shared.domain.port.out.SchemaRepositoryPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JsonSchemaRepository implements SchemaRepositoryPort {

    private final Map<String, Map<String, Object>> schema;

    public JsonSchemaRepository(ObjectMapper objectMapper) {
        try (InputStream inputStream = new ClassPathResource("schema.json").getInputStream()) {
            this.schema = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo cargar schema.json", ex);
        }
    }

    @Override
    public Map<String, Map<String, Object>> loadSchema() {
        return schema;
    }

    @Override
    public boolean tableExists(String table) {
        return schema.containsKey(table.toLowerCase());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean columnExists(String table, String column) {
        Map<String, Object> tableData = schema.get(table.toLowerCase());
        if (tableData == null) {
            return false;
        }
        Map<String, String> columns = (Map<String, String>) tableData.get("columnas");
        return columns != null && columns.containsKey(column.toLowerCase());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> getColumnType(String table, String column) {
        Map<String, Object> tableData = schema.get(table.toLowerCase());
        if (tableData == null) {
            return Optional.empty();
        }
        Map<String, String> columns = (Map<String, String>) tableData.get("columnas");
        if (columns == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(columns.get(column.toLowerCase()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getColumns(String table) {
        Map<String, Object> tableData = schema.get(table.toLowerCase());
        if (tableData == null) {
            return List.of();
        }
        Map<String, String> columns = (Map<String, String>) tableData.get("columnas");
        return columns == null ? List.of() : List.copyOf(columns.keySet());
    }
}
