package com.vladte.devhack.parser.service.parser;

import com.vladte.devhack.parser.service.VacancyParser;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating VacancyParser instances based on the source.
 * Implements the Factory pattern and Strategy pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyParserFactory {

    private final List<VacancyParser> parsers;
    private final Map<String, VacancyParser> parserMap = new HashMap<>();

    /**
     * Initialize the parser map after dependency injection.
     * This method is called by Spring after all dependencies are injected.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing VacancyParserFactory with {} parsers", parsers.size());

        for (VacancyParser parser : parsers) {
            String source = getSourceFromClassName(parser.getClass().getSimpleName());
            parserMap.put(source.toLowerCase(), parser);
            log.info("Registered parser for source: {}", source);
        }
    }

    /**
     * Get a parser for the specified source.
     *
     * @param source The source to get a parser for
     * @return The parser for the specified source, or empty if no parser is found
     */
    public Optional<VacancyParser> getParser(String source) {
        if (source == null || source.isEmpty()) {
            log.warn("Source is null or empty");
            return Optional.empty();
        }

        VacancyParser parser = parserMap.get(source.toLowerCase());
        if (parser == null) {
            log.warn("No parser found for source: {}", source);
            return Optional.empty();
        }

        return Optional.of(parser);
    }

    /**
     * Extract the source name from the parser class name.
     * For example, "DjinnyParserService" -> "Djinny"
     *
     * @param className The class name to extract the source from
     * @return The extracted source name
     */
    private String getSourceFromClassName(String className) {
        if (className.endsWith("ParserService")) {
            return className.substring(0, className.length() - "ParserService".length());
        }
        return className;
    }
}
