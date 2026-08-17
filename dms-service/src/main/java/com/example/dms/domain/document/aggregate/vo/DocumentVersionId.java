package com.example.dms.domain.document.aggregate.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * 文件版本唯一識別碼 (Value Object)
 */
public class DocumentVersionId {

    private final String value;

    public DocumentVersionId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DocumentVersionId cannot be null or empty");
        }
        this.value = value;
    }

    public static DocumentVersionId generate() {
        return new DocumentVersionId(UUID.randomUUID().toString().replace("-", ""));
    }

    public static DocumentVersionId of(String value) {
        return new DocumentVersionId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentVersionId that = (DocumentVersionId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "DocumentVersionId{" +
                "value='" + value + '\'' +
                '}';
    }
}
