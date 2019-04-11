/**
 * Google ranking analysis - Generates a report for google rankings
 * Copyright © 2019 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.ranking;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;
import org.slf4j.impl.SimpleLogger;

import de.shadowhunt.ranking.data.Result;
import de.shadowhunt.ranking.report.HeaderReporter;
import de.shadowhunt.ranking.report.PageReporter;
import de.shadowhunt.ranking.report.StatisticReporter;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

public class Main {

    private static OptionSpec<File> createBaseOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("base", "b");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "target folder for dumps");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("base");
        return optionSpec.ofType(File.class).defaultsTo(new File("."));
    }

    private static OptionSpec<String> createDomainsOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("doamin", "d");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "list of domains");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("domains");
        return optionSpec.ofType(String.class).withValuesSeparatedBy(",");
    }

    private static OptionSpec<URI> createGoogleOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("google", "g");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "google instance to query");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("instance");
        final URI uri = URI.create("https://www.google.com");
        return optionSpec.ofType(URI.class).defaultsTo(uri);
    }

    private static OptionSpec<Void> createHelpOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("help", "h", "?");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "show this command help");
        return builder.forHelp();
    }

    private static OptionSpec<Integer> createPagesOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("pages", "p");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "maximum number of pages");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("pages");
        return optionSpec.ofType(Integer.class).defaultsTo(10);
    }

    private static OptionParser createParser() {
        final OptionParser parser = new OptionParser(false);
        final BuiltinHelpFormatter formatter = new BuiltinHelpFormatter(160, 2);
        parser.formatHelpWith(formatter);
        parser.posixlyCorrect(true);
        return parser;
    }

    private static OptionSpec<String> createQueryOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("query", "q");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "google query");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("query");
        return optionSpec.ofType(String.class).required();
    }

    private static OptionSpec<File> createWireLogOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("wirelog", "w");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "dump all communication to");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("file");
        return optionSpec.ofType(File.class);
    }

    public static void main(final String[] args) throws Exception {
        final OptionParser parser = createParser();
        final OptionSpec<File> baseOption = createBaseOption(parser);
        final OptionSpec<String> domainsOption = createDomainsOption(parser);
        final OptionSpec<URI> googleOption = createGoogleOption(parser);
        final OptionSpec<Void> helpOption = createHelpOption(parser);
        final OptionSpec<Integer> pagesOption = createPagesOption(parser);
        final OptionSpec<String> queryOption = createQueryOption(parser);
        final OptionSpec<File> wireLogOption = createWireLogOption(parser);

        final OptionSet options;
        try {
            options = parser.parse(args);
        } catch (final OptionException e) {
            parser.printHelpOn(System.err);
            return;
        }

        if (options.has(helpOption)) {
            parser.printHelpOn(System.out);
            return;
        }

        final File log = options.valueOf(wireLogOption);
        if (log != null) {
            System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, "[yyyy-MM-dd HH:mm:ss.SSS]");
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "error");
            System.setProperty(SimpleLogger.LEVEL_IN_BRACKETS_KEY, "true");
            final String absolutePath = log.getAbsolutePath();
            System.setProperty(SimpleLogger.LOG_FILE_KEY, absolutePath);
            System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "org.apache.http.wire", "debug");
            System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true");
            System.setProperty(SimpleLogger.SHOW_LOG_NAME_KEY, "false");
            System.setProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        }

        final File base = baseOption.value(options);
        final List<String> domains = domainsOption.values(options);
        final URI google = googleOption.value(options);
        final int pages = pagesOption.value(options);
        final String query = queryOption.value(options);

        final Main main = new Main(google, query, pages, base, new HashSet<>(domains));
        main.run();
    }

    private final File base;

    private final URI google;

    private final Set<String> hosts;

    private final int maxPages;

    private final String query;

    public Main(final URI google, final String query, final int maxPages, final File base, final Set<String> hosts) {
        this.google = google;
        this.base = base;
        this.query = query;
        this.maxPages = maxPages;
        this.hosts = hosts;
    }

    private Date getCreationDate(final File folder) throws IOException {
        if (folder.exists()) {
            final Path path = folder.toPath();
            final BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
            final FileTime creationTime = fileAttributes.creationTime();
            final long millis = creationTime.toMillis();
            return new Date(millis);
        }
        return new Date(0L);
    }

    private boolean isDatabaseCurrent(final File folder) throws IOException {
        final Date creationDate = getCreationDate(folder);
        final Date now = new Date();
        final long age = (now.getTime() - creationDate.getTime());
        return age < (12 * 60 * 60 * 1000); // 12h
    }

    private void run() throws Exception {
        final File folder = Collector.calculateDatabaseFolder(base, query);
        if (!isDatabaseCurrent(folder)) {
            // clean old database
            FileUtils.deleteDirectory(folder);
            final Dumper dumper = new Dumper(google, query, maxPages, base);
            dumper.collectData();
        }

        final Collector collector = new Collector(base, query);
        final SortedSet<Result> data = collector.parseData();

        final File report = new File(folder, "report.txt");
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(report.toPath()), "UTF-8"))) {
            final Date date = getCreationDate(folder);
            new HeaderReporter(google, query, maxPages, date, hosts, data).generate(writer);
            new PageReporter(hosts, data).generate(writer);
            new StatisticReporter(hosts, data).generate(writer);
        }
    }

}
