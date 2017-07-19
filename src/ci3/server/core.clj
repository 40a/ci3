(ns ci3.server.core
  (:require
   [ci3.server.watch :as watch]
   [ci3.build.core :as build]
   [unifn.rest :as rest]
   [ci3.server.watch]
   [ci3.repo.core :as repo]
   [unifn.formats]
   [unifn.core :as u]
   [unifn.env :as e]
   [ci3.k8s :as k8s]))

(defmethod u/*fn ::routes
  [{{routes :routes} :cache}]
  {:response {:body (var-get routes)}})

(defmethod u/*fn ::watches [_]
  {:response {:status 200
              :body (with-out-str (clojure.pprint/pprint  @watch/state))}})

(defmethod u/*fn
  ::configure
  [arg]
  {:k8s k8s/cfg})

(defmethod u/*fn
  ::webhook-verify
  [{req :request}]
  {:response {:status 200 :body "Ok! Wait for hook"}})

(def routes
  {:GET ::routes
   "repositories" {:GET ::repositories}
   "watches"      {:GET ::watches}
   "webhook" { [:id] {:POST ::repo/webhook
                      :GET  ::webhook-verify}}
   "builds" {:GET ::builds
             [:id] {:GET ::logs} }})


(def metadata
  {:cache {:routes #'routes}
   :watch {:timeout 5000
           :resources [{:handler ::repo/init
                        :apiVersion "ci3.io/v1"
                        :resource :repositories
                        :ns "default"}
                       {:handler ::build/build
                        :apiVersion "ci3.io/v1"
                        :resource :builds
                        :ns "default"}]}
   :web [:unifn.routing/dispatch
         :unifn.formats/response]
   :bootstrap [::e/env ::configure]
   :config {:web {:port 8888}}})


(defn exec [& args]
  (rest/restart metadata)
  (u/*apply [::e/env :k8s/watch] metadata))

(comment
  (rest/restart metadata))
