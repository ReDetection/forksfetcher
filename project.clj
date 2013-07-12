(defproject forksfetcher "0.1.0-SNAPSHOT"
  :description "Easiest way to look through all forks"
  :url "https://github.com/ReDetection/forksfetcher"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"],
                 [tentacles "0.2.4"],
                 [clj-jgit "0.3.8"]
                ],
  :main forksfetcher.core)
