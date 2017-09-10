/**
 *   Copyright (c) Shantanu Kumar. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file LICENSE at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 *   the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package cambium.logback.core.strategy;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;
import cambium.logback.core.TurboFilterStrategy;

public class MultiLevelOverrideStrategy implements TurboFilterStrategy {

    private static final int NO_LEVEL = -1;  // this value must be different from all other Logback levels

    private final Level defaultRootLevel;
    private volatile Callable<Boolean> validator = null;
    private final Map<String, Level> overridingLogLevels =
        new ConcurrentHashMap<String, Level>();              // {partial-logger-name : level}
    private final Map<String, Integer> cachedLoggerLevels =
        new ConcurrentHashMap<String, Integer>();            // {full-logger-name : intLevel}

    public MultiLevelOverrideStrategy(Level defaultRootLevel) {
        this.defaultRootLevel = defaultRootLevel;
        reset();
    }

    public void reset() {
        this.validator = null;
        this.overridingLogLevels.clear();
        this.cachedLoggerLevels.clear();
        setRootLogLevel(defaultRootLevel);
    }

    public void removeValidator() {
        this.validator = null;
    }

    public void setValidator(Callable<Boolean> validator) {
        this.validator = validator;
    }

    public Map<String, Level> getLogLevels() {
        return Collections.unmodifiableMap(overridingLogLevels);
    }

    public void removeLogLevel(String partialLoggerName) {
        this.overridingLogLevels.remove(partialLoggerName);
        invalidateCachedLoggerName(partialLoggerName);
    }

    public void setLogLevel(String partialLoggerName, Level logLevel) {
        this.overridingLogLevels.put(partialLoggerName, logLevel);
        invalidateCachedLoggerName(partialLoggerName);
    }

    public void setRootLogLevel(Level logLevel) {
        this.overridingLogLevels.put("", logLevel);
        invalidateCachedLoggerName("");
    }

    private void invalidateCachedLoggerName(String partialLoggerName) {
        for (String fullLoggerName: cachedLoggerLevels.keySet()) {
            if (fullLoggerName.startsWith(partialLoggerName)) {
                cachedLoggerLevels.remove(fullLoggerName);
            }
        }
    }

    /**
     * Cache specified full-logger-name associated with a boolean match against all configured log levels. Return
     * cached value if available, else linear-scan specified log levels for given full-logger-name. Return true on
     * match, false otherwise.
     * @param fullLoggerName full logger name
     * @return integer log-level if found matching, {@link #NO_LEVEL} otherwise
     */
    private Integer cacheLoggerNameMatch(String fullLoggerName) {
        final Integer match = cachedLoggerLevels.get(fullLoggerName);
        if (match != null) {
            return match;
        }
        int matchRank = -1;
        Integer matchIntegerLevel = NO_LEVEL;
        for (Map.Entry<String, Level> overridingLogger: overridingLogLevels.entrySet()) {
            final String partialLoggerName = overridingLogger.getKey();
            if (fullLoggerName.startsWith(partialLoggerName) && partialLoggerName.length() > matchRank) {
                matchRank = partialLoggerName.length();
                matchIntegerLevel = overridingLogger.getValue().toInteger();
            }
        }
        cachedLoggerLevels.put(fullLoggerName, matchIntegerLevel);
        return matchIntegerLevel;
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (validator == null) {
            return FilterReply.NEUTRAL;
        }
        Boolean isValid;
        try {
            isValid = validator.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (isValid == null || !isValid.booleanValue()) {
            return FilterReply.NEUTRAL;
        }
        final String loggerName = logger.getName();
        final Integer integerLevel = cacheLoggerNameMatch(loggerName);
        if (integerLevel == NO_LEVEL) {
            return FilterReply.NEUTRAL;
        }
        final Level overridingLevel = Level.toLevel(integerLevel);
        if (level.isGreaterOrEqual(overridingLevel)) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }

}
