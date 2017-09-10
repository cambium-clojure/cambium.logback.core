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

import org.slf4j.MDC;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;
import cambium.logback.core.TurboFilterStrategy;

public class MDCLevelOverrideStrategy implements TurboFilterStrategy {

    private final String mdcLevelKey;

    public MDCLevelOverrideStrategy(String mdcLevelKey) {
        this.mdcLevelKey = mdcLevelKey;
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        final String mdcValue = MDC.get(this.mdcLevelKey);
        if (mdcValue == null) {
            return FilterReply.NEUTRAL;
        }
        final Level overrideLevel = Level.toLevel(mdcValue, /* no default level */ null);
        if (overrideLevel == null) {
            return FilterReply.NEUTRAL;
        }
        if (level.isGreaterOrEqual(overrideLevel)) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }

}
