# midje-readme

A Leiningen plugin to pull tests from your README.md into midje automatically. This will make it easy to make sure your readme doesn't contain any errors.

## Latest version

[![Clojars Project](http://clojars.org/midje-readme/latest-version.svg)](http://clojars.org/midje-readme)

## Usage

Put `[midje-readme "1.0.6"]` into the `:plugins` vector of your project.clj.

By default midje-readme will put

`
(:require [your-project-namespace :refer :all])
`

into the test file, attempting to guess at the namespace to use according the "lein new" default.

You can override this by putting

`
:midje-readme {:require "[your-project.something-else :refer [whatever]]"}
`

into your project.clj.

When you run a leiningen command the plugin will create a file "test/readme.clj" that contains the code in your readme. Line numbers will be the same as in your readme so debugging should be pretty easy.

You might want to add test/readme.clj to your .gitignore too.

You can show that your README is tested with this badge: [![Examples tested with midje-readme](http://img.shields.io/badge/readme-tested-brightgreen.svg)](https://github.com/boxed/midje-readme) Copy paste the markdown syntax below:

    [![Examples tested with midje-readme](http://img.shields.io/badge/readme-tested-brightgreen.svg)](https://github.com/boxed/midje-readme)
