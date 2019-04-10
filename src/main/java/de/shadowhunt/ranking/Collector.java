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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.RegExUtils;

import de.shadowhunt.ranking.data.Page;
import de.shadowhunt.ranking.data.Result;

public class Collector {

    private static final FilenameFilter HTML_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
            return name.endsWith("html");
        }
    };

    private static final Comparator<File> NAME_COMPARATOR = new Comparator<File>() {

        @Override
        public int compare(final File file1, final File file2) {
            final String name1 = file1.getName();
            final String name2 = file2.getName();
            return name1.compareTo(name2);
        }

    };

    static File calculateDatabaseFolder(final File base, final String query) {
        final String lowerCase = query.toLowerCase(Locale.GERMAN);
        final String queryDB = RegExUtils.replaceAll(lowerCase, "\\s+", "_");
        final File folder = new File(base, queryDB);
        return folder;
    }

    private final File folder;

    public Collector(final File folder) {
        this.folder = folder;
    }

    public Collector(final File base, final String query) {
        this(calculateDatabaseFolder(base, query));
    }

    private SortedSet<Result> createReport() throws IOException {
        final SortedSet<Result> results = new TreeSet<>();

        final File[] files = folder.listFiles(HTML_FILTER);
        if (files == null) {
            return Collections.emptySortedSet();
        }
        Arrays.sort(files, NAME_COMPARATOR);

        int pageCounter = 1;
        int rank = 1;
        for (final File file : files) {
            final Page page = new Page(file, pageCounter, rank);
            final SortedSet<Result> pageResult = page.parse();
            results.addAll(pageResult);
            pageCounter++;
            rank += pageResult.size();
        }
        return results;
    }

    public SortedSet<Result> parseData() throws IOException {
        if (folder.isDirectory()) {
            return createReport();
        }
        return Collections.emptySortedSet();
    }
}
