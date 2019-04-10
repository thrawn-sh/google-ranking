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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;

import de.shadowhunt.ranking.data.Result;
import de.shadowhunt.ranking.report.HeaderReporter;
import de.shadowhunt.ranking.report.PageReporter;
import de.shadowhunt.ranking.report.StatisticReporter;

public class Main {

    public static void main(final String[] args) throws Exception {
        final Set<String> hosts = new HashSet<>();
        hosts.add("example.net");
        final Main main = new Main("test test", 20, hosts);
        main.run();
    }

    private final File base = new File(".");

    private final Set<String> hosts;

    private final int maxPages;

    private final String query;

    public Main(final String query, final int maxPages, final Set<String> hosts) {
        this.query = query;
        this.maxPages = maxPages;
        this.hosts = hosts;
    }

    private Date getCreationDate(final File folder) {
        if (folder.exists()) {
            return new Date(folder.lastModified());
        }
        return new Date(0L);
    }

    private boolean isDatabaseCurrent(final File folder) {
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
            final Dumper dumper = new Dumper(base, query, maxPages);
            dumper.collectData();
        }

        final Collector collector = new Collector(base, query);
        final SortedSet<Result> data = collector.parseData();

        final File report = new File(folder, "report.txt");
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(report.toPath()), "UTF-8"))) {
            final Date date = getCreationDate(folder);
            new HeaderReporter(query, date, data).generate(writer);
            new PageReporter(hosts, data).generate(writer);
            new StatisticReporter(hosts, data).generate(writer);
        }
    }

}
