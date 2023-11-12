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
            #?(:clj [tech.v3.datatype.statistics :as stats])))

(def chart {:width 533
            :height 560
            :margin-top 20
            :margin-right 20
            :margin-bottom 35
            :margin-left 40})

#?(:clj (def data (ds/->dataset "resources/data/nations.json")))

(slurp "resources/data/nations.json")