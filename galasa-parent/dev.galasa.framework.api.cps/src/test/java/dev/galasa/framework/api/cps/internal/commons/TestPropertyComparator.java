/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.commons;

import java.util.*;

import org.junit.Test;

import dev.galasa.framework.api.cps.internal.common.PropertyComparator;

import static org.assertj.core.api.Assertions.*;


public class TestPropertyComparator{
    
    private Map<String, String> sort(Collection<String> unsorted ) {
        PropertyComparator comparator = new PropertyComparator();
        Map<String,String> sorted = new TreeMap<String,String>(comparator);
    
        for( String key : unsorted ) {
            sorted.put(key,"1"); // All properties have value 1. We don't care for testing.
        }
    
        return sorted;
    }

    @Test
    public void testTwoEntriesInCorrectOrder() {

        List<String> keys = new ArrayList<>();
        keys.add("a.b.c.d");
        keys.add("a.b.cdefg");

        Map<String,String> results = sort(keys);
        assertThat(results.size()).isEqualTo(2);
        Iterator<String> walker = results.keySet().iterator();
        assertThat(walker.next()).isEqualTo("a.b.c.d");
        assertThat(walker.next()).isEqualTo("a.b.cdefg");
    }

    @Test
    public void testTwoEntriesInWrongOrder() {

        List<String> keys = new ArrayList<>();
        keys.add("a.b.cdefg");
        keys.add("a.b.c.d");


        Map<String,String> results = sort(keys);
        assertThat(results.size()).isEqualTo(2);
        Iterator<String> walker = results.keySet().iterator();
        assertThat(walker.next()).isEqualTo("a.b.c.d");
        assertThat(walker.next()).isEqualTo("a.b.cdefg");
    }

    @Test
    public void testTwoEntriesLongestLaterAlphabetically() {

        List<String> keys = new ArrayList<>();
        keys.add("a.b.adefg");
        keys.add("a.b.c.d");

        Map<String,String> results = sort(keys);
        assertThat(results.size()).isEqualTo(2);
        Iterator<String> walker = results.keySet().iterator();
        assertThat(walker.next()).isEqualTo("a.b.c.d");
        assertThat(walker.next()).isEqualTo("a.b.adefg");
    }

    @Test
    public void testTwoEntriesAlphabeticallySortedThirdPart() {

        List<String> keys = new ArrayList<>();
        keys.add("a.b.a.d");
        keys.add("a.b.c.d");

        Map<String,String> results = sort(keys);
        assertThat(results.size()).isEqualTo(2);
        Iterator<String> walker = results.keySet().iterator();
        assertThat(walker.next()).isEqualTo("a.b.a.d");
        assertThat(walker.next()).isEqualTo("a.b.c.d");
    }

    @Test
    public void testComparatorInDetail() {

        PropertyComparator comparator = new PropertyComparator();

        assertThat( comparator.compare("a", "b")).isLessThan(0);
        assertThat( comparator.compare("b", "a")).isGreaterThan(0);
        assertThat( comparator.compare("a.a", "a.b")).isLessThan(0);
        assertThat( comparator.compare("a.b", "a.a")).isGreaterThan(0);
        assertThat( comparator.compare("a.a", "a")).isLessThan(0);
        assertThat( comparator.compare("a", "a.a")).isGreaterThan(0);

        assertThat( comparator.compare("a", "a")).isEqualTo(0);
    }

    @Test
    public void testComparatorwithNull() {

        PropertyComparator comparator = new PropertyComparator();
        assertThat( comparator.compare("a", null)).isLessThan(0);
        assertThat( comparator.compare(null, "a")).isGreaterThan(0);
        assertThat( comparator.compare(null, null)).isEqualTo(0);
    }

}