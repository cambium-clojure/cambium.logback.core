;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns cambium.logback.core.strategy
  "Log level override utility fns based on strategy turbo-filter.
  Example config (logback.xml):

    <turboFilter class=\"cambium.logback.core.StrategyTurboFilter\">
      <name>mdcStrategy</name>
    </turboFilter>

    <turboFilter class=\"cambium.logback.core.StrategyTurboFilter\">
      <name>multiStrategy</name>
    </turboFilter>

  Here we define two strategy based turbo-filters that would probably be configured during app init phase."
  (:import
    [clojure.lang Named]
    [ch.qos.logback.classic Level]
    [cambium.logback.core StrategyTurboFilter]
    [cambium.logback.core.strategy MDCLevelOverrideStrategy MultiLevelOverrideStrategy MultiLevelOverrideValidator]))


(defn- as-str
  "Convert argument to string. Special handling for: clojure.lang.Named (e.g. keyword, symbol)."
  ^String [x]
  (if (instance? Named x)
    (let [^String the-ns (.getNamespace ^Named x)
          ^String x-name (.getName ^Named x)]
      (if (nil? the-ns)
        x-name
        (str the-ns \/ x-name)))
    (str x)))


(defn set-mdc-strategy!
  "Set turbo-filter strategy for the given strategy name based on specified MDC attribute, whose value can be set to a
  log level string to control which log levels are allowed to be logged."
  [^String mdc-strategy-name ^String mdc-key]
  (StrategyTurboFilter/setStrategy mdc-strategy-name (MDCLevelOverrideStrategy. (as-str mdc-key))))


(defn set-multi-strategy!
  "Set turbo-filter strategy for the given strategy name based on multiple logger names, each of which can be assigned
  with a log level string to control which log levels are the loggers allowed to log at. A validator is a no-argument
  function (or instance of the java.util.concurrent.Callable interface) that returns a boolean value. The validator
  argument is used to determine how long should the log-level override be considered valid."
  [^String multi-strategy-name ^String root-log-level validator]
  (when-let [^MultiLevelOverrideStrategy multi-strategy (MultiLevelOverrideStrategy. (Level/toLevel
                                                                                       (as-str root-log-level)))]
    (StrategyTurboFilter/setStrategy multi-strategy-name multi-strategy)
    (.setValidator multi-strategy validator)))


(defn multi-count-validator
  "Return a validator based on specified count of times when log level override should be applied.
  See: set-multi-strategy!"
  [^long n]
  (MultiLevelOverrideValidator/untilCount n))


(defn multi-millis-validator
  "Return a validator based on the duration (in milliseconds) for which the log level override should be applied.
  See: set-multi-strategy!"
  [^long millis]
  (MultiLevelOverrideValidator/untilDurationMillis millis))


(def multi-forever-validator
  "A validator that always causes log level to be overridden.
  See: set-multi-strategy!"
  MultiLevelOverrideValidator/FOREVER_TRUE)


(defn multi-log-levels
  "Return a map of logger names to levels in the current multi-log-override configuration."
  [^String multi-strategy-name]
  (when-let [^MultiLevelOverrideStrategy multi-strategy (StrategyTurboFilter/getStrategy multi-strategy-name)]
    (->> (seq (.getLogLevels multi-strategy))
      (map (fn [[k v]] {k (str v)}))
      (reduce merge {}))))


(defn remove-log-level!
  "Remove specified logger names from the current multi-log-override configuration."
  [^String multi-strategy-name logger-names]
  (when-let [^MultiLevelOverrideStrategy multi-strategy
             (StrategyTurboFilter/getStrategy multi-strategy-name)]
    (doseq [^String each-name logger-names]
      (.removeLogLevel multi-strategy each-name))))


(defn set-log-level!
  "Set the specified log level for the given logger names in the current multi-log-override configuration."
  [^String multi-strategy-name logger-names ^String log-level]
  (when-let [^MultiLevelOverrideStrategy multi-strategy (StrategyTurboFilter/getStrategy multi-strategy-name)]
    (doseq [^String each-name logger-names]
      (.setLogLevel multi-strategy each-name (Level/toLevel (as-str log-level))))))


(defn remove-strategy!
  "Remove a configured strategy by its name."
  [strategy-name]
  (StrategyTurboFilter/removeStrategy strategy-name))
