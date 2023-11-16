(ns app.animation.connected-scatterplot
  (:require contrib.str
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric-ui4 :as ui]
            [app.lib :as lib]
            #?(:cljs ["d3-shape" :as d3-shape])
            #?(:clj [tech.v3.dataset :as ds])))

(def chart {:width 928
            :height 720
            :margin-top 20
            :margin-right 30
            :margin-bottom 30
            :margin-left 40})

#?(:clj (defonce data (ds/->dataset "resources/data/driving.csv")))

(defonce !vis-line-elem (atom nil))
(defonce !label-anim-elems (atom {}))

(e/def vis-line-elem (e/client (e/watch !vis-line-elem)))
(e/def label-anim-elems (e/client (e/watch !label-anim-elems)))

(e/def line-length (e/client (when-let [elem (:node vis-line-elem)]
                               (.getTotalLength elem))))

(e/def x-scale
  (let [{:keys [width margin-left margin-right]} chart]
    (lib/linear-scale {:domain (e/server (lib/col->min-max data "miles"))
                       :range [margin-left (- width margin-right)]})))

(e/def y-scale
  (let [{:keys [height margin-top margin-bottom]} chart]
    (lib/linear-scale {:domain (e/server (lib/col->min-max data "gas"))
                       :range [(- height margin-bottom) margin-top]})))

#?(:cljs (defn start-line-animation! []
           (let [{:keys [node keyframes options]} @!vis-line-elem
                 anim-object (.animate node keyframes options)]
             (swap! !vis-line-elem assoc :animation anim-object)
             anim-object)))

#?(:cljs (defn start-label-animations! []
           (let [{:keys [keyframes nodes animations]} @!label-anim-elems
                 _ (when animations (doall (map #(.cancel %) animations)))
                 create-options (fn [delay]
                                  (clj->js {:duration 125
                                            :fill "forwards"
                                            :delay delay}))
                 anim-objects (doall (map
                                       (fn [{:keys [node delay]}]
                                         (let [options (create-options delay)
                                               anim-object (.animate node keyframes options)]
                                           (.pause anim-object)
                                           anim-object))
                                       (sort-by :year nodes)))]
             (swap! !label-anim-elems assoc :animations anim-objects)
             (doall (map #(.play %) anim-objects)))))

#?(:cljs (defn start-animations! []
           (start-line-animation!)
           (start-label-animations!)))

#?(:cljs (def make-line (->
                          (d3-shape/line)
                          (.curve d3-shape/curveCatmullRom)
                          (.x (fn [[x _]] x))
                          (.y (fn [[_ y]] y)))))

(e/defn Plot [data]
  (svg/g (dom/props {:fill "white"
                     :stroke "black"
                     :stroke-width 2})
    (e/for-by identity [{:keys [x y]} data]
      (svg/circle (dom/props {:cx x
                              :cy y
                              :r 3})))))

(e/defn Labels [data]
  (swap! !label-anim-elems conj {:keyframes (clj->js [{:opacity 0}
                                                      {:opacity 1}])
                                 :nodes []})
  (when (< 0 line-length)
    (svg/g
      (dom/props {:font-family "sans-serif"
                  :font-size 10})
      (e/for-by identity [[i {:keys [x y] :as d}] (map-indexed vector data)]
        (let [line (make-line (map (fn [{:keys [x y]}]
                                     [x y])
                                (take (inc i) data)))
              path (svg/path (dom/props {:d line :fill "none"}) dom/node)
              path-length (.getTotalLength path)
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
                              :dx "-0.5em"})
              delay (* (+ 5000 125) (/ path-length line-length))
              year (get d "year")]
          (svg/text (dom/props (merge props
                                 {:x x
                                  :y y
                                  :fill "currentColor"
                                  :stroke "white"
                                  :paint-order "stroke"
                                  :opacity 0}))
            (dom/text year)
            (swap! !label-anim-elems update :nodes conj {:node dom/node
                                                         :delay delay
                                                         :year year})))))))

(e/defn Chart []
  (when (and (< 0 line-length)
          (seq (:nodes label-anim-elems)))
    (start-animations!))
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
      (when (and (< 0 line-length) vis-line-elem)
        (ui/button (e/fn [] (start-animations!))
          (dom/div (dom/text "Replay"))))
      (dom/div (dom/props {:id "chart"}))
      (svg/svg (dom/props {:width width
                           :height height
                           :viewBox (str "0 0 " width " " height)
                           :style {:max-width "100%"
                                   :height "auto"}})
        (lib/AxisBottom. {:chart chart
                          :x-scale x-scale
                          :label-dist "0.71em"})
        (lib/AxisLeft. {:chart chart
                        :y-scale y-scale
                        :label-dist -25
                        :pos 3})
        (let [elem (svg/path (dom/props {:id "vis-line"
                                         :fill "none"
                                         :stroke "black"
                                         :stroke-width 2.5
                                         :stroke-join "round"
                                         :stroke-linecap "round"
                                         :d line
                                         :stroke-dasharray line-length
                                         :stroke-dashoffset 0})
                     dom/node)]
          (when (< 0 line-length)
            (swap! !vis-line-elem conj {:keyframes (clj->js [{:strokeDashoffset line-length}
                                                             {:strokeDashoffset 0}])
                                        :options (clj->js {:duration 5000
                                                           :fill "forwards"})}))
          (reset! !vis-line-elem {:node elem}))
        (Plot. tdata)
        (Labels. tdata)))))