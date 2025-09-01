package com.vladte.devhack.parser.entities;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class QueryParameters {
    private String topic;
    private int maxClicks;
    private Instant from;
    private String url;
    private Map<String, String> filters;
}
