(ns conjure.nvim
  "Tools to interact with Neovim at a higher level than RPC."
  (:require [taoensso.timbre :as log]
            [conjure.rpc :as rpc]))

(defn call
  "Simply a thin nvim specific wrapper around rpc/request."
  [req]
  (let [{:keys [error result] :as resp} (rpc/request req)]
    (when error
      (log/error "Error while making nvim call" req "->" resp))
    result))

(defn- ->atomic-call
  "Transform a regular call into an atomic call param."
  [{:keys [method params]}]
  [(rpc/kw->method method) (vec params)])

(defn call-batch
  "Perform multiple calls together atomically."
  [& reqs]
  (let [[results [err-idx err-type err-msg]]
        (call {:method :nvim-call-atomic
               :params [(map ->atomic-call reqs)]})]
    (when err-idx
      (log/error "Error while making atomic batch call"
                 (get reqs err-idx) "->" err-type err-msg))
    results))

;; These functions return the data that you can pass to call or call-batch.

(defn get-current-buf []
  {:method :nvim-get-current-buf})

(defn get-current-win []
  {:method :nvim-get-current-win})

(defn win-get-cursor [win]
  {:method :nvim-win-get-cursor
   :params [win]})

(defn win-set-cursor [win pos]
  {:method :nvim-win-set-cursor
   :params [win pos]})