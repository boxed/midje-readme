(defproject midje-readme "1.0.4-SNAPSHOT"
  :description "A Leiningen plugin to pull tests from your README.md into midje."
  :url "https://github.com/boxed/midje-readme"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :uberjar-name "midje-readme.jar"
  :scm {:name "git"
        :url "https://github.com/boxed/midje-readme"}
  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]
  :eval-in-leiningen true
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
