;;   Copyright (c) Zachary Tellman. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns sleight.core
  (:require
    [sleight.rt :as rt]))

;;; adapted from clojure.core

(def ^:private check-cyclic-dependency #'clojure.core/check-cyclic-dependency)
(def ^:private root-resource #'clojure.core/root-resource)
(def ^:private root-directory #'clojure.core/root-directory)

(defn- load*
  [transform & paths]
  (doseq [^String path paths]
    (let [^String path (if (.startsWith path "/")
                          path
                          (str (root-directory (ns-name *ns*)) \/ path))]
      (check-cyclic-dependency path)
      (let [pending-paths @#'clojure.core/*pending-paths*]
        (when-not (= path (first pending-paths))
          (with-bindings {#'clojure.core/*pending-paths* (conj pending-paths path)}
            (rt/load* transform (.substring path 1))))))))

(defn hijack-reader
  [& {:keys [pre post transform]}]

  (when pre (pre))

  (when transform
    (alter-var-root #'clojure.core/load
      (constantly (partial load* transform))))

  (when post
    (.addShutdownHook (Runtime/getRuntime)
      (Thread. post))))

(defmacro def-transform [name & {:as options}]
  `(def ~name options))



