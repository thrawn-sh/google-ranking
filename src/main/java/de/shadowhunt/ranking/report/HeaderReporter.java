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
import java.util.Date;
import java.util.SortedSet;

import de.shadowhunt.ranking.data.Result;

public class HeaderReporter extends AbstractReporter {

    private final Date date;

    private final String query;

    public HeaderReporter(final String query, final Date date, final SortedSet<Result> data) {
        super(data);
        this.date = new Date(date.getTime());
        this.query = query;
    }

    @Override
    public void generate(final PrintWriter writer) {
        writer.println("Übersicht");
        writer.println("=========");
        writer.printf("  Query: %s%n", query);
        writer.printf("   Date: %s%n", date);
        writer.printf("Results: %d%n", data.size());
        final Result last = data.last();
        writer.printf("  Pages: %d%n", last.getPage());
        writer.println();
    }

}
