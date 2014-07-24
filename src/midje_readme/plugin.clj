(ns midje-readme.plugin)

(defn readme-to-midje-test [readme require-str]
  (let [keep-line (atom false)
        is-code-start #(= %1 "```clojure")
        is-code-stop #(= %1 "```")]
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
  (spit "test/readme.clj"
        (readme-to-midje-test (slurp "README.md")
                              (or (get-in project [:midje-readme :require])
                                  (format "[%s.core :refer :all]" (:group project)))))

  project)
