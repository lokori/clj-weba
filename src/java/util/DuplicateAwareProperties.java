package util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Checks for duplicate keys in Java properties.
 */
public class DuplicateAwareProperties extends Properties {

  private final Set<Object> duplicateKeys = new HashSet<Object>();

  public Set<Object> getDuplicates() {
    return Collections.unmodifiableSet(duplicateKeys);
  }

  @Override
  public synchronized Object put(Object key, Object value) {
    if (get(key) != null) {
      duplicateKeys.add(key);
    }
    return super.put(key, value);
  }
}
