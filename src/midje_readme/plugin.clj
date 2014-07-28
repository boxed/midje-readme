(ns midje-readme.plugin
  (:require [leiningen.core.eval :refer :all]))

(defn readme-to-midje-test [readme require-str]
  (let [keep-line (atom false)
        is-code-start #(= (clojure.string/trim %1) "```clojure")
        is-code-stop #(= (clojure.string/trim %1) "```")]
    (clojure.string/join "\n" (assoc
                                (into []
                                      (for [line (clojure.string/split-lines readme)]
                                         (cond
                                          (and (is-code-start line) (not @keep-line))
                                            (do (swap! keep-line not) "(use 'midje.sweet) (ns readme) (fact")
                                          (and (is-code-stop line) @keep-line)
                                            (do (swap! keep-line not) ")")
                                          :else
                                             (if @keep-line
                                               line
                                                ""))))
                                0 (format "(ns readme (:use midje.sweet) (:require %s)) (def ... :...)" require-str)))))

(defn middleware [project]
  (let [used-clojure-version (read-string (with-out-str (eval-in project '(prn *clojure-version*))))]
    (if (or (> (:major used-clojure-version) 1)
            (> (:minor used-clojure-version) 3))
      (spit "test/readme.clj"
            (readme-to-midje-test (slurp "README.md")
                                  (or (get-in project [:midje-readme :require])
                                      (format "[%s.core :refer :all]" (:group project)))))
      (do
        (clojure.java.io/delete-file "test/readme.clj" true)
        (println "Warning: midje-readme doesn't support clojure < 1.4. The readme will not be tested"))))

  project)
