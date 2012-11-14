{
 :test {
        :datomic-db "datomic:mem://zolodeck-test"
        
        :kiss-api "0574cc154095cc7ddcaa04480daa22903da7f1b7"
        
        :context-io-key "lq4k7hlv"
        :context-io-secret "8KDEzAPKKn0whYT9"
        
        :fb-app-id "411225185584626"
        :fb-app-secret "28392ef5b18c541c44f041dd723f0e6f"

        :new-user-freshness-millis (* 1000 10 2) ;; 2 minutes
        :new-user-wait-millis (* 1000 10) ;; 10 seconds

        :user-update-wait-fb-millis (* 1000 60 60) ;; 1 HOUR
        :stale-users-wait-fb-millis (* 1000 60) ;; 1 MINUTE
        
        :li-api-key "p8rw9l9pzvl8"
        :li-secret-key "1thkgbvKwrcFdZ7N"
        }     
  }
