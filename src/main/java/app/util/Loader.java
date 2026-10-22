package app.util;

import app.model.Disease;
import com.google.gson.Gson;
import com.networknt.schema.ValidationMessage;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;

import java.io.IOException;
import java.util.stream.Collectors;

import static app.util.IO.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public enum Loader {;

    private static final int DISEASE_PATH_OFFSET = "disease/".length();

    public static String[] listDiseases() {
        try (final var scanResult = new ClassGraph().acceptPathsNonRecursive("disease").scan()) {
            return scanResult.getResourcesWithExtension("json").stream()
                .map(Resource::getPath)
                .map(path -> path.substring(DISEASE_PATH_OFFSET, path.length() - 5))
                .toArray(String[]::new);
        }
    }

    public static Disease[] loadDiseases(final Gson gson, final String... resources) throws IOException {
        final var schemaDisease = loadSchema("/schema/disease.json");

        final var diseases = new Disease[resources.length];
        for (int i = 0; i < resources.length; i++) {
            final var json = loadResource("/disease/" + resources[i] + ".json", UTF_8);
            final var validationMessages = validateJson(schemaDisease, json);
            if (!validationMessages.isEmpty()) {
                final var errors = validationMessages.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining(", "));
                System.err.println("Disease json " + resources[i] + " contains schema validation errors:\n" + errors);
            }
            diseases[i] = gson.fromJson(json, Disease.class);
        }
        return diseases;
    }

}
