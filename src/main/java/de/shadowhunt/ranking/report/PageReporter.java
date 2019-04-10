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
import java.util.Set;
import java.util.SortedSet;

import de.shadowhunt.ranking.data.Result;

public class PageReporter extends AbstractReporter {

    private final Set<String> hosts;

    public PageReporter(final Set<String> hosts, final SortedSet<Result> data) {
        super(data);
        this.hosts = hosts;
    }

    @Override
    public void generate(final PrintWriter writer) {
        writer.println("Pages");
        writer.println("======");
        writer.println();

        int currentPage = 0;
        for (final Result result : data) {
            final int page = result.getPage();
            if (currentPage != page) {
                writer.printf("  =================================== Page %02d ===================================%n", page);
                currentPage = page;
            }

            final int rank = result.getRank();
            final URI uri = result.getUri();
            final String prefix = getPrefix(uri);
            if (result.isAdvertisement()) {
                writer.printf("%s %03d: ADV %s%n", prefix, rank, uri);
            } else {
                writer.printf("%s %03d:     %s%n", prefix, rank, uri);
            }
        }
        // writer.println(" ====================================== ENDE ======================================");
        writer.println();
    }

    private String getPrefix(final URI uri) {
        final String host = uri.getHost();
        if (hosts.contains(host)) {
            return "*";
        }
        return " ";
    }
}
