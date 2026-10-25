package app.util;

import app.Generate;
import com.google.gson.JsonParseException;
import com.networknt.schema.*;
import io.pebbletemplates.pebble.PebbleEngine;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import static com.networknt.schema.InputFormat.JSON;
import static com.networknt.schema.SpecVersion.VersionFlag.V7;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.isDirectory;

public enum IO {;

    private static final PebbleEngine pebble = new PebbleEngine.Builder().build();
    public static String compilePebble(final String resource, final Map<String, Object> model) throws IOException {
        final var name = resource.startsWith("/") ? resource.substring(1) : resource;
        final var template = pebble.getTemplate(name);
        try (final var writer = new StringWriter()) {
            template.evaluate(writer, model);
            return writer.toString();
        }
    }

    public static Path deleteDirectory(final Path pathToBeDeleted) throws IOException {
        if (!isDirectory(pathToBeDeleted)) return pathToBeDeleted;
        try (final var paths = Files.walk(pathToBeDeleted)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        return pathToBeDeleted;
    }

    public static String loadResource(final String resource, final Charset charset) throws IOException {
        return new String(loadResource(resource), charset);
    }

    public static byte[] loadResource(final String resource) throws IOException {
        try (final var in = IO.class.getResourceAsStream(resource)) {
            if (in == null) throw new FileNotFoundException("Resource " + resource + " does not exist.");
            return in.readAllBytes();
        }
    }

    public static void copyResource(final String resourceName, final Path parent, final String fileName) throws IOException {
        copyResource(resourceName, parent.resolve(fileName));
    }

    public static void copyResource(final String resourceName, final Path destination) throws IOException {
        try (final var in = Generate.class.getResourceAsStream(resourceName)) {
            if (in == null) throw new FileNotFoundException(resourceName + " missing in resources");

            Files.copy(in, destination);
        }
    }

    public static void writeFile(final String content, final Path parent, final String fileName) throws IOException {
        writeFile(content, parent.resolve(fileName));
    }
    public static void writeFile(final String content, final Path destination) throws IOException {
        Files.writeString(destination, content);
    }

    public static Set<ValidationMessage> validateJson(final JsonSchema schema, final String json) {
        try {
            return schema.validate(json, JSON, executionContext -> {
                executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
            });
        } catch (IllegalArgumentException | JsonParseException e) {
            System.err.println("JSON parse error for " + json);
            throw e;
        }
    }

    public static JsonSchema loadSchema(final String resourcePath) throws IOException {
        return JsonSchemaFactory.getInstance(V7).getSchema(loadResource(resourcePath, UTF_8));
    }

}
