(ns app.todo-list
  (:require contrib.str
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-ui4 :as ui]
            [app.category-lists :as c-list]
            
            ;; Animation
            [app.animation.connected-scatterplot :as connected-scatterplot]
            [app.animation.the-wealth-of-nations :as the-wealth-of-nations]))

(defonce !current-page (atom :main))
(e/def current-page (e/client (e/watch !current-page)))

(e/defn Category [title items]
  (dom/div
    (dom/h2 (dom/text title))
    (dom/ul
      (e/for-by identity [i items]
        (dom/li
          (dom/a
            (when (c-list/has-page i)
              (dom/props {:style {:color "blue"}})
              (dom/on "click" (e/fn [_] (reset! !current-page i))))
            (dom/text (str i)))
          (when (and
                  (c-list/has-page i)
                  (not (c-list/completed i)))
            (dom/i (dom/text " - partially completed"))))))))

(e/defn RenderPage [page]
  (case page
    :main (dom/div 
            (Category. "Animation" c-list/animation)
            (Category. "Interactive" c-list/interaction)
            (Category. "Analysis" c-list/analysis)
            (Category. "Hierarchies" c-list/hierarchies)
            (Category. "Networks" c-list/networks)
            (Category. "Lines" c-list/lines)
            (Category. "Area" c-list/area)
            (Category. "Dots" c-list/dots)
            (Category. "Radial" c-list/radial)
            (Category. "Bars" c-list/annotation)
            (Category. "Maps" c-list/maps)
            (Category. "Essays" c-list/essays)
            (Category. "Just for fun" c-list/just-for-fun))
    :connected-scatterplot (connected-scatterplot/Chart.)
    :the-wealth-and-health-of-nations (the-wealth-of-nations/Chart.)))


(e/defn Todo-list []
  (e/client
    (dom/link (dom/props {:rel :stylesheet :href "/todo-list.css"}))
    (dom/h1 (dom/text "Electric Datavis Gallery"))
    (when-not (= current-page :main)
      (ui/button (e/fn [] (reset! !current-page :main))
        (dom/div
          (dom/text "Return to main page"))))
    (dom/p (dom/text "A gallery of data visualisations based on the ")
      (dom/a (dom/props {:href "https://observablehq.com/@d3/gallery?utm_source=d3js-org&utm_medium=hero&utm_campaign=try-observable"})
        (dom/text "D3 gallery")))
    (RenderPage. current-page)))

