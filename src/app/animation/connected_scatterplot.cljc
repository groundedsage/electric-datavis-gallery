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

(def chart {:width 928
            :height 720
            :margin-top 20
            :margin-right 30
            :margin-bottom 30
            :margin-left 40})

#?(:clj (def data (ds/->dataset "resources/data/driving.csv")))

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

(e/defn Labels [data]
  (svg/g 
    (dom/props {:font-family "sans-serif"
                :font-size 10})
    (e/for-by identity [{:keys [x y] :as d} data]
      (let [side (get d "side")
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
                                :fill "currenColor"
                                :stroke "white"
                                :paint-order "stroke"
                                ;; :fill-opacity 0
                                }))
          (dom/text (get d "year"))
          (svg/animate (dom/props {:class "label-animate"
                                            ;;  :begin "1s"
                                   :dur "1s"
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
        make-line (->
                    (d3-shape/line)
                    (.curve d3-shape/curveCatmullRom)
                    (.x (fn [[x _]] x))
                    (.y (fn [[_ y]] y)))
        line (make-line (map (fn [{:keys [x y]}]
                               [x y]) 
                          tdata))
        !line-length (atom nil)
        line-length (e/watch !line-length)]
    (dom/div 
      (dom/p (dom/text "This is a recreation of Hannah Fairfield's Driving Shifts Into Reverse, sans annotations. See also Fairfield's Driving Safety, in Fits and Starts, Noah Veltman's variation of this graphic, and a paper on connected scatterplots by Haroz et al."))
      (ui/button (e/fn []
                   (let [anim-path (.getElementById js/document "path-animate")
                         anim-labels (.getElementsByClassName js/document "label-animate")]
                     (.beginElement anim-path)
                     (println anim-labels)
                     #_(map #(.beginElement %) anim-labels)))
        (dom/div
          (dom/text "Replay")))

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
        (svg/path (dom/props {:id "vis-line"
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
                                   :fill "freeze"})))
        
        (js/setTimeout #(let [element (.getElementById js/document "vis-line")
                              length (.getTotalLength element)
                              anim-path (.getElementById js/document "path-animate")
                              anim-labels (js->clj (.getElementsByClassName js/document "label-animate"))]
                          (reset! !line-length length)
                          (.beginElement anim-path)
                          (map (fn [] (.beginElement %)) anim-labels))
          1000)
        
        (Plot. tdata)
        (Labels. tdata)))))