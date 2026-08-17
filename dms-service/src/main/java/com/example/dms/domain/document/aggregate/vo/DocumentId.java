package com.example.dms.domain.document.aggregate.vo;

import java.util.Objects;
import java.util.UUID;

public class DocumentId {
    private final String value;

    public DocumentId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DocumentId generate() {
        return new DocumentId(UUID.randomUUID().toString().replace("-", ""));
    }

    public static DocumentId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DocumentId cannot be null or empty");
        }
        return new DocumentId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentId that = (DocumentId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "DocumentId{" +
                "value='" + value + '\'' +
                '}';
    }
}
