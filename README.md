# clojure sentiment analysis 

This is a clojure utility that fetches tweets, analyzes them using 
[Vader](https://github.com/apanimesh061/VaderSentimentJava) and inserts the data into a sqlite database.

## Usage

Register your application as described [here](https://developer.twitter.com/en/docs/basics/apps/overview).

### Building

### Running using Leiningen

Create a `profiles.clj` file:

```sh
cp profiles.clj.example profiles.clj
```

Update you twitter app key and secret

Run

```sh
lein run clojurescript,thewitcher
```

### Running 

```sh

```


```sh
java -DTWITTER_APP_KEY=xxx -DTWITTER_APP_SECRET=xxx -jar target/jsa-0.1.0-SNAPSHOT-standalone.jar clojurescript,thewitcher
```




FIXME

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
