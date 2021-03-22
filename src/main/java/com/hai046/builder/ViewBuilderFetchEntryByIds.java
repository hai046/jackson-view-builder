package com.hai046.builder;

import java.util.Collection;
import java.util.Map;

/**
 * @author hai046
 * @param <ID>
 * @param <Entry>
 */
public interface ViewBuilderFetchEntryByIds<ID, Entry> {

    Map<ID, Entry> getEntries(Collection<ID> ids);

}
