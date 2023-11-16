(ns app.lib
  (:require contrib.str
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-svg :as svg]
            #?(:cljs ["d3-scale" :as d3-scale])
            #?(:clj [tech.v3.datatype.statistics :as stats])))

#?(:clj (defn col->min-max [data colname]
          (let [c (get data colname)]
            [(stats/min c)
             (stats/max c)])))

(e/defn AxisBottom [{:keys [chart x-scale label-dist]}]
  (let [{:keys [width height margin-bottom]} chart
        ticks (.ticks x-scale (clj->js (/ width 80)))
        tick-pos (map x-scale ticks)
        tick-pairs (map vector ticks tick-pos)]
    (svg/g (dom/props {:id "x-axis"
                       :transform (str "translate(0," (- 0 margin-bottom) ")")})
      (e/for-by identity [[label x] tick-pairs]
        (svg/g
          (svg/line (dom/props {:x1 x :x2 x
                                :y1 0 :y2 height
                                :stroke "black"
                                :stroke-opacity 0.1
                                :stroke-width 1}))
          (svg/line (dom/props {:x1 x :x2 x
                                :y1 height :y2 (+ height 6)
                                :stroke "black"
                                :stroke-opacity 1
                                :stroke-width 1}))
          (svg/text (dom/props {:x x
                                :y (+ height 9)
                                :font-size 10
                                :text-anchor "middle"
                                :dy label-dist})
            (dom/text label)))))))

(e/defn AxisLeft [{:keys [chart y-scale label-dist pos]}]
  (let [{:keys [width margin-left]} chart
        ticks (.ticks y-scale)
        tick-pos (map y-scale ticks)
        tick-pairs (map vector ticks tick-pos)]
    (svg/g (dom/props {:id "y-axis"
                       :transform (str "translate(" margin-left ",0)")})
      (e/for-by identity [[label y] tick-pairs]
        (svg/g
          (svg/line (dom/props {:x1 0 :x2 width
                                :y1 y :y2 y
                                :stroke "black"
                                :stroke-opacity 0.1
                                :stroke-width 1}))
          (svg/line (dom/props {:x1 -6 :x2 0
                                :y1 y :y2 y
                                :stroke "black"
                                :stroke-opacity 1
                                :stroke-width 1}))
          (svg/text (dom/props {:x label-dist
                                :y (+ y pos)
                                :text-anchor "start"
                                :font-size 10})
            (dom/text label)))))))

#?(:cljs (defn linear-scale [{:keys [domain range]}]
           (-> (d3-scale/scaleLinear)
             (.domain (clj->js domain))
             (.nice)
             (.range (clj->js range)))))