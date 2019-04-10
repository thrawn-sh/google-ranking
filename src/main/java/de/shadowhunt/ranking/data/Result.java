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

import java.net.URI;

public class Result implements Comparable<Result> {

    private final boolean advertisement;

    private final int page;

    private final int rank;

    private final URI uri;

    public Result(final int page, final int rank, final URI uri, final boolean advertisement) {
        this.page = page;
        this.rank = rank;
        this.uri = uri;
        this.advertisement = advertisement;
    }

    @Override
    public int compareTo(final Result o) {
        return rank - o.rank;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Result other = (Result) obj;
        if (advertisement != other.advertisement) {
            return false;
        }
        if (page != other.page) {
            return false;
        }
        if (rank != other.rank) {
            return false;
        }
        if (uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!uri.equals(other.uri)) {
            return false;
        }
        return true;
    }

    public int getPage() {
        return page;
    }

    public int getRank() {
        return rank;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (advertisement ? 1231 : 1237);
        result = (prime * result) + page;
        result = (prime * result) + rank;
        result = (prime * result) + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    public boolean isAdvertisement() {
        return advertisement;
    }

    @Override
    public String toString() {
        return "Result [page=" + page + ", rank=" + rank + ", uri=" + uri + ", advertisement=" + advertisement + "]";
    }

}
