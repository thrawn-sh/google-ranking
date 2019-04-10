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
package de.shadowhunt.ranking.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class Page {

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final File file;

    private final int pageId;

    private final int rank;

    public Page(final File file, final int pageId, final int rank) {
        this.file = file;
        this.pageId = pageId;
        this.rank = rank;
    }

    private URI extractUri(final Element link, final boolean advertisement) {
        final Element parent = link.parent();
        final Element greatParent = parent.parent();
        if (advertisement) {
            final String url = greatParent.attr("data-preconnect-urls");
            final String[] split = StringUtils.split(url, ",");
            return URI.create(split[0]);
        }
        final String url = greatParent.attr("href");
        return URI.create(url);
    }

    private boolean isAdvertisement(final Element element) {
        final Elements siblings = element.siblingElements();
        for (int i = 0; i < siblings.size(); i++) {
            final Element sibling = siblings.get(i);
            final String text = sibling.text();
            if ("Ad".equals(text) || "Anzeige".equals(text)) {
                return true;
            }
        }

        return false;
    }

    public SortedSet<Result> parse() throws IOException {
        final Document document = Jsoup.parse(file, CHARSET);

        final SortedSet<Result> results = new TreeSet<>();
        final Elements links = document.getElementsByTag("cite");
        for (int i = 0; i < links.size(); i++) {
            final Element link = links.get(i);
            final boolean advertisement = isAdvertisement(link);
            final URI uri = extractUri(link, advertisement);
            final Result result = new Result(pageId, rank + i, uri, advertisement);
            results.add(result);
        }
        return results;
    }
}
