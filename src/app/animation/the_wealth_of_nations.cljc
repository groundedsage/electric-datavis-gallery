(ns app.animation.the-wealth-of-nations
  (:require contrib.str
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric-ui4 :as ui]
            [app.lib :as lib]
            #?(:cljs ["d3-scale" :as d3-scale])
            #?(:cljs ["d3-shape" :as d3-shape])
            #?(:cljs ["d3-scale-chromatic" :as d3-chromatic])
            #?(:clj [tech.v3.dataset :as ds])
            #?(:clj [tech.v3.datatype.statistics :as stats])
            #?(:clj [charred.api :as charred])
            #?(:clj [clojure.data.json :as json])
            #?(:clj [clojure.java.io :as io])))

(def chart {:width 1000
            :height 560
            :margin-top 20
            :margin-right 20
            :margin-bottom 35
            :margin-left 40})

#?(:clj (defonce data (charred/read-json (io/resource "data/nations.json"))))

#?(:clj (def shaped-data (-> 
                           (apply ds/concat (map ds/->dataset data))
                           (ds/row-map 
                               (fn [row]
                                 (let [income-yr (first (row "income"))
                                       [life-exp-period life-exp-yr] (row "lifeExpectancy")
                                       population (first (row "population"))
                                       yrs  (keep identity [income-yr life-exp-period population])]
                                   {:max-yrs (apply max yrs)
                                    :min-yrs (apply min yrs)
                                    :life-expect life-exp-yr}))))))

(e/def regions (e/server (set (get (ds/unique-by-column shaped-data "region") "region"))))

(e/def y-scale
  (let [{:keys [height margin-top margin-bottom]} chart
        domain-data (e/server (lib/col->min-max shaped-data :life-expect))]
    (-> (d3-scale/scaleLinear)
      (.domain (clj->js domain-data))
      (.nice)
      (.range (clj->js [(- height margin-bottom) margin-top])))))

(e/def x-scale
  (let [{:keys [width margin-left margin-right]} chart]
    (-> (d3-scale/scaleLog)
      (.domain (clj->js [200, 1e5]))
      (.nice)
      (.range (clj->js [margin-left (- width margin-right)])))))

(e/def r-scale
  (d3-scale/scaleSqrt 
    (clj->js [0 5e8])
    (clj->js [0 (/ (:width chart) 24)])))

#?(:cljs (def color-scale
           (d3-scale/scaleOrdinal (clj->js (vec regions)) d3-chromatic/schemeCategory10)))

(defonce !year (atom 1800))
(e/def year (e/watch !year))

#?(:clj (e/def plot-columns (-> (ds/concat
                                (ds/filter-column shaped-data "income" (fn [[y v]] (= y year)))
                                (ds/filter-column shaped-data "lifeExpectancy" (fn [[y v]] (= y year)))
                                (ds/filter-column shaped-data "population" (fn [[y v]] (= y year))))
                            (ds/select-columns ["income" "lifeExpectancy" "population" "region"]))))

(e/defn Plot []
    (let [row-range (e/server (range (ds/row-count plot-columns)))]
      (svg/g (dom/props {:stroke "black"})
        (e/for-by identity [i  row-range] 
          (let [row (e/server (first (vec (ds/rows (ds/select-rows plot-columns [i])))))
                x (x-scale  (second (get row "income")))
                y (y-scale (second (get row "lifeExpectancy")))
                color (color-scale (get row "region"))
                r (r-scale (second (get row "population")))]
            (println "the population" population)
            (svg/circle (dom/props {:cx x
                                    :cy y
                                    :r r
                                    :fill color})))))))

(e/defn Chart []
  (let [{:keys [width height]} chart]
    (dom/div
      (dom/p (dom/text "This is a recreation of a Gapminder visualization made famous by Hans Rosling. It shows per-capita income (x), life expectancy (y) and population (area) of 180 nations over the last 209 years, colored by region. Data prior to 1950 is sparse, so this chart uses bisection and linear interpolation to fill in missing data points."))
      (dom/p (dom/text year))
      (dom/ul
        (e/for-by identity [region regions]
          (dom/li 
            (dom/props {:style {:color (color-scale region)}})
            (dom/text region))))
      (dom/div (dom/props {:id "chart"}))
      (svg/svg (dom/props {:width width
                           :height height
                           :viewBox (str "0 0 " width " " height)
                           :style {:border "solid black 1px"
                                   :max-width "100%"
                                   :height "auto"}})
        (lib/AxisBottom. {:chart chart
                          :x-scale x-scale})
        (lib/AxisLeft. {:chart chart
                        :y-scale y-scale})
        ;; (println "colors: " color-scale)
        ;; (println (color-scale "UK"))
        ;; (println "(d3-chromatic/schemeCategory10):"   d3-chromatic/schemeCategory10)
        (Plot.)))))


(comment
  #?(:clj (def data (charred/read-json (io/resource "data/nations.json"))))

  (stats/min (:min-yrs shaped-data))
  (stats/max (:max-yrs shaped-data))

  (defn col->min-max [data colname]
    (let [c (get data colname)]
      [(stats/min c)
       (stats/max c)]))

  (lib/col->min-max shaped-data :life-expect)

  (stats/min (:life-expect shaped-data))
  (stats/max (:life-expect shaped-data))

  (ds/row-count shaped-data)

  (last (range (ds/row-count shaped-data)))

  (ds/select-rows shaped-data [18003])

  (map #(dissoc % :max-yrs :min-yrs) (ds/rows (ds/select-rows shaped-data [0 1 3 5 17 42])))

  (keys (first (ds/rows (ds/select-rows shaped-data [0 1 3 5 17 42]))))

  (->
    (ds/select-columns shaped-data ["income" :life-expect "population" "region"])
    (ds/select-rows))


  (set (get (ds/unique-by-column shaped-data "region") "region"))

  (->
    (ds/concat
      (ds/filter-column shaped-data "income" (fn [[y v]] (= y 1800)))
      (ds/filter-column shaped-data "lifeExpectancy" (fn [[y v]] (= y 1800)))
      (ds/filter-column shaped-data "population" (fn [[y v]] (= y 1800))))
    (ds/row-count))

  )