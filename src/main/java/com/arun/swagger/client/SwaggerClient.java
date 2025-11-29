package com.arun.swagger.client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


public class SwaggerClient {

    private SwaggerClient() {
    }

    public static Map<String, Class<?>> createSwaggerClient(String swaggerFilePath, String generationOutputDir, String compiledOutputDir) throws Exception {
        // Step 1: Generate the client
        File compiledDir = new File(compiledOutputDir);
        File outputDir = new File(generationOutputDir);
        if (outputDir.exists()) {
            deleteDirectory(outputDir);
        }
        outputDir.mkdirs();
        SwaggerClientGenerator.generateClient(swaggerFilePath, generationOutputDir);
        // Step 2: Compile the generate client
        File sourceDir = new File(generationOutputDir + "/src");
        if (compiledDir.exists()) {
            deleteDirectory(compiledDir);
        }
        compiledDir.mkdirs();
        SwaggerSourceCompiler.compileJavaSources(sourceDir, compiledDir);

        // Step 3: load the compiled client classes
        return SwaggerClientClassLoader.loadClasses(compiledDir);
    }

    // Utility method to delete directories recursively
    private static void deleteDirectory(File directory) throws Exception {
        if (directory.exists()) {
            Files.walk(directory.toPath())
                    .sorted((a, b) -> b.compareTo(a)) // Delete children before parents
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}

