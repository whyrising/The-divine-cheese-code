(ns the-divine-cheese-code.visualization.svg
  (:require [clojure.string :as s])
  (:refer-clojure :exclude [min max]))

(def coords-keys [:lat :lng])

(defn comparator-over-maps [comparison-fn keys]
  (fn [maps]
    (zipmap keys
            (map (fn [key] (apply comparison-fn (map key maps)))
                 keys))))

(def min (comparator-over-maps clojure.core/min coords-keys))

(def max (comparator-over-maps clojure.core/max coords-keys))

(defn translate-to-00 [locations]
  (let [min-coords (min locations)]
    (map #(merge-with - % min-coords)
         locations)))

(defn scale [width height locations]
  (let [max-coords (max locations)
        ratio {:lat (/ height (:lat max-coords))
               :lng (/ width (:lng max-coords))}]
    (map #(merge-with * % ratio) locations)))

(defn latlng->point
  "Convert lat/lng map to comma-separated string"
  [latlng]
  (str (:lng latlng) "," (:lat latlng)))

(defn points
  "Given a seq of lat/lng maps, return string of points joined by space"
  [locations]
  (s/join " " (map latlng->point locations)))

(defn line
  [points]
  (str "<polyline points=\"" points "\" />"))

(defn transform
  "Just chains other functions"
  [width height locations]
  (->> locations
       translate-to-00
       (scale width height)))

(defn xml
  "svg 'template', which also flips the coordinate system"
  [width height locations]
  (str "<svg height=\"" height "\" width=\"" width "\">"
       ;; These two <g> tags flip the coordinate system
       "<g transform=\"translate(0," height ")\">"
       "<g transform=\"scale(1,-1)\">"
       (-> (transform width height locations)
           points
           line)
       "</g></g>"
       "</svg>"))