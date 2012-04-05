(ns zolo.scenario
  (:use  [clojure.test :only [is are]]
         zolo.test.web-utils
         zolo.scenarios.user
         clj-facebook-graph.auth))

(defn scenario []
  {})

(defmacro with-stubs [scenario & {:as bindings}]
  `(update-in ~scenario [:stubs]
     (fn [mocks#]
       (merge
	 mocks#
	 (zipmap
	   (list ~@(map #(if-let [v (resolve %)] v (throw (Exception. (str "Can't resolve " %)))) (keys bindings)))
	   (list ~@(map (fn [v] `(constantly ~v)) (vals bindings))))))))


(defmacro with-scenario [scenario & body]
  `(let [scenario# ~scenario]
     (do
       (with-bindings (or (:stubs scenario#) {})
         (let [response# (do ~@body)]
           (-> scenario#
               (assoc :response response#)))))))

(defn request-successful? [scenario]
  (is (= 200 (-> scenario :response :status)))
  scenario)

(defn login-as-valid-facebook-user 
  ([scenario]
     (login-as-valid-facebook-user scenario default-user))
  ([scenario user]
      (merge scenario
             {:current-user user})))

(defn post-new-user 
  ([scenario]
     (post-new-user scenario (:current-user scenario)))
  ([scenario user]
     (-> scenario
         (with-stubs decode-signed-request true)
         (with-scenario 
           (web-request :post (new-user-url) user)))))



