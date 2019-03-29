(ns cljplot.sketches.lattice
  (:require [cljplot.render :as r]
            [cljplot.build :as b]
            [cljplot.common :refer :all]
            [fastmath.interpolation :as in]
            [fastmath.stats :as stats]
            [clojure2d.color :as c]
            [clojure2d.core :as c2d]
            [cljplot.scale :as s]
            [fastmath.core :as m]
            [fastmath.random :as rnd]
            [cljplot.core :refer :all]
            [java-time :as dt]
            [clojure.string :as str]
            [clojure2d.core :as c2d]
            [java-time :as d]
            [fastmath.vector :as v]))

;; data

(defn data->timeseries
  [vs start end]
  (let [c (dec (count vs))]
    (map-indexed #(assoc %2 :time-id (m/norm %1 0.0 c start end)) vs)))

(do

  (def chem97 (read-json "data/chem97.json"))
  (def score-gcsescore (->> chem97
                            (group-by :score)
                            (map-kv #(map :gcsescore %))
                            (into (sorted-map))))

  (def oats (read-json "data/oats.json"))
  (def barley (read-json "data/barley.json"))
  (def titanic (read-json "data/titanic.json"))
  (def faithful (read-json "data/faithful.json"))
  (def gvhd10 (read-json "data/gvhd10.json"))
  (def chem97 (read-json "data/chem97.json"))
  (def quakes (read-json "data/quakes.json"))

  ;; time goes from 1 to 61
  (def ssd (data->timeseries (read-json "data/ssd.json") 1 61))

  ;; time goes from 1700 1988
  (def sunspot-year (data->timeseries (read-json "data/sunspot_year.json") 1700 1988))

  ;; fsc-h from gvhd
  (def gvhd-fsc-h (for [patient [5 6 7 9 10]
                        visit [1 2 3 4 5 6 7]
                        :let [nm (str "data/GvHD-FSC-H/s" patient "a0" visit)]]
                    {:patient patient
                     :visit visit
                     :data (map first (read-json nm))}))

  (def hnanes (read-json "data/nhanes.json"))
  
  (def blue (c/set-alpha (last (:rdylbu-9 c/palette-presets)) 200))
  (def lblue (c/set-alpha (nth (:rdylbu-9 c/palette-presets) 7) 200)))


;;;;;;;;;;;;;;;;;;;;;;
;; lattice
;; http://lmdvr.r-forge.r-project.org/figures/figures.html

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chapter 1

;; figure 1.1

(-> (b/lattice :histogram score-gcsescore {:bins 10 :padding-out 0.0} {:grid true :label (partial str "Score: ")})
    (b/preprocess-series)
    (b/update-scales :x :ticks 5)
    (b/add-axes :bottom)
    (b/add-axes :left)
    ;; (b/add-axes :right)
    ;; (b/add-axes :top)
    (r/render-lattice {:width 800 :height 600})
    (save "results/lattice/figure_1.1.jpg")
    (show))

;; figure 1.2

(-> (b/lattice :density score-gcsescore {} {:grid true :label (partial str "Score: ")})
    (b/preprocess-series)
    (b/update-scales :x :ticks 5)
    (b/update-scales :y :ticks 5)
    (b/add-axes :bottom)
    (b/add-axes :left)
    (r/render-lattice {:width 800 :height 600})
    (save "results/lattice/figure_1.2.jpg")
    (show))

;; figure 1.3

(-> (b/series [:grid])
    (b/add-multi :density score-gcsescore {:stroke {:size 2}} {:color (c/palette-presets :category10)
                                                               :stroke (map #(hash-map :size 2 :dash %) line-dash-styles)})
    (b/preprocess-series)
    (b/add-axes :bottom)
    (b/add-axes :left)
    (r/render-lattice {:width 800 :height 400})
    (save "results/lattice/figure_1.3.jpg")
    (show))


;; figure 1.4 same as above

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chapter 2

;; figure 2.1

(let [data (->> oats
              (group-by (juxt :Block :Variety))
              (map-kv (partial map (juxt :nitro :yield)))
              (sort-by first))]
  (-> (b/lattice :line data {:point {:type \o}} {:grid true :label str :shape [nil 3]})
     (b/preprocess-series)
     (b/tie-domains :y)
     (b/update-scales :y :fmt int)
     (b/update-scales :x :ticks 5)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 600 :height 1000 :padding-in 0.1 :padding-out 0.05})
     (save "results/lattice/figure_2.1.jpg")
     (show)))

;; figure 2.2

(let [data (->> oats
                (filter #(= (:Block %) "I"))
                (group-by (juxt :Block :Variety))
                (map-kv (partial map (juxt :nitro :yield)))
                (sort-by first))]
  (-> (b/lattice :line data {:point {:type \o}} {:grid true :label str :shape [nil 3]})
      (b/preprocess-series)
      (b/update-scales :y :fmt int)
      (b/update-scales :x :ticks 5)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:width 600 :height 300})
      (save "results/lattice/figure_2.2.jpg")
      (show)))

;; figure 2.3

(let [data (->> oats
              (group-by (juxt :Block :Variety))
              (map-kv (partial map (juxt :nitro :yield)))
              (sort-by first))]
  (-> (b/lattice :line data {:point {:type \o}} {:grid true :label (partial str/join \space) :shape [nil 3]})
     (b/preprocess-series)
     (b/tie-domains :y)
     (b/update-scales :y :fmt int)
     (b/update-scales :x :ticks 2)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 200 :height 1000 :padding-in 0.1})
     (save "results/lattice/figure_2.3.jpg")
     (show)))


;; figure 2.4

(let [data (->> oats
              (group-by (juxt :Block :Variety))
              (map-kv (partial map (juxt :nitro :yield)))
              (sort-by first))]
  (-> (b/lattice :line data {:point {:type \o}} {:grid true :label (partial str/join \space) :shape [nil 6]})
     (b/preprocess-series)
     (b/tie-domains :y)
     (b/update-scales :y :fmt int)
     (b/update-scales :x :ticks 3)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 600 :height 800 :padding-in 0.1 :padding-out 0.05})
     (save "results/lattice/figure_2.4.jpg")
     (show)))

;; figure 2.5 - no grouping

;; figure 2.6

(let [data (->> barley
              (group-by :year) ;; group by year
              (map-kv (fn [year]
                        (let [sites (group-by :site year)] ;; for each year group by site (it will be block)
                          (map-kv (fn [site]
                                    (map-kv (partial map :yield) (group-by :variety site))) sites))))) ;; group by variety and list yield
      
      get-year (fn [year conf] (map-kv #(vector :strip % conf) (get data year)))] ;; manually select year
  (-> (b/lattice :stack-horizontal (get-year 1931 {:shape \x :size 6 :color blue}) {} {:shape [6 1] :grid true}) ;; first layer, with grid
     (b/add-series (b/lattice :stack-horizontal (get-year 1932 {:shape \o :size 6 :color blue}) {} {:shape [6 1] :label str})) ;; last layer with label
     (b/preprocess-series)
     (b/update-scales :x :domain [10 70])
     (b/update-scales :x :ticks 5)
     (b/update-scales :x :fmt int)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 300 :height 800 :padding-in 0.1})
     (save "results/lattice/figure_2.6.jpg")
     (show)))

;; figure 2.7

(let [data (->> oats
              (group-by :Variety)
              (map-kv (fn [variety]
                        (let [blocks (group-by :Block variety)]
                          (map-kv (fn [block] (map (juxt :nitro :yield) block)) blocks)))))
      grids-labels (b/lattice :grid (second (first data)) {} {:label str :shape [1 6]})]
  (-> grids-labels
     (b/add-series (mapcat (fn [[k shape]] (b/lattice :line (data k) {:point {:type shape}} {:shape [1 6]})) (map vector (keys data) [\o \+ \v])))
     (b/preprocess-series)
     (b/update-scales :y :domain [50 180])
     (b/update-scales :x :ticks 4)
     (b/add-axes :bottom)
     (b/add-axes :left) 
     (r/render-lattice {:width 800 :height 300 :padding-in 0.1})
     (save "results/lattice/figure_2.7.jpg")
     (show)))

;; figure 2.8

(let [data (->> titanic
              (group-by (juxt :Sex :Age))
              (map-kv (fn [v] [:sbar (map-kv (fn [c] (map :Freq (sort-by :Survived c))) (group-by :Class v))])))]
  (-> (b/lattice :stack-horizontal data {} {:label str :shape [1 4]})
     (b/preprocess-series)
     (b/tie-domains :x)
     (b/update-scales :x :fmt int)
     (b/update-scales :x :ticks 5)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 800 :height 300})
     (save "results/lattice/figure_2.8.jpg")
     (show)))

;; figure 2.9

(let [data (->> titanic
              (group-by (juxt :Sex :Age))
              (map-kv (fn [v] [:sbar (map-kv (fn [c] (map :Freq (sort-by :Survived c))) (group-by :Class v))])))]
  (-> (b/lattice :stack-horizontal data {} {:label str :shape [1 4]})
     (b/preprocess-series)
     (b/update-scales :x :fmt int)
     (b/update-scales :x :ticks 5)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 800 :height 300})
     (save "results/lattice/figure_2.9.jpg")
     (show)))

;; figure 2.10

(let [data (->> titanic
              (group-by (juxt :Sex :Age))
              (map-kv (fn [v] [:sbar (map-kv (fn [c] (map :Freq (sort-by :Survived c))) (group-by :Class v))])))]
  (-> (b/lattice :stack-horizontal data {} {:label str :shape [1 4] :grid {:y nil}})
     (b/preprocess-series)
     (b/update-scales :x :fmt int)
     (b/update-scales :x :ticks 5)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 800 :height 300})
     (save "results/lattice/figure_2.10.jpg")
     (show)))

;; figure 2.11

(let [data (->> titanic
              (group-by (juxt :Sex :Age))
              (map-kv (fn [v] [:sbar (map-kv (fn [c] (map :Freq (sort-by :Survived c))) (group-by :Class v)) {:stroke? false}])))]
  (-> (b/lattice :stack-horizontal data {} {:label str :shape [1 4] :grid {:y nil}})
     (b/preprocess-series)
     (b/update-scales :x :fmt int)
     (b/update-scales :x :ticks 5)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 800 :height 300})
     (save "results/lattice/figure_2.10.jpg")
     (show)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chapter 3

;; figure 3.1

(let [data (map :eruptions faithful)]
  (-> (b/series [:density data])
     (b/preprocess-series)
     (b/add-side :bottom 15 (b/series [:strip data {:distort 0.2}]))
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 600 :height 300})
     (save "results/lattice/figure_3.1.jpg")
     (show)))

;; figure 3.2

(let [data (map :eruptions faithful)]
  (-> (b/series [:density data {:kernel-bandwidth 0.05 :samples 200}])
      (b/preprocess-series)
      (b/add-side :bottom 15 (b/series [:rug data {:distort 0.2}]))
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:width 600 :height 200})
      (save "results/lattice/figure_3.2.jpg")
      (show)))

;; v2

(let [data (map :eruptions faithful)]
  (-> (b/series [:density data {:kernel-bandwidth 0.1 :samples 200}]
                [:rug data {:distort 0.2 :scale 0.1}])
      (b/preprocess-series)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:width 600 :height 200})
      (save "results/lattice/figure_3.2v2.jpg")
      (show)))


;; figure 3.3

(first gvhd10);; => {:FSC.H 548, :SSC.H 536, :FL1.H 1, :FL2.H 20.0472, :FL3.H 1, :FL2.A 8, :FL4.H 2.0923, :Days "-6"}

(let [data (->> gvhd10
              (group-by :Days)
              (map-kv (fn [vs] (map #(m/log (:FSC.H %)) vs))))]
  (-> (b/lattice :density data {} {:label str :shape [4 2]})
     (b/preprocess-series)
     (b/tie-domains :x :y)
     (b/update-scales :y :ticks 4)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:width 600 :height 500})
     (save "results/lattice/figure_3.3.jpg")
     (show)))

;; figure 3.4

;; TODO: verify why `log2` changes domain to wide one.

(let [data (->> gvhd10
                (group-by :Days)
                (map-kv (fn [vs] (map #(m/log2 (:FSC.H %)) vs))))]
  (-> (b/lattice :histogram data {:bins 50 :stroke? false :rendering-hint :highest} {:label str :shape [4 2] :grid true})
      (b/preprocess-series)
      (b/tie-domains :x :y)
      (b/update-scales :y :ticks 4)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:width 800 :height 500})
      (save "results/lattice/figure_3.4.jpg")
      (show)))

;; figure 3.5

(let [data (->> chem97
              (group-by :score)
              (map-kv (fn [v] (map :gcsescore v)))
              (sort-by first))]
  (-> (b/lattice :qqplot data {:shape \o} {:label str})
     (b/preprocess-series)
     (b/tie-domains :y)
     (b/update-scales :y :ticks 5)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:height 400})
     (save "results/lattice/figure_3.5.jpg")
     (show)))

;; figure 3.6

(let [data (->> chem97
              (group-by :score)
              (map-kv #(map-kv (fn [v] (map :gcsescore v)) (group-by :gender %))))
      labels (b/lattice :empty (second (first data)) {} {:label str})]
  (-> (mapcat (fn [[k shape]] (b/lattice :qqplot (data k) {:shape shape})) (map vector (keys data) [\o \+ \v \s \x \O]))
     (b/add-series labels)
     (b/preprocess-series)
     (b/tie-domains :x)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:height 400 :width 500})
     (save "results/lattice/figure_3.6.jpg")
     (show)))

;; figure 3.7

(let [data (->> chem97
              (group-by :score)
              (map-kv #(map-kv (fn [v] (map (fn [rec] (m/pow (:gcsescore rec) 2.34)) v)) (group-by :gender %))))
      labels (b/lattice :empty (second (first data)) {} {:label str})]
  (-> (mapcat (fn [[k shape]] (b/lattice :qqplot (data k) {:shape shape})) (map vector (keys data) [\o \+ \v \s \x \O]))
     (b/add-series labels)
     (b/preprocess-series)
     (b/tie-domains :x)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:height 400 :width 600})
     (save "results/lattice/figure_3.7.jpg")
     (show)))

;; figure 3.8

(let [data (->> chem97
              (group-by :gender)
              (map-kv #(sort-by first (map-kv (fn [v] (filter pos? (map :gcsescore  v))) (group-by :score %)))))
      labels (b/lattice :empty (second (first data)) {} {:label str})]
  (-> (mapcat (fn [[k col]] (b/lattice :cdf (data k) {:color col})) (map vector (keys data) (c/palette-presets :category10)))
     (b/add-series labels)
     (b/preprocess-series)
     (b/tie-domains :x)
     (b/update-scales :x :ticks 5)
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:height 400 :width 600})
     (save "results/lattice/figure_3.8.jpg")
     (show)))

;; figure 3.9

(let [uniform (rnd/distribution :uniform-real)
      data (->> chem97
              (group-by :gender)
              (map-kv #(sort-by first (map-kv (fn [v] [uniform (filter pos? (map :gcsescore v))]) (group-by :score %)))))
      labels (b/lattice :empty (second (first data)) {} {:label str :shape [1 6]})]
  (-> (mapcat (fn [[k col]] (b/lattice :ppplot (data k) {:size 2 :color col} {:shape [1 6]})) (map vector (keys data) (c/palette-presets :category10)))
     (b/add-series labels)
     (b/preprocess-series)
     (b/tie-domains :x)
     (b/update-scales :x :ticks [0.4 0.8])
     (b/add-axes :bottom)
     (b/add-axes :left)
     (r/render-lattice {:height 400 :width 600})
     (save "results/lattice/figure_3.9.jpg")
     (show)))

;; figure 3.10

;; TODO: guide line

(let [data (->> chem97
                (group-by :score)
                (map-kv #(let [data (map-kv (fn [v] (map :gcsescore v)) (group-by :gender %))]
                           [(data "M") (data "F")]))
                (sort-by first))
      labels (b/lattice :empty data {} {:label str})]
  (-> (b/lattice :ppplot data {} {:grid true})
      (b/add-series (b/lattice :function (zipmap (keys data) (repeat identity)) {:color (c/color :gray 100) :domain [3 8]}))
      (b/add-series labels)
      (b/preprocess-series)
      (b/tie-domains :x :y)
      (b/update-scales :x :ticks 5)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:height 400 :width 600})
      (save "results/lattice/figure_3.10.jpg")
      (show)))

;; figure 3.11

(let [data (->> chem97
                (group-by :gender)
                (map-kv (fn [g] [:box (sort-by first (map-kv (fn [v] (map :gcsescore v)) (group-by :score g)))])))]
  (-> (b/lattice :stack-horizontal data {} {:label str})
      (b/preprocess-series)
      (b/tie-domains :x)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:height 400 :width 800})
      (save "results/lattice/figure_3.11.jpg")
      (show)))

;; figure 3.12

(let [data (->> chem97
                (group-by :score)
                (map-kv (fn [g] [:box (sort-by first (comp - compare) (map-kv (fn [v] (map (comp #(m/pow % 2.34)  :gcsescore) v)) (group-by :gender g)))]))
                (sort-by first))]
  (-> (b/lattice :stack-vertical data {} {:label str :shape [1 6]})
      (b/preprocess-series)
      (b/tie-domains :x)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:height 400 :width 800})
      (save "results/lattice/figure_3.12.jpg")
      (show)))

;; figure 3.13

(first gvhd10)

(let [data (->> gvhd10
                (group-by :Days)
                (map-kv (partial map (comp #(m/log %) :FSC.H)))
                (sort-by (comp read-string first)))]
  (-> (b/series [:stack-horizontal [:box data]])
      (b/preprocess-series)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:height 400 :width 600})
      (save "results/lattice/figure_3.13.jpg")
      (show)))

;; figure 3.14

(let [data (->> gvhd10
                (group-by :Days)
                (map-kv (partial map (comp #(m/log %) :FSC.H)))
                (sort-by (comp read-string first)))]
  (-> (b/series [:stack-horizontal [:violin data]])
      (b/preprocess-series)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:height 400 :width 600})
      (save "results/lattice/figure_3.14.jpg")
      (show)))


;; figure 3.15

(let [data (->> quakes
                (group-by :mag)
                (map-kv (partial map :depth))
                (sort-by first))]
  (-> (b/series [:stack-horizontal [:strip data {:color blue :size 5 :distort 0.1 :shape \o}]])
      (b/preprocess-series)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:height 400 :width 600})
      (save "results/lattice/figure_3.15.jpg")
      (show)))

;; figure 3.16

(let [data (->> quakes
                (group-by :mag)
                (map-kv (partial map :depth))
                (sort-by first))]
  (-> (b/series [:stack-vertical [:strip data {:color blue :size 5 :distort 0.1 :shape \o}]])
      (b/preprocess-series)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:height 400 :width 600})
      (save "results/lattice/figure_3.16.jpg")
      (show)))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chapter 13

;; figure 13.1

(defn hypotrochoid
  ([] (hypotrochoid (rnd/drand 0.25 0.75)))
  ([r] (hypotrochoid r (rnd/drand (* 0.25 r) r)))
  ([r d] (hypotrochoid r d 10))
  ([r d cycles] (hypotrochoid r d cycles 30))
  ([^double r ^double d cycles density]
   (let [r- (- 1.0 r)]
     (for [cycle (range 0 cycles (/ density))
           :let [t (* m/TWO_PI cycle)
                 f (/ (* r- t) r)]]
       (v/vec2 (+ (* r- (m/cos t))
                  (* d (m/cos f)))
               (- (* r- (m/sin t))
                  (* d (m/sin f))))))))

(defn hypocycloid
  ([x y] (hypocycloid x y x))
  ([x y cycles] (hypocycloid x y cycles 30))
  ([x y cycles density]
   (let [rd (/ x y)]
     (hypotrochoid rd rd cycles density))))

(defn plot-h [p]
  (fn [c]
    (let [s (min (c2d/width c) (c2d/height c))
          halfs (- (/ s 2.0) 2)]
      (-> c
          (c2d/set-color :black)
          (c2d/set-stroke 0.5)
          (c2d/translate (/ (c2d/width c) 2) (/ (c2d/height c) 2))
          (c2d/path (map (fn [v] (v/mult v halfs)) p))))))

(let [free (map #(let [nm (m/approx (/ % 10) 1)
                       p (hypocycloid 10 %)]
                   [nm (plot-h p)]) (range 11 31))]
  
  (-> (b/lattice :free free {} {:shape [5 4] :label str})
      (b/preprocess-series)
      (r/render-lattice {:width 800 :height 1000 :rendering-hint :highest})
      (save "results/lattice/figure_13.1.jpg")
      (show)))

;; figure 13.2

(let [free (repeatedly 42 #(vector nil (plot-h (hypotrochoid))))]
  (rnd/set-seed! rnd/default-rng 20070706)
  (-> (b/lattice :free free {} {:shape [7 6]})
      (b/preprocess-series)
      (r/render-lattice {:width 800 :height 1000 :rendering-hint :highest})
      (save "results/lattice/figure_13.2.jpg")
      (show)))


;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chapter 14

;; figure 14.1

;; grid is not possible, interactive version below

(let [wname "Sunspots - years"
      data (map (juxt (comp dt/local-date-time int :time-id) :x) sunspot-year)
      domain (second (extent (map second data)))
      cnt (- (count data) 5)
      draw-fn (fn [c w _ last-mouse]
                (let [window (c2d/get-state w)
                      curr-mouse (c2d/mouse-x w)
                      mx (if (neg? curr-mouse) (or last-mouse 0) curr-mouse)
                      mx (int (m/norm mx 0 600 0 cnt))
                      d (take window (drop mx data))]
                  (c2d/image c (-> (b/series [:grid] [:line d])
                                   (b/preprocess-series)
                                   (b/update-scales :y :domain domain)
                                   (b/add-axes :bottom)
                                   (b/add-axes :left)
                                   (r/render-lattice {:height 300 :width 600})))
                  (if (neg? curr-mouse) last-mouse curr-mouse)))
      window (c2d/show-window {:canvas (c2d/canvas 600 300)
                               :window-name wname
                               :draw-fn draw-fn
                               :state 50})]
  
  (defmethod c2d/key-pressed [wname \.] [_ s] (min cnt (inc s)))
  (defmethod c2d/key-pressed [wname \,] [_ s] (max 2 (dec s)))
  (defmethod c2d/mouse-event [wname :mouse-pressed] [_ s] (c2d/save window "results/lattice/figure_14.1.jpg") s))

;; figure 14.2

;; time goes from 1 to 61

(let [[time seasonal trend remainder] (map #(map % ssd) [:time-id :seasonal :trend :remainder])
      data (reverse [[:data (map #(vector %1 (+ %2 %3 %4)) time seasonal trend remainder)]
                     [:seasonal (map vector time seasonal)]
                     [:trend (map vector time trend)]
                     [:remainder (map vector time remainder)]])]
  (-> (b/lattice :line data {} {:shape [4 1] :label name})
      (b/preprocess-series)
      (b/update-scales :y :ticks 5)
      (b/update-scale :x :fmt int)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:height 400 :width 600 :padding-in 0.2})
      (save "results/lattice/figure_14.2.jpg")
      (show)))

;; figure 14.3

(let [data (->> gvhd-fsc-h
                (group-by :patient)
                (map-kv (fn [v] [:density-strip
                                (map-kv (partial mapcat :data) (group-by :visit v))
                                {:margins {:x [0.1 0.0]} :color (fn [_ {:keys [id]}]
                                                                  (if (odd? id) blue lblue)) :scale 4 :area? true}])))]
  (-> (b/lattice :stack-horizontal data {} {:label str})
      (b/preprocess-series)
      (b/update-scales :x :ticks 3)
      (b/add-axes :bottom)
      (b/add-axes :left)
      (r/render-lattice {:height 400 :width 600})
      (save "results/lattice/figure_14.3.jpg")
      (show)))

;; figure 14.4

