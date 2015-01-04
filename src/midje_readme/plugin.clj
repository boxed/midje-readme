(ns midje-readme.plugin
  (:require [leiningen.core.eval :refer :all]
            [leiningen.new.templates :as templates]
            [watchtower.core :as wt]))

(def README_FILENAME "README.md")

(def OUTPUT_FILENAME "test/readme.clj")

(defn- readme? [file]
  (and (= "." (.getParent file))
       (= README_FILENAME (.getName file))))

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

(defn- guess-namespace-to-use-for-require [{:keys [group name]}]
  (templates/multi-segment
   (if (= name group)
     group
     (format "%s.%s" group name))))

(defn write-readme-tests! [project]
  (spit OUTPUT_FILENAME
        (readme-to-midje-test (slurp README_FILENAME)
                              (or (get-in project [:midje-readme :require])
                                  (format "[%s :refer :all]"
                                          (guess-namespace-to-use-for-require project))))))

(defn middleware [project]
  (let [used-clojure-version (read-string (with-out-str (eval-in project '(prn *clojure-version*))))]
    (if (or (> (:major used-clojure-version) 1)
            (> (:minor used-clojure-version) 3))
      (wt/watcher ["."]
        (wt/rate 100)
        (wt/file-filter readme?)
        (wt/on-change (fn [_] (write-readme-tests! project))))
      (do
        (io/delete-file OUTPUT_FILENAME true)
        (warn "Warning: midje-readme doesn't support clojure < 1.4. The readme will not be tested"))))

  project)
