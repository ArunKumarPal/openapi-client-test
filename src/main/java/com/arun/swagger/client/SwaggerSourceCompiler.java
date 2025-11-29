package com.arun.swagger.client;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwaggerSourceCompiler {

    private SwaggerSourceCompiler() {
    }

    public static void compileJavaSources(File sourceDir, File outputDir) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Cannot find the system Java compiler. "
                                            + "Check that your class path includes tools.jar");
        }
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends javax.tools.JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(Arrays.asList(findJavaFiles(sourceDir)));
        Iterable<String> compileOptions = Arrays.asList("-d", outputDir.getAbsolutePath());
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                null,
                compileOptions,
                null,
                compilationUnits
        );
        boolean success = task.call();
        assert success;
        fileManager.close();
    }

    private static File[] findJavaFiles(File dir) throws IOException {

        List<File> javaFiles = new ArrayList<>();
        Files.walk(dir.toPath())
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> javaFiles.add(path.toFile()));
        File[] files = new File[javaFiles.size()];
        return javaFiles.toArray(files);
    }
}
