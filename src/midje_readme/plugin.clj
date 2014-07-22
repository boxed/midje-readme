(ns midje-readme.plugin)

(defn readme-to-midje-test [readme require-str]
  (let [keep-line (atom false)
        is-code-start-stop #(or (= %1 "```") (= %1 "```clojure"))]
    (clojure.string/join "\n" (assoc
                                (into [] (filter #(not (is-code-start-stop %))
                                                 (for [line (clojure.string/split-lines readme)]
                                                   (if (is-code-start-stop line)
                                                     (if @keep-line
                                                       (do (swap! keep-line not) ")")
                                                       (do (swap! keep-line not) "(fact")
                                                       )

                                                     (if @keep-line
                                                       line
                                                       "")))))
                                0 (format "(ns readme (:use midje.sweet) (:require %s))" require-str)))))

(defn middleware [project]
  (spit "test/readme.clj"
        (readme-to-midje-test (slurp "README.md")
                              (or (get-in project [:midje-readme :require])
                                  (format "[%s.core :refer :all]" (:group project)))))

  project)
