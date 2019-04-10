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
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import de.shadowhunt.ranking.data.Result;

public class StatisticReporter extends AbstractReporter {

    private static class ResultCluster implements Comparable<ResultCluster> {

        private final String name;

        private final SortedSet<Result> results = new TreeSet<>();

        ResultCluster(final String name) {
            this.name = name;
        }

        public void addResult(final Result result) {
            results.add(result);
        }

        @Override
        public int compareTo(final ResultCluster o) {
            final int size = results.size();
            final int otherSize = o.results.size();
            if (size == otherSize) {
                return name.compareTo(o.name);
            }
            return otherSize - size;
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
            final ResultCluster other = (ResultCluster) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (results == null) {
                if (other.results != null) {
                    return false;
                }
            } else if (!results.equals(other.results)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((name == null) ? 0 : name.hashCode());
            result = (prime * result) + ((results == null) ? 0 : results.hashCode());
            return result;
        }
    }

    private final Set<String> hosts;

    public StatisticReporter(final Set<String> hosts, final SortedSet<Result> data) {
        super(data);
        this.hosts = hosts;
    }

    private int countAdvertisements(final SortedSet<Result> results) {
        int count = 0;
        for (final Result result : results) {
            if (result.isAdvertisement()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void generate(final PrintWriter writer) {
        writer.println("Statistic");
        writer.println("=========");
        writer.println();

        final Map<String, ResultCluster> clusters = new ConcurrentHashMap<>();
        for (final Result result : data) {
            final URI uri = result.getUri();
            final String host = uri.getHost();
            ResultCluster cluster = clusters.get(host);
            if (cluster == null) {
                cluster = new ResultCluster(host);
                clusters.put(host, cluster);
            }
            cluster.addResult(result);
        }

        final SortedSet<ResultCluster> sortedClusters = new TreeSet<>();
        sortedClusters.addAll(clusters.values());
        for (final ResultCluster cluster : sortedClusters) {
            final String prefix = getPrefix(cluster.name);
            writer.printf("%s %s%n", prefix, cluster.name);
            final int advResults = countAdvertisements(cluster.results);
            writer.printf("   -      total: %d (ADV: %d)%n", cluster.results.size(), advResults);
            writer.printf("   - best rank: %d%n", cluster.results.first().getRank());
            writer.printf("   - best page: %d%n", cluster.results.first().getPage());
        }
    }

    private String getPrefix(final String host) {
        if (hosts.contains(host)) {
            return "*";
        }
        return " ";
    }

}
