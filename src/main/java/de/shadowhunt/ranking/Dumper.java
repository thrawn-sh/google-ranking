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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.CheckForNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Dumper {

    private static final String BASE = "https://www.google.de";

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final File base;

    private final int maxPages;

    private final String query;

    public Dumper(final File base, final String query, final int maxPages) {
        this.base = base;
        this.query = query;
        this.maxPages = maxPages;
    }

    @CheckForNull
    private URI calculateUri(final Document document) throws URISyntaxException {
        if (document == null) {
            return createInitialUri();
        }
        return getNextUri(document);
    }

    public void collectData() throws URISyntaxException, IOException {
        final File baseFolder = Collector.calculateDatabaseFolder(base, query);
        if (!baseFolder.mkdirs()) {
            throw new IOException("can not create " + baseFolder);
        }

        try (CloseableHttpClient client = createClient()) {
            Document document = null;
            for (int i = 1; i <= maxPages; i++) {
                final URI uri = calculateUri(document);
                if (uri == null) {
                    break;
                }

                document = performRequest(client, uri);
                if (document == null) {
                    break;
                }

                final File file = new File(baseFolder, String.format("page-%03d.html", i));
                dumpDocumentToFile(document, file);
            }
        }
    }

    private CloseableHttpClient createClient() {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        return builder.build();
    }

    private URI createInitialUri() throws URISyntaxException {
        final URIBuilder builder = new URIBuilder();
        builder.setScheme("https");
        builder.setHost("www.google.de");
        builder.setPath("/search");
        builder.addParameter("client", "firefox-b-d");
        builder.addParameter("q", query);
        return builder.build();
    }

    private HttpGet createRequest(final URI uri) {
        final HttpGet request = new HttpGet(uri);
        // disguise as firefox browser
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:60.0) Gecko/20100101 Firefox/60.0");
        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.addHeader("Accept-Language", "de;q=0.5");
        request.addHeader("Accept-Encoding", "gzip, deflate");
        request.addHeader("DNT", "1");
        request.addHeader("Connection", "keep-alive");
        request.addHeader("Upgrade-Insecure-Requests", "1");
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        return request;
    }

    private void dumpDocumentToFile(final Document document, final File file) throws IOException {
        try (OutputStream output = Files.newOutputStream(file.toPath())) {
            final String dump = document.toString();
            final byte[] raw = dump.getBytes(StandardCharsets.UTF_8);
            output.write(raw);
        }
    }

    @CheckForNull
    private URI getNextUri(final Document document) {
        final Elements links = document.getElementsByAttributeValue("class", "pn");
        for (int i = 0; i < links.size(); i++) {
            final Element element = links.get(i);
            final String href = element.attr("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            return URI.create(BASE + href);
        }
        return null;
    }

    @CheckForNull
    private Document performRequest(final CloseableHttpClient client, final URI uri) throws IOException {
        final HttpGet request = createRequest(uri);
        try (CloseableHttpResponse response = client.execute(request)) {
            final StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                final HttpEntity entity = response.getEntity();
                try (InputStream content = entity.getContent()) {
                    return Jsoup.parse(content, CHARSET, BASE);
                }
            }
        }
        return null;
    }
}
