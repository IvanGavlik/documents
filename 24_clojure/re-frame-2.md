from:
https://ericnormand.me/guide/re-frame-building-blocks

1. Re-frame - Shadow-cljs  - package.json - leningen/ deeps
- how this fits together - what is lifecycle 


2. How do you approach a situation when you have complex app db
- how do you keep of track which events cause change in the DB
- how do you keep of track which subs listen to some part of the DB

3. when to use Co-effects reg-cofx

4. Intreceptors


Notes: 
reg-event-fx - event 
An Event can have multiple Effects;
Each Effect is a key/value pair in the map the Event Handler returns


Every Event Handler of the longer form gets a Co-effects map as the first argument

(rf/reg-event-fx
  :buy
  [(rf/inject-cofx :now)] ;; add the co-effects we need here.
  (fn [cofx [_ item-id]]
    {:http-xhrio {:uri (str "http://url.com/product/" item-id "/purchase")
                  :params {:time (:now cofx)} ;; use the time
                  :method :post
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:added-cart]
                  :on-failure [:notified-error]}
     :db (update-in (:db cofx) [:cart :items] conj {:item item-id})})) 


re-frame.core/reg-fx - efects
Re-frame comes with a few built-in Effects.


coefects 
(rf/reg-cofx
  :now
  (fn [cofx _data] ;; _data unused
    (assoc cofx :now (js/Date.))))


Interceptors are a funny name. But they're just a fancy way of creating a data pipeline. 
