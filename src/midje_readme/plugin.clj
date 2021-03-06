(ns midje-readme.plugin
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [leiningen.core.main :refer [warn]]
            [leiningen.core.eval :refer :all]
            [leiningen.new.templates :as templates]
            [watchtower.core :as wt])
  (:import (java.io ByteArrayOutputStream PrintStream FileDescriptor
                    FileOutputStream)))

(def README_FILENAME "README.md")

(def OUTPUT_FILENAME "test/readme.clj")

(def ^:private is-code-start? (comp #{"```clojure"} str/trim))

(def ^:private is-code-stop?  (comp #{"```"} str/trim))

(defn- readme? [file]
  (and (= "." (.getParent file))
       (= README_FILENAME (.getName file))))

(defn- process-readme-line [keep-line line]
  (cond
   (and (is-code-start? line) (not keep-line))
     [true "(use 'midje.sweet) (ns readme) (fact"]
   (and (is-code-stop? line) keep-line)
     [false ")"]
   :else
     [keep-line (if keep-line line "")]))

(defn- readme-to-midje-test [readme require-str refer-clojure-str]
  (let [acc   [(format "(ns readme (:use midje.sweet) (:require %s) %s) (def ... :...)"
                       require-str
                       (if refer-clojure-str
                         (format "(:refer-clojure %s)" refer-clojure-str)
                         ""))]
        lines (rest (str/split-lines readme))]
    (str/join "\n" (loop [acc acc, keep-line false, [line & rest] lines]
                     (if-not line
                       acc
                       (let [[keep-line output] (process-readme-line keep-line line)]
                         (recur (conj acc output) keep-line rest)))))))

(defn- guess-namespace-to-use-for-require [{:keys [group name]}]
  (templates/multi-segment
   (if (= name group)
     group
     (format "%s.%s" group name))))

(defn- write-readme-tests! [project]
  (spit OUTPUT_FILENAME
        (readme-to-midje-test (slurp README_FILENAME)
                              (or (get-in project [:midje-readme :require])
                                  (format "[%s :refer :all]"
                                          (guess-namespace-to-use-for-require project)))
                              (get-in project [:midje-readme :refer-clojure]))))

(defn- keep-writing-tests!
  [project]
  (write-readme-tests! project)
  (wt/watcher ["."]
              (wt/rate 100)
              (wt/file-filter readme?)
              (wt/on-change (fn [_] (write-readme-tests! project)))))

(defn- fail
  []
  (io/delete-file OUTPUT_FILENAME true)
  (warn "Warning: midje-readme doesn't support clojure < 1.4. The readme will not be tested"))

;; Taken from https://github.com/technomancy/leiningen/blob/master/test/leiningen/test/helper.clj
;; See https://github.com/technomancy/leiningen/commit/c35a1de4f0b45365a97e6e54c1eba309b45b7f46
(defmacro with-system-out-str
  "Like with-out-str, but for System/out."
  [& body]
  `(try (let [o# (ByteArrayOutputStream.)]
          (System/setOut (PrintStream. o#))
          ~@body
          (.toString o#))
        (finally
          (System/setOut
           (-> FileDescriptor/out FileOutputStream. PrintStream.)))))

(defn middleware [project]
  (let [used-clojure-version (read-string
                              (with-system-out-str
                                (eval-in project
                                         '(prn *clojure-version*))))]
    (if (or (> (:major used-clojure-version) 1)
            (> (:minor used-clojure-version) 3))
      (keep-writing-tests! project)
      (fail))
    project))

