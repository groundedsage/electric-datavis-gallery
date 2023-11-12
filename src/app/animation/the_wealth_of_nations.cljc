(ns app.animation.the-wealth-of-nations
  (:require contrib.str
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric-ui4 :as ui]
            [app.lib :as lib]
            #?(:cljs ["d3-scale" :as d3-scale])
            #?(:cljs ["d3-shape" :as d3-shape])
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

#?(:clj (def shaped-data (ds/row-map (apply ds/concat (map ds/->dataset data))
                           (fn [row]
                             (let [income-yr (first (row "income"))
                                   [life-exp-period life-exp-yr] (row "lifeExpectancy")
                                   population (first (row "population"))
                                   yrs  (keep identity [income-yr life-exp-period population])]
                               {:max-yrs (apply max yrs)
                                :min-yrs (apply min yrs)
                                :life-expect life-exp-yr})))))

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

(e/defn Plot []
  (let [shaped-data (e/server (let [y-life-expectancy (vec (:life-expect shaped-data))
                                    x-income (map second (get shaped-data "income"))
                                    population (map second (get shaped-data "population"))]
                                (map vector x-income y-life-expectancy population)))]
    (svg/g (dom/props {:stroke "black"})
      
      (e/for-by identity [{:keys [x y population]} shaped-data]
        (svg/circle (dom/props {:cx x
                                :cy y
                                :r population}))))))

(e/defn Chart []
  (let [{:keys [width height]} chart]
    (dom/div
      (dom/p (dom/text "This is a recreation of a Gapminder visualization made famous by Hans Rosling. It shows per-capita income (x), life expectancy (y) and population (area) of 180 nations over the last 209 years, colored by region. Data prior to 1950 is sparse, so this chart uses bisection and linear interpolation to fill in missing data points."))
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

  
  
  )