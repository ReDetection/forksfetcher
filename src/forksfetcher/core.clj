(ns forksfetcher.core
  (:use [clojure.string :only (split)])
  (:require [clojure.java.io :as io] [clj-jgit.porcelain :as git] tentacles.repos)
  (:gen-class :main true))


(defn originURL [gitrepo]
  (def conf (.getConfig (.getRepository gitrepo)))
  (def origin (first (.getSubsections conf "remote")))
  (.getString conf "remote" origin "url")
)

(defn namesFromUrl [githubURL]
  (def parts (drop 3 (split githubURL #"/")))
  [(first parts) (first (rest parts))]
)

(defn getForks [repourl]
  (loop [giturls nil
         htmlurls (list repourl)]
    (if (first htmlurls)
      (let [[user reponame] (namesFromUrl (first htmlurls))]
        (def forksInfo (tentacles.repos/forks user reponame))
        (if (:message forksInfo)
          (do (println "Rate limit exceeded, I will break") giturls)
          (do 
            (def gitsadd (map :clone_url forksInfo))
            (def htmlsadd (map :html_url forksInfo))
            (recur (concat giturls gitsadd)
                   (concat (rest htmlurls) htmlsadd))
            )
        )
      )
      giturls
    )
  )  
)

(defn addRemoteAndFetch [gitrepo url]
  (let [[user reponame] (namesFromUrl url)]
    (def conf (.getConfig (.getRepository gitrepo)))
    (.setString conf "remote" user "url" url)
    (.setString conf "remote" user "fetch" (str "+refs/heads/*:refs/remotes/" user "/*"))
    (.save conf)
    (try 
      (git/git-fetch gitrepo user) (println (str "Fetched " user "'s fork")) ;(str "+refs/heads/*:refs/remotes/" user "/*") ;(println "before fetching") 
      (catch org.eclipse.jgit.api.errors.TransportException e 
        (println (str "Nothing to fetch from " user "'s fork")))
    )
  )
)

(defn addForks [gitrepo forks]
  (for [fork forks]
    (addRemoteAndFetch gitrepo fork)
  )
)

(defn -main 
  "The application's main function"
  ([& args]
    (def repo-dir (if args (first args) (System/getProperty "user.dir")))
    (def repo (git/load-repo repo-dir)) ; catch exception 
    (def repoURL (originURL repo))
    (println (str "First remote url at " repo-dir " is " repoURL))
    (def forks (getForks repoURL))
    (println (str "It has " (count forks) " forks"))
    (addForks repo forks)
  )
)
