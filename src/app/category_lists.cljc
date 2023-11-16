(ns app.category-lists)

(def has-page #{:connected-scatterplot :the-wealth-and-health-of-nations :pannable-chart})
(def completed #{:connected-scatterplot})

(def animation [:animated-treemap
                :temporal-force-directed-graph
                :connected-scatterplot
                :the-wealth-and-health-of-nations
                :scatterplot-tour
                :bar-chart-race
                :stacked-to-group-bars
                :steamgraph-transitions
                :smooth-zooming
                :zoom-to-bounding-box
                :orthographic-to-equirectangular
                :world-tour
                :walmarts-growth
                :heirarchical-bar-chart
                :zoomable-treemap
                :zoomable-circle-packing
                :collapsible-tree
                :zoomable-icicle
                :zoomable-sunburst
                :sortable-bar-chart
                :icelanding-population-by-age])

(def interaction [:versor-dragging
                  :index-chart
                  :sequences-sunburst
                  :brushable-scatterplot
                  :brushable-scatterplot-matrix
                  :pannable-chart
                  :zoomable-area-chart
                  :zoomable-bar-chart
                  :seamless-zoomable-map-tiles])

(def analysis [:moving-average
               :bollinger-bands
               :box-plot
               :histogram
               :kernal-density-estimation
               :density-contours
               :volcano-contours
               :contours
               :hexbin
               :hexbin-area
               :hexbin-map
               :Q-Q-plot
               :normal-quantile-plot
               :parallel-sets])

(def hierarchies [:treemap
                  :cascaded-treemap
                  :nested-treemap
                  :circle-packing
                  :indented-tree
                  :tidy-tree
                  :radial-tidy-tree
                  :cluster-dendrogram
                  :radial-dendrogram
                  :sunburst
                  :icicle
                  :tangled-tree-visualisation
                  :phylogenetic-tree
                  :force-directed-tree])

(def networks [:force-directed-graph
               :disjoint-force-directed-graph
               :mobile-patent-suits
               :arc-diagram
               :sankey-diagram
               :heirarchical-edge-bundling
               :heriarchical-edge-bundling-2
               :chord-diagram
               :chord-diagram-2
               :directed-chord-diagram
               :chord-dependency-diagram])

(def bars [:bar-chart
           :horizontal-bar-chart
           :diverging-bar-chart
           :stacked-bar-chart
           :stacked-horizontal-bar-chart
           :stacked-bar-chart-normalized
           :world-history-timeline
           :calendar
           :the-impact-of-vaccines
           :electricity-usage-2019
           :revenue-by-music-format-1973-2018])

(def lines [:line-chart
            :line-with-missing-data
            :multi-line-chart
            :change-line-chart
            :slope-chart
            :slope-chart-2
            :mareys-trains
            :candlestick-chart
            :variable-color-line
            :graident-encoding
            :threshold-encoding
            :parallel-coordinates
            :inedquality-in-america-cities
            :new-zealand-tourists-1921-2018
            :seas-ice-extend-1978-2017])

(def area [:area-chart 
           :area-with-missing-data 
           :stacked-area-chart
           :normalized-stacked-area-chart 
           :US-population-by-state-1970-1990
           :streamgraph
           :difference-chart
           :band-chart
           :ridgeline-plot
           :horizon-chart 
           :realtime-horizon-chart])

(def dots [:scatterplot
           :scatterplot-with-shapes
           :brushable-scatterplot-matrix
           :dot-plot
           :global-temperature-trends
           :bubble-map
           :spike-map
           :bubble-chart
           :beeswarm
           :mirrored-beeswarm
           :Hertzsprung-Russle-diagram])

(def radial [:pie-chart 
             :donut-chart
             :radial-area-chart
             :radial-stacked-bar-chart
             :radial-stacked-bar-chart-sorted])

(def annotation [:inline-labels 
                 :directly-labelling-lines
                 :line-chart-with-tooltip
                 :voronoi-labels 
                 :occlusion
                 :graticule-labels
                 :styles-axes 
                 :color-legend])

(def maps [:choropleth
           :bivariate-choropleth
           :state-choropleth
           :world-choropleth
           :world-map 
           :projection-comparison
           :tissot's-indicatrix
           :web-mercator-tiles
           :raster-tiles
           :vector-tiles
           :clipped-map-tiles
           :raster-and-vector 
           :vector-field
           :geoTIFF-contours
           :US-airports-voronoi
           :world-airpots-voronoi
           :solar-terminator 
           :solar-path
           :start-map 
           :non-contiguous-cartogram])

(def essays [:d3-packEnclose
             :centreline-labelling
             :methods-of-comparison-compared
             :predator-and-prey])

(def just-for-fun [:polar-clock 
                   :stern-brocot-tree
                   :voronoi-stippling 
                   :watercolor
                   :PSR-B1919+21
                   :epicyclic-gearing
                   :owls-to-the-max
                   :tadpoles
                   :word-cloud
                   :spilhaus-shoreline-map
                   :phases-of-the-moon
                   :color-schemes])