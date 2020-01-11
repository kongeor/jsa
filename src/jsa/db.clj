(ns jsa.db
  (:require [clojure.java.io :as io]
            [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :refer :all :as helpers])
  (:refer-clojure :exclude [update]))

(defonce ds (atom nil))

(defn strip-non-char [s]
  (clojure.string/join (filter (fn [x] (Character/isLetter x)) s)))

#_(strip-non-char "#asdf!")

(defn ds-name [name]
  (let [db-name (str "db/" (strip-non-char name) ".db")]
    (io/make-parents db-name)
    (str "jdbc:sqlite:" db-name)))

#_(ds-name "$foo!")

(defn create-schema []
  (jdbc/execute! @ds
    ["CREATE TABLE IF NOT EXISTS tweets (
      id INTEGER NOT NULL UNIQUE,
      created_at DATETIME NOT NULL,
      query TEXT,
      text TEXT NOT NULL,
      negative REAL,
      neutral REAL,
      positive REAL,
      compound REAL
    )"])
  (jdbc/execute! @ds
    ["CREATE INDEX IF NOT EXISTS idx_tweets_negative ON tweets(negative)"])
  (jdbc/execute! @ds
    ["CREATE INDEX IF NOT EXISTS idx_tweets_created_at ON tweets(created_at)"])
  )

(defn set-datasource-by-name! [name]
  (reset! ds (jdbc/get-datasource (ds-name name)))
  (create-schema))


(defn find-tweet-by-id [id]
  (jdbc/execute-one! @ds
    (sql/format
      {:select [:*]
       :from [:tweets]
       :where [:= :id id]})))

(defn find-tweets-without-polarity []
  (jdbc/execute! @ds
    (sql/format
      {:select [:id :text]
       :from [:tweets]
       :where [:= :negative nil]})))

#_(count (find-tweets-without-polarity))

(defn find-min-id [query]
  (:id
    (jdbc/execute-one! @ds
      (sql/format
        {:select [[:%min.id :id]]
         :from [:tweets]
         :where [:= :query query]}))))

(defn find-max-id [query]
  (:id
    (jdbc/execute-one! @ds
      (sql/format
        {:select [[:%max.id :id]]
         :from [:tweets]
         :where [:= :query query]}))))

(defn insert-tweet [id tweet query]
  (when-not (find-tweet-by-id id)
    (jdbc/execute! @ds
      (->
        (insert-into :tweets)
        (columns :id :created_at :text :query)
        (values [[id (java.util.Date.) tweet query]])
        sql/format))))

(defn update-polarity [id polar]
  (jdbc/execute! @ds
    (->
      (helpers/update :tweets)
      (sset polar)
      (where [:= :id id])
      sql/format)))

(comment
  (set-datasource-by-name! "#yolo")
  (create-schema)
  (find-tweet-by-id 11210359347055865856)
  (find-min-id)
  (insert-tweet -1 "foo4"))
