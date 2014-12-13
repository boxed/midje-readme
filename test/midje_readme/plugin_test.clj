(ns midje-readme.plugin-test
  (:require [midje-readme.plugin :ref :all]
            [midje.sweet :refer :all]
            [midje.util :refer [testable-privates]]))

(testable-privates midje-readme.plugin
                   guess-namespace-to-use-for-require)

(facts "guess-namespace-to-use-for-require (using Leiningen default)"
  (fact "\"no\" group, simple name (lein new abc)"
    (guess-namespace-to-use-for-require {:group "abc" :name "abc"})
    => "abc.core")
  (fact "\"no\" group, complex name (lein new def.ghi)"
    (guess-namespace-to-use-for-require {:group "def.ghi" :name "def.ghi"})
    => "def.ghi")
  (fact "group, simple name (lein new jkl.mno/pqr)"
    (guess-namespace-to-use-for-require {:group "jkl.mno" :name "pqr"})
    => "jkl.mno.pqr")
  (fact "group, complex name (lein new sru.wxy/zab.cde)"
    (guess-namespace-to-use-for-require {:group "sru.wxy" :name "zab.cde"})
    => "sru.wxy.zab.cde"))

