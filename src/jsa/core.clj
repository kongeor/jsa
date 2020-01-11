(ns jsa.core
  (:require [twitter.oauth :as oauth]
            [twitter.api.restful :as api]
            [jsa.db :as db]
            [environ.core :refer [env]])
  (:import (com.vader.sentiment.analyzer SentimentAnalyzer))
  (:gen-class))

(defn ensure-creds-are-passed []
  (assert (env :twitter-app-key) "twitter app key is not set")
  (assert (env :twitter-app-secret) "twitter app secret is not set"))

(defn get-creds []
  (oauth/make-oauth-creds (env :twitter-app-key) (env :twitter-app-secret)))

(defn get-tweets-after [query id]
  (let [my-creds (get-creds)
        tweets (api/search-tweets
                 :oauth-creds my-creds
                 :params {:result_type "recent"
                          :lang "en"
                          :count 100
                          :max_id id
                          :q (str query " AND -filter:retweets AND -filter:replies")
                          :tweet_mode "extended"})]
    (mapv (fn [e] [(:id e) (:full_text e)])
      (-> tweets :body :statuses))))

(defn insert-tweet-data [[id text] query]
  (try
    (db/insert-tweet id text query)
    (catch Exception e
      (println "Could not insert tweet" e))))

(defn analyze [text]
  (let [sa (SentimentAnalyzer. text)]
    (.analyze sa)
    (.getPolarity sa)))

#_(get (analyze "this is awesome") "positive")

(defn analyze-tweet [tweet]
  (let [text (:tweets/text tweet)
        polarity (analyze text)]
    {:negative (get polarity "negative")
     :neutral (get polarity "neutral")
     :positive (get polarity "positive")
     :compound (get polarity "compound")}))

#_(analyze-tweet 1210501431448330241)

(defn analyze-tweets-without-polarity []
  (let [tweets (db/find-tweets-without-polarity)]
    (println "analyzing tweets")
    (doseq [t tweets]
      (db/update-polarity (:tweets/id t) (analyze-tweet t)))
      #_(map (fn [[id polar]] (db/update-polarity id polar)) (pmap analyze-tweet tweets))))

#_(analyze-tweets-without-polarity)

(defn run [query]
  (assert query "query not specified")
  (db/set-datasource-by-name! "tweets")
  (let [max-db-id (db/find-max-id query)]
    (loop [min-run-id nil]
      (println "Processing" query "tweets after" min-run-id)
      (let [tweets (get-tweets-after query min-run-id)
            _ (println "Found" (count tweets) "for" query)
            min-id (apply min (map first tweets))
            recur? (and (not= min-run-id min-id)
                     (or (nil? max-db-id) (> min-id max-db-id)))]
        (do
          (doseq [t tweets]
            (insert-tweet-data t query))
          (analyze-tweets-without-polarity)
          (Thread/sleep 1000)
          (if recur?
            (recur min-id)
            (println "no new data")))))))

(comment
  (run "super mario bros, links awakening"))

(defn run-all [queries]
  (let [tags (clojure.string/split queries #",")]
    (doseq [t tags]
      (run t))))


(comment
  (run-all "clojure")
  (run-all "clojure,clojurescript,golang,haskell")
  (run-all "clojurescript")
  (run-all "rustlang,java,ruby,python,javascript")
  (run-all "python,javascript")
  (run-all "red alert, super mario")
  (run-all "super mario bros, links awakening")
  (get-tweets-after nil))


(comment
  (def data (api/search-tweets :oauth-creds (get-creds) :params {
                                                              ; :result_type "recent"
                                                              :lang "en"
                                                              :count 10
                                                              :max_id 1210085731042881536
                                                              :q "#thewitcher AND -filter:retweets AND -filter:replies"
                                                              :tweet_mode "extended"}))
  (first data)
  (map (juxt :text :id :url) (filter  #(not (% :retweeted_status)) (-> data :body :statuses)))
  (map :text (-> data :body :statuses))
  (-> data :body :statuses)
  (apply min (map :id (-> data :body :statuses))))


(defn -main [& args]
  (ensure-creds-are-passed)
  (let [hashtags (nth args 0)]
    (assert hashtags "hashtag(s) param is required")
    (run-all hashtags)))
