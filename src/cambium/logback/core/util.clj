;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns cambium.logback.core.util
  (:import
    [org.slf4j LoggerFactory]
    [ch.qos.logback.classic LoggerContext]))


(defn find-logger-context
  "Return current/default logger-context."
  ^LoggerContext []
  (LoggerFactory/getILoggerFactory))


(defn logger-context-name
  "Return name of the specified logger-context."
  [^LoggerContext logger-context]
  (.getName logger-context))


(defn start-logger-context
  "Start the specified logger-context."
  [^LoggerContext logger-context]
  (.start logger-context))


(defn stop-logger-context
  "Stop the specified logger-context."
  [^LoggerContext logger-context]
  (.stop logger-context))
