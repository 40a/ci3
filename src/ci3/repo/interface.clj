(ns ci3.repo.interface
  (:require [clojure.tools.logging :as log]
            [unifn.core :as u]))

(defmulti init (fn [_ repo] (keyword (or (:type repo) "github"))))
(defmethod init
  :default
  [_ repo]
  (log/info "No handler for repo " repo))

(defmulti webhook
  (fn [req]
    ;; some rule
    :bitbucket))

(comment
  (ns-unmap *ns* 'init))
