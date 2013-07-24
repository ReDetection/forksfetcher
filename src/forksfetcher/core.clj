(ns forksfetcher.core
  (:use [clojure.string :only (split)])
  (:require [clojure.java.io :as io] [clj-jgit.porcelain :as git] tentacles.repos)
  (:gen-class :main true))

(defn remove-from-end [s end]
  (if (.endsWith s end)
      (.substring s 0 (- (count s) (count end)))
      s))

(defn githubHtmlUrl [gitrepo]
  (let [conf (.getConfig (.getRepository gitrepo))]
    (let [origin (first (.getSubsections conf "remote"))]
      (remove-from-end (.getString conf "remote" origin "url") ".git"))))

(defn namesFromUrl [githubURL]
  (let [parts (drop 3 (split githubURL #"/"))]
    [(first parts) (first (rest parts))]))

(defn getAllPagesWForks [repourl]
  (let [[user reponame] (namesFromUrl repourl)]
    (loop [result nil
           page 1]
      (let [forkspage (tentacles.repos/forks user reponame {:per_page 100 :page page})] 
        (if (:message forkspage)
          (do (println "Rate limit exceeded, I will break") [result 1])
          (if (< 0 (count forkspage))
            (recur (concat result forkspage) (+ 1 page))
            [result 0]))))))

(defn getForks [repourl]
  (loop [giturls nil
         htmlurls (list repourl)]
    (if (first htmlurls)
      (let [[forksInfo limitWasReached] (getAllPagesWForks (first htmlurls))]
        (if (= 1 limitWasReached)
          giturls
          (recur (concat giturls (map :clone_url forksInfo))
                 (concat (rest htmlurls) (filter identity (map (fn ([x] (if (> (:forks_count x) 0) (:html_url x)))) forksInfo))))))
      giturls)))

(defn addRemoteAndFetch [gitrepo url]
  (let [[user reponame] (namesFromUrl url)
        conf (.getConfig (.getRepository gitrepo))]
    (.setString conf "remote" user "url" url)
    (.setString conf "remote" user "fetch" (str "+refs/heads/*:refs/remotes/" user "/*"))
    (.save conf)
    (try 
      (git/git-fetch gitrepo user) (println (str "Fetched " user "'s fork")) ;(str "+refs/heads/*:refs/remotes/" user "/*") ;(println "before fetching") 
      (catch org.eclipse.jgit.api.errors.TransportException e 
        (println (str "Nothing to fetch from " user "'s fork")))
      (catch java.lang.Exception e 
        (println (str "Can't fetch " user "'s fork — please check availability at url " (remove-from-end url ".git")))))))

(defn addForks [gitrepo forks]
  (dorun (for [fork forks]
    (addRemoteAndFetch gitrepo fork))))

(defn -main 
  "The application's main function"
  ([& args]
    (let [repo-dir (if args (first args) (System/getProperty "user.dir"))]
      (if-let [repo (try (git/load-repo repo-dir) (catch java.io.FileNotFoundException e nil) )]
        (let [repoURL (githubHtmlUrl repo)]
          (println (str "First remote url at " repo-dir " is " repoURL))
          (let [forks (getForks repoURL)]
            (println "It has" (count forks) "forks")
            (addForks repo forks)))
        "Can't find any git repo"))))
