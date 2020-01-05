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

(defn get-tweets-after [hashtag id]
  (let [my-creds (get-creds)
        tweets (api/search-tweets
                 :oauth-creds my-creds
                 :params {:result_type "recent"
                          :lang "en"
                          :count 100
                          :max_id id
                          :q (str hashtag " AND -filter:retweets AND -filter:replies")
                          :tweet_mode "extended"})]
    (mapv (fn [e] [(:id e) (:full_text e)])
      (-> tweets :body :statuses))))

(defn insert-tweet-data [[id text]]
  (try
    (db/insert-tweet id text)
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

(defn run [hashtag]
  (assert hashtag "hashtag not specified")
  (db/set-datasource-by-name! hashtag)
  (loop [max-id (db/find-min-id)]
    (println "Processing" hashtag "tweets after" max-id)
    (let [tweets (get-tweets-after hashtag max-id)
          min-id (apply min (map first tweets))]
      (if (= max-id min-id)
        (println "No new data fetched. Stopping ...")
        (do
          (doseq [t tweets]
            (insert-tweet-data t))
          (analyze-tweets-without-polarity)
          (Thread/sleep 1000)
          (recur min-id))))))

(defn run-all [hashtags]
  (let [tags (clojure.string/split hashtags #",")]
    (doseq [t tags]
      (run t))))


(comment
  (run-all "clojure,clojurescript,golang,haskell")
  (run-all "clojurescript")
  (run-all "rustlang,java,ruby,python,javascript")
  (run-all "python,javascript")
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
