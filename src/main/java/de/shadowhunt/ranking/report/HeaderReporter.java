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
package de.shadowhunt.ranking.report;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.shadowhunt.ranking.data.Result;

public class HeaderReporter extends AbstractReporter {

    private final URI google;

    private final SortedSet<String> hosts = new TreeSet<>();

    private final String query;

    private final Date queryDate;

    private final int requestedMaxPages;

    public HeaderReporter(final URI google, final String query, final int requestedMaxPages, final Date queryDate, final Set<String> hosts, final SortedSet<Result> data) {
        super(data);
        this.google = google;
        this.hosts.addAll(hosts);
        this.query = query;
        this.queryDate = new Date(queryDate.getTime());
        this.requestedMaxPages = requestedMaxPages;
    }

    @Override
    public void generate(final PrintWriter writer) {
        writer.println("Overview");
        writer.println("========");
        writer.printf("          URL: %s%n", google);
        writer.printf("        Query: %s%n", query);
        writer.printf("   Query Date: %s%n", queryDate);
        writer.printf("Analysis Date: %s%n", new Date());
        writer.printf("      Results: %d%n", data.size());
        final Result last = data.last();
        writer.printf("        Pages: %d / %d%n", last.getPage(), requestedMaxPages);
        if (!hosts.isEmpty()) {
            writer.println(" Host markers:");
            for (final String host : hosts) {
                writer.printf("    - %s%n", host);
            }
        }
        writer.println();
    }

}
