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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

public class MultiLevelOverrideValidator {

    public static final Callable<Boolean> FOREVER_TRUE = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            return Boolean.TRUE;
        }
    };

    public static Callable<Boolean> untilDurationMillis(final long durationMillis) {
        return untilTimeMillis(System.currentTimeMillis() + durationMillis);
    }

    public static Callable<Boolean> untilTimeMillis(final long untilTimeMillis) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return System.currentTimeMillis() < untilTimeMillis;
            }
        };
    }

    public static Callable<Boolean> untilCount(final long count) {
        final AtomicLong counter = new AtomicLong();
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (counter.get() >= count) {  // short-circuit if threshold crossed
                    return false;
                }
                return counter.getAndIncrement() < count;
            }
        };
    }

}
