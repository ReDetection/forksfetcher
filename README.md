# forks fetcher

A Clojure application designed to add remotes for every fork on the github.

If you found interesting repo, but isn't good enough for you, it's often very hard to choose which fork does exactly what you want. With this fetcher you're able to quickly add all forks as remotes in your local repository and just look througs all commits in all repos.

## Usage
Easiest way — is just grab and run jar file (you need only java machine to be installed):

```
wget https://github.com/ReDetection/forksfetcher/raw/download/target/forksfetcher-0.1.0.jar
java -jar forksfetcher-0.1.0.jar /path/to/cloned/repo
```

Or you can clone the source code and run in leiningen:

```
git clone https://github.com/ReDetection/forksfetcher.git
cd forksfetcher
lein run /path/to/cloned/repo
```

You can omit path if you want to fetch forks in current directory.

## License

Copyright © 2013 ReDetection

Distributed under the Eclipse Public License, the same as Clojure.

## Contribute

If you want to contribute just fork the repository, work on the code, cover it with tests and submit a pull request through Github. Tasks I need to do:

* look also for parent, not only for child repos
* continue to forks search after reaching github api rate limit 
* try to add remotes just from html_url, not from clone_url
