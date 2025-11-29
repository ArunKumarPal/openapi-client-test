package com.arun.swagger.client;


import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SwaggerClientGenerator {

    private SwaggerClientGenerator() {
    }

    public static void generateClient(String openApiSpecPath, String outputDir) {
        CodegenConfigurator configurator = new CodegenConfigurator();
        configurator.setInputSpec(fetchOpenApiJson(openApiSpecPath));
        configurator.setLang("java");
        configurator.setOutputDir(outputDir);
        DefaultGenerator generator = new DefaultGenerator();
        generator.opts(configurator.toClientOptInput());
        generator.generate();
    }

    private static String fetchOpenApiJson(String url) {
        try (InputStream is = new URL(url).openStream();
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            Assertions.fail("Failed to fetch OpenAPI JSON from " + url + ": " + e.getMessage());
            return null; // Unreachable, but required
        }
    }
}
