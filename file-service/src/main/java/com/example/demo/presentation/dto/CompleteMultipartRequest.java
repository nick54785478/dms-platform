package com.example.demo.presentation.dto;

import java.util.Map;

public record CompleteMultipartRequest(
    Map<Integer, String> partETags
) {}
