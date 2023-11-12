(ns app.lib
  (:require contrib.str
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-svg :as svg]))

(e/defn AxisBottom [{:keys [chart x-scale]}]
  (let [{:keys [width height margin-bottom]} chart
        ticks (.ticks x-scale (clj->js (/ width 80)))
        tick-pos (map x-scale ticks)
        tick-pairs (map vector ticks tick-pos)]
    (svg/g (dom/props {:id "x-axis"
                       :transform (str "translate(0," (- 0 margin-bottom) ")")})
      (e/for-by identity [[label x] tick-pairs]
        (svg/line (dom/props {:x1 x :x2 x
                              :y1 0 :y2 height
                              :stroke "black"
                              :stroke-opacity 0.1
                              :stroke-width 1}))
        (svg/text (dom/props {:x x
                              :y height
                              :font-weight "bold"
                              :text-anchor "end"})
          (dom/text label))))))

(e/defn AxisLeft [{:keys [chart y-scale]}]
  (let [{:keys [width margin-left]} chart
        ticks (.ticks y-scale)
        tick-pos (map y-scale ticks)
        tick-pairs (map vector ticks tick-pos)]
    (svg/g (dom/props {:id "y-axis"
                       :transform (str "translate(" margin-left ",0)")})
      (e/for-by identity [[label y] tick-pairs]
        (svg/line (dom/props {:x1 0 :x2 width
                              :y1 y :y2 y
                              :stroke "black"
                              :stroke-opacity 0.1
                              :stroke-width 1}))
        (svg/text (dom/props {:x 0
                              :y y
                              :font-weight "bold"
                              :text-anchor "end"})
          (dom/text label))))))