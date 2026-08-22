package com.example.dms.application.shared.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDocumentQuery {
    private int page;
    private int size;
    private String title;
    private String status;
}
