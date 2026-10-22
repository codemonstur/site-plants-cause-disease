package app;

import app.model.Disease;
import com.google.gson.Gson;
import nu.validator.servlet.ListErrorHandler;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.*;

import static app.core.Pages.newDiseaseFile;
import static app.core.Pages.newIndexFile;
import static app.model.Disease.toSourceName;
import static app.util.Functions.isNullOrEmpty;
import static app.util.Loader.listDiseases;
import static app.util.Loader.loadDiseases;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public enum Validate {;

    public static void main(final String... args) throws Exception {
        final var gson = new Gson();
        final var diseases = loadDiseases(gson, listDiseases());

        validateFields(diseases);
        validateDefinition(diseases);
        validateAnecdotes(diseases);
        validateSee(diseases);
        validateContent(diseases);

        validateHtml(diseases);
    }

    private static void validateFields(final Disease[] diseases) {
        final var missingPapers = new LinkedList<String>();
        final var missingArticles = new LinkedList<String>();
        final var missingVideos = new LinkedList<String>();
        final var seeAndPapers = new LinkedList<String>();
        final var seeAndArticles = new LinkedList<String>();
        final var seeAndVideos = new LinkedList<String>();
        final var seeAndSearch = new LinkedList<String>();
        final var missingSource = new LinkedList<String>();
        final var missingSearch = new LinkedList<>();

        for (final var disease : diseases) {
            if (disease.papers() == null && disease.see() == null)
                missingPapers.add(disease.uri());
            if (disease.articles() == null && disease.see() == null)
                missingArticles.add(disease.uri());
            if (disease.videos() == null && disease.see() == null)
                missingVideos.add(disease.uri());
            if (disease.papers() != null && disease.see() != null)
                seeAndPapers.add(disease.uri());
            if (disease.articles() != null && disease.see() != null)
                seeAndArticles.add(disease.uri());
            if (disease.videos() != null && disease.see() != null)
                seeAndVideos.add(disease.uri());
            if (disease.see() != null && disease.carnivoreDietSearchQueries() != null)
                seeAndSearch.add(disease.uri());
            if (disease.definition() != null && disease.definition().source() == null)
                missingSource.add(disease.uri());
            if (disease.see() == null && (disease.carnivoreDietSearchQueries() == null || disease.carnivoreDietSearchQueries().isEmpty()))
                missingSearch.add(disease.uri());
        }

        if (!missingPapers.isEmpty())
            System.err.println("Papers field missing in " + missingPapers);
        if (!missingArticles.isEmpty())
            System.err.println("Articles field missing in " + missingArticles);
        if (!missingVideos.isEmpty())
            System.err.println("Videos field missing in " + missingVideos);
        if (!seeAndPapers.isEmpty())
            System.err.println("Both 'see' and 'papers' field in " + seeAndPapers);
        if (!seeAndArticles.isEmpty())
            System.err.println("Both 'see' and 'articles' field in " + seeAndArticles);
        if (!seeAndVideos.isEmpty())
            System.err.println("Both 'see' and 'videos' field in " + seeAndVideos);
        if (!seeAndSearch.isEmpty())
            System.err.println("Both 'see' and 'carnivoreDietSearchQueries' field in " + seeAndSearch);
        if (!missingSource.isEmpty())
            System.err.println("Missing source on definition: " + missingSource);
        if (!missingSearch.isEmpty())
            System.err.println("Missing search queries for: " + missingSearch);
    }

    private static void validateAnecdotes(final Disease[] diseases) {
        final var missingAnecdotes = new LinkedList<String>();
        final var queriesInSee = new LinkedList<String>();
        final var noQueries = new LinkedList<String>();
        final var nonVisibleAnecdotes = new HashMap<Disease, Integer>();

        for (final var disease : diseases) {
            if (disease.see() == null && (disease.anecdotes() == null || disease.anecdotes().isEmpty()))
                missingAnecdotes.add(disease.uri());

            if (disease.filteredAnecdotes() != null && !disease.filteredAnecdotes().isEmpty()) {
                final var set = new HashSet<String>();
                for (final var uri : disease.filteredAnecdotes()) {
                    if (set.contains(uri)) {
                        System.err.println("Duplicate filtered anecdote " + uri + " for " + disease.uri());
                    }
                    set.add(uri);
                }
            }
            if (disease.anecdotes() != null && !disease.anecdotes().isEmpty()) {
                final var set = new HashSet<String>();
                for (final var anecdote : disease.anecdotes()) {
                    if (!anecdote.visible())
                        nonVisibleAnecdotes.merge(disease, 1, Integer::sum);

                    if (set.contains(anecdote.uri())) {
                        System.err.println("Duplicate anecdote " + anecdote.uri() + " for " + disease.uri());
                    }
                    set.add(anecdote.uri());
                }
                if (disease.see() != null) {
                    System.err.println("Disease " + disease.uri() + " has both a see and anecdotes");
                }
            }
            if (disease.see() != null) {
                if (disease.carnivoreDietSearchQueries() != null && !disease.carnivoreDietSearchQueries().isEmpty())
                    queriesInSee.add(disease.uri());
                continue;
            }
            if (disease.carnivoreDietSearchQueries() == null || disease.carnivoreDietSearchQueries().isEmpty()) {
                noQueries.add(disease.uri());
            }
        }
        if (!queriesInSee.isEmpty())
            System.err.println("Queries and See section found for " + queriesInSee);
        if (!noQueries.isEmpty())
            System.err.println("Missing search queries for " + noQueries);
        if (!nonVisibleAnecdotes.isEmpty()) {
            final var notEnoughAnecdotes = new LinkedList<String>();
            for (final var anecdote : nonVisibleAnecdotes.entrySet()) {
                final var disease = anecdote.getKey();
                final var total = disease.anecdotes().size();
                final var unchecked = anecdote.getValue();
                if (total - unchecked < 50) notEnoughAnecdotes.add(disease.uri());
            }
            if (!notEnoughAnecdotes.isEmpty())
                System.err.println("Anecdotes need to be checked for " + notEnoughAnecdotes);
        }
        if (!missingAnecdotes.isEmpty())
            System.err.println("Missing anecdotes for " + missingAnecdotes);
    }

    private static void validateDefinition(final Disease[] diseases) {
        final var set = new HashMap<String, String>();
        for (final var disease : diseases) {
            if (disease.definition() == null) continue;
            if (isNullOrEmpty(disease.definition().source())) {
                continue;
            }
            set.compute(disease.definition().source(), (key, value) -> {
                if (value != null)
                    System.err.println("Disease " + disease.uri() + " and " + value + " have the same definition source");
                return disease.uri();
            });

            try {
                final var hostname = toSourceName(disease.definition().source());
                if (hostname == null)
                    System.err.println("No known source name for URI " + disease.definition().source());
            } catch (final URISyntaxException e) {
                System.err.println("Source URI for definition of " + disease.uri() + " is not valid");
            }
        }
    }

    private static void validateContent(final Disease[] diseases) {
        final var nonVisible = new LinkedList<String>();
        final var noSeeOrDef = new LinkedList<String>();
        final var noContent = new LinkedList<String>();

        for (final var disease : diseases) {
            if (!disease.visible())
                nonVisible.add(disease.uri());
            if (disease.see() == null && disease.definition() == null)
                noSeeOrDef.add(disease.uri());
            if (disease.see() == null && disease.text() == null)
                noContent.add(disease.uri());
        }

        if (!noSeeOrDef.isEmpty())
            System.err.println("Missing see or definition for " + noSeeOrDef);
        if (!noContent.isEmpty())
            System.err.println("No content for " + noContent);
        if (!nonVisible.isEmpty())
            System.err.println("Non-visible unfinished diseases " + nonVisible);
    }


    private static void checkStatusIsOk(final HttpClient client, final String diseaseName, final URI uri) {
        final var request = HttpRequest.newBuilder()
            .uri(uri).GET().build();
        try {
            final var response = client.send(request, discarding());
            if (response.statusCode() != 200)
                System.err.println("URI " + uri + " from " + diseaseName + " returns " + response.statusCode());
        } catch (final InterruptedException | IOException e) {
            System.err.println("Failed to connect to " + uri);
        }
    }

    public static void validateSee(final Disease[] diseases) {
        final var map = new HashMap<String, Disease>();
        for (final var disease : diseases) {
            if (disease.see() != null) {
                if (disease.see().uri() == null)
                    System.err.println(disease.uri() + " has see but no see URI");
                if (disease.see().name() == null)
                    System.err.println(disease.uri() + " has see but no see name");
            }

            map.put(disease.uri(), disease);
        }

        for (final var disease : diseases) {
            if (disease.see() != null && disease.see().uri() != null) {
                final var see = map.get(disease.see().uri());
                if (see == null) {
                    System.err.println("Missing see Uri reference to " + disease.see().uri() + " in " + disease.uri());
                    continue;
                }
                if (!see.name().equals(disease.see().name()))
                    System.err.println("see name of " + disease.uri() + " doesn't match linked name " + see.name());
                if (see.see() != null)
                    System.err.println("Disease " + disease.uri() + " has see " + disease.see().uri() + " which has another see " + see.see().uri());
                if (disease.visible() && !see.visible())
                    System.err.println("Visible disease " + disease.uri() + " has see " + disease.see().uri() + " which is not visible");
                if (!disease.visible() && see.visible())
                    System.err.println("Invisible disease " + disease.uri() + " points to visible disease " + see.uri());
            }
        }
    }

    public static void validateHtml(final Disease[] diseases) throws Exception {

        final var validator = new SimpleDocumentValidator();
        validator.setUpMainSchema("http://s.validator.nu/html5-rdfalite.rnc", new SystemErrErrorHandler());

        printErrors("index", validateHtml(validator, newIndexFile(diseases)));
        for (final var disease : diseases) {
            printErrors("disease " + disease.uri(), validateHtml(validator, newDiseaseFile(disease)));
        }
    }

    private static List<String> validateHtml(final SimpleDocumentValidator validator, final String html) throws IOException, SAXException {
        final var errorHandler = new ListErrorHandler();

        try (final var in = new ByteArrayInputStream(html.getBytes(UTF_8))) {
            validator.setUpValidatorAndParsers(errorHandler, true, false);
            validator.checkHtmlInputSource(new InputSource(in));
        }

        return errorHandler.getErrors();
    }

    private static void printErrors(final String fileName, final List<String> errors) {
        for (final var error : errors) {
            System.out.println(fileName + ": "  + error);
        }
    }

}
