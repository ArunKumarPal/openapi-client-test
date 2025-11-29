package com.arun.swagger.client;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwaggerClientClassLoader {

    private SwaggerClientClassLoader() {
    }

    public static Map<String, Class<?>> loadClasses(File outputDir) throws MalformedURLException, ClassNotFoundException {
        Map<String, Class<?>> classes = new HashMap<>();
        URL[] urls = {outputDir.toURI().toURL()};
        try (URLClassLoader classLoader = new URLClassLoader(urls, SwaggerClientClassLoader.class.getClassLoader())) {
            for (File classFile : findJavaFiles(outputDir)) {
                // Convert file paths to platform-independent Paths
                Path classFilePath = Paths.get(classFile.getPath());
                Path outputDirPath = Paths.get(outputDir.getPath());
                // Get the relative path between outputDir and classFile
                Path relativePath = outputDirPath.relativize(classFilePath);
                // Convert the relative path to a class name (replacing separators and removing .class extension)
                String className = relativePath.toString()
                        .replace(File.separator, ".")
                        .replace(".class", "");
                Class<?> loadedClass = classLoader.loadClass(className);
                for (Class<?> innerClass : loadedClass.getDeclaredClasses()) {
                    String innerClassName = innerClass.getName();
                    classLoader.loadClass(innerClassName);
                }
                classes.put(className, loadedClass);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    private static List<File> findJavaFiles(File dir) throws IOException {
        return Files.walk(dir.toPath())
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".class"))
                .map(Path::toFile).toList();
    }
}