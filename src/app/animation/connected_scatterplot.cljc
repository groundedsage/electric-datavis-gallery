(ns app.animation.connected-scatterplot
  (:require contrib.str
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric-ui4 :as ui]
            [app.lib :as lib]
            #?(:cljs ["d3-scale" :as d3-scale])
            #?(:cljs ["d3-shape" :as d3-shape])
            #?(:clj [tech.v3.dataset :as ds])
            #?(:clj [tech.v3.datatype.statistics :as stats])))

;; TODO:
;; - animate labels
;; - start animation on load instead of timeout

(def chart {:width 928
            :height 720
            :margin-top 20
            :margin-right 30
            :margin-bottom 30
            :margin-left 40})

#?(:clj (defonce data (ds/->dataset "resources/data/driving.csv")))

(defonce !vis-line-elem (atom nil))
(defonce !path-animate-elem (atom nil))
(def !loaded? (atom false))

(e/def vis-line-elem (e/client (e/watch !vis-line-elem)))
(e/def path-animate-elem (e/client (e/watch !path-animate-elem)))
(e/def loaded? (e/client (e/watch !loaded?)))

(e/def line-length (e/client (when vis-line-elem (.getTotalLength vis-line-elem))))

#?(:clj (defn col->min-max [data colname]
          (let [c (get data colname)]
            [(stats/min c)
             (stats/max c)])))

(e/def x-scale
  (let [{:keys [width margin-left margin-right]} chart
        domain-data (e/server (col->min-max data "miles"))]
    (-> (d3-scale/scaleLinear) 
      (.domain (clj->js domain-data))
      (.nice)
      (.range (clj->js [margin-left (- width margin-right)])))))

(e/def y-scale
  (let [{:keys [height margin-top margin-bottom]} chart
        domain-data (e/server (col->min-max data "gas"))]
    (-> (d3-scale/scaleLinear)
      (.domain (clj->js domain-data))
      (.nice)
      (.range (clj->js [(- height margin-bottom) margin-top])))))

(e/defn Plot [data]
  (svg/g (dom/props {:fill "white"
                     :stroke "black"
                     :stroke-width 2})
    (e/for-by identity [{:keys [x y]} data]
      (svg/circle (dom/props {:cx x
                              :cy y
                              :r 3})))))
#?(:cljs (def make-line (->
                          (d3-shape/line)
                          (.curve d3-shape/curveCatmullRom)
                          (.x (fn [[x _]] x))
                          (.y (fn [[_ y]] y)))))

(e/defn Labels [data]
  (svg/g 
    (dom/props {:font-family "sans-serif"
                :font-size 10})
    (e/for-by identity [[i {:keys [x y] :as d}] (map-indexed vector data)] 
      (let [line (make-line (map (fn [{:keys [x y]}]
                                   [x y])
                              (take (inc i) data)))
            path (svg/path (dom/props {:d line :fill "none"}) dom/node)
            path-length (.getTotalLength path)
            _ (println "result: "i "- " path-length)
            side (get d "side")
            props (case side
                    "top" {:text-anchor "middle"
                           :dy "-0.7em"}
                    "right" {:text-anchor "start"
                             :dx "0.5em"}
                    "bottom" {:text-anchor "middle"
                              :dy "1.4em"}
                    "left" {:text-anchor "end"
                            :dy "0.32"
                            :dx "-0.5em"})]
        (svg/text (dom/props (merge props
                               {:x x
                                :y y
                                :fill "currentColor"
                                :stroke "white"
                                :paint-order "stroke"
                                :fill-opacity 0.3}))
          (dom/text (get d "year"))
          (svg/animate (dom/props {:class "label-animate"
                                   :begin (str (* i 100) "ms")
                                   :dur "200ms"
                                   :fill-opacity 1
                                   :fill "freeze"})))))))

(e/defn Chart []
  (let [{:keys [width height]} chart
        data (e/server (seq (ds/rows data)))
        tdata (map (fn [col]
                     (-> col
                       (assoc :x (x-scale (get col "miles")))
                       (assoc :y (y-scale (get col "gas")))))
                data)
        line (make-line (map (fn [{:keys [x y]}]
                               [x y])
                          tdata))]
    (dom/div
      (dom/p (dom/text "This is a recreation of Hannah Fairfield's Driving Shifts Into Reverse, sans annotations. See also Fairfield's Driving Safety, in Fits and Starts, Noah Veltman's variation of this graphic, and a paper on connected scatterplots by Haroz et al."))
      (ui/button (e/fn [] (.beginElement @!path-animate-elem))
        (dom/div (dom/text "Replay")))
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
        (let [elem (svg/path (dom/props {:id "vis-line"
                                         :fill "none"
                                         :stroke (when line-length "black")
                                         :stroke-width 2.5
                                         :stroke-join "round"
                                         :stroke-linecap "round"
                                         :d line
                                         :stroke-dasharray line-length
                                         :stroke-dashoffset line-length})
                     (svg/animate (dom/props {:id "path-animate"
                                              :attributeName "stroke-dashoffset"
                                              :from line-length
                                              :to 0
                                              :dur "5s"
                                              :fill "freeze"})
                       (reset! !path-animate-elem dom/node))
                     dom/node)]
          (reset! !vis-line-elem elem))
        (Plot. tdata)
        (Labels. tdata)))))