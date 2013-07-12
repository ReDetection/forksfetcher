(ns forksfetcher.core
  (:require [clojure.java.io :as io] [clj-jgit.porcelain :as git])
  (:gen-class :main true))


(defn originURL [gitrepo]
  ; (str "asd" "asgsg")
  (def conf (.getConfig (.getRepository gitrepo)))
  (def origin (first (.getSubsections conf "remote")))
  (.getString conf "remote" origin "url")
)

(defn -main 
   "The application's main function"
   [& args]
   (def repo-dir (System/getProperty "user.dir"))
   (if-let [repo (git/load-repo repo-dir)]
       (println (str "First remote url at " repo-dir " is " (originURL repo)))
   )
)
