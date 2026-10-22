package app;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static app.core.Pages.newDiseaseFile;
import static app.core.Pages.newIndexFile;
import static app.util.IO.*;
import static app.util.Loader.listDiseases;
import static app.util.Loader.loadDiseases;
import static java.nio.file.Files.createDirectories;

public enum Generate {;

    public static void main(final String... args) {
        try {
            run(args);
        } catch (final Exception e) {
            System.err.println("Generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void run(final String... args) throws IOException, URISyntaxException {
        final var gson = new Gson();
        final var diseases = loadDiseases(gson, listDiseases());

        final var target = createDirectories(deleteDirectory(Paths.get("docs")));
        final var diseaseDir = createDirectories(target.resolve("disease"));

        copyResource("/favicon.ico", target, "favicon.ico");
        writeFile(newIndexFile(diseases), target, "index.html");

        for (final var disease : diseases) {
            try {
                writeFile(newDiseaseFile(disease), diseaseDir, disease.uri() + ".html");
            } catch (final Exception e) {
                System.err.println("Failed to generate page for " + disease.uri() + ": " + e.getMessage());
            }
        }
    }

}
