package org.libelektra.lyrebird.service;

import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.errortypes.AbstractErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InjectionFilterService {

    private final static Logger LOG = LoggerFactory.getLogger(InjectionFilterService.class);
    private final Collection<AbstractErrorType> allErrors;

    @Autowired
    public InjectionFilterService(Collection<AbstractErrorType> allErrors) {
        this.allErrors = allErrors;
    }

    public void filter(KeySet errorKeys) {
        int initialSize = errorKeys.length();
        errorKeys.rewind();
        Iterator<Key> iterator = errorKeys.iterator();
        while (iterator.hasNext()) {
            Key current = iterator.next();
            List<Integer> applicableMetas = Arrays.stream(current.getMeta("types").getString().split(","))
                    .map(String::trim)
                    .filter(str -> str.matches("\\d+"))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
            boolean relevantMeta = allErrors.stream()
                    .anyMatch(a -> a.canBeApplied(applicableMetas));
            if (!relevantMeta) {
                iterator.remove();
            }
        }
        LOG.info("Filtering injectionKeys for applicable. Before: {}, After: {}", initialSize, errorKeys.length());
    }
}
