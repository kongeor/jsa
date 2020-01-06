# clojure sentiment analysis 

A clojure utility that fetches tweets, analyzes them using 
[Vader](https://github.com/apanimesh061/VaderSentimentJava) and inserts the data into sqlite databases.

## Usage

Register your twitter application as described [here](https://developer.twitter.com/en/docs/basics/apps/overview).

### Building

Make sure [Leiningen](https://leiningen.org/) is installed.

Then just:

```sh
lein uberjar
```

see below how to execute the utility using the produced jar.

### Running using Leiningen

Create a `profiles.clj` file:

```sh
cp profiles.clj.example profiles.clj
```

and set your twitter app key and secret this file.

Run

```sh
lein run clojurescript,thewitcher
```

### Uber run

If you don't want to build and/or install Leiningen, just grab a prebuilt jar from releases.

```sh
java -DTWITTER_APP_KEY=xxx -DTWITTER_APP_SECRET=xxx -jar target/jsa-0.X-X-standalone.jar clojurescript,thewitcher
```

## Data

The produced data will be stored in sqlite databases - each hashtag will get its own database - put 
in a `db` folder.

## License

Copyright © 2019 Kostas Georgiadis

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
