package com.example.demo.presentation.resource.in;

import java.util.Map;

public record CompleteMultipartUploadResource(
    Map<Integer, String> partETags
) {}
