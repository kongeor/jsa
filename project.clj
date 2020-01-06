(defproject jsa "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [seancorfield/next.jdbc "1.0.12"]
                 [org.xerial/sqlite-jdbc "3.28.0"]
                 [honeysql "0.9.8"]
                 [twitter-api "1.8.0"]
                 [com.github.apanimesh061/vader-sentiment-analyzer "1.0"]
                 [log4j/log4j "1.2.17"]
                 [org.slf4j/slf4j-nop "1.7.30"]
                 [org.apache.lucene/lucene-analyzers-common "6.4.1"]
                 [environ"1.1.0"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-pprint "1.2.0"]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :profiles {:uberjar {:aot :all}}
  :main ^:skip-aot jsa.core
  :repl-options {:init-ns jsa.core})
