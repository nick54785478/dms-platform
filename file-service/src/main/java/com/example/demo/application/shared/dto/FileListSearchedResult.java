package com.example.demo.application.shared.dto;

import java.util.List;

public record FileListSearchedResult(
    List<String> fileKeys,
    String lastKey,
    boolean hasMore
) {}
