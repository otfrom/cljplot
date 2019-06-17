(ns bayesian-optimisation
  (:require [cljplot.render :as r]
            [cljplot.build :as b]
            [cljplot.common :refer :all]
            [fastmath.core :as m]
            [fastmath.random :as rnd]
            [cljplot.core :refer :all]
            [fastmath.optimization :as opt]
            [fastmath.kernel :as k]
            [fastmath.regression :as reg]
            [clojure2d.color :as c]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(m/use-primitive-operators)

;; https://github.com/fmfn/BayesianOptimization/blob/master/examples/visualization.ipynb

(defn target
  "1d target function for optimization."
  ^double [^double x]
  (+ (m/exp (- (m/sq (- x 2.0))))
     (m/exp (- (* 0.1 (m/sq (- x 6.0)))))
     (/ (inc (* x x)))))

(defn target
  ^double [^double x]
  (- (+ (* x (m/sin x))
        (* x (m/cos (+ x x))))))

(defn target
  ^double [^double x]
  (- (/ (+ (m/sq x) (* -5.0 x) 6.0)
        (inc (m/sq x)))))

(defn target
  ^double [^double x]
  (* (+ x (m/sin x))
     (m/exp (- (m/sq x)))))

(defn target
  ^double [^double x]
  (- (+ 10.0 (- (* x x) (* 10.0 (m/cos (* m/TWO_PI x)))))))

(def bounds [-5.12 5.12])

(-> (xy-chart {:width 600 :height 600}
              (b/series [:grid]
                        [:function target {:domain bounds :samples 500}])
              (b/add-axes :bottom)
              (b/add-axes :left))
    (show))

(def optimizer (opt/bayesian-optimization target {:kernel (k/kernel :mattern-52 0.9)
                                                  :bounds [bounds]
                                                  :utility-function-type :ei
                                                  :init-points 1
                                                  :jitter 1
                                                  :utility-param 0.3}))

(defn draw-bo
  ([opt] (draw-bo opt 0))
  ([opt idx]
   (let [{:keys [x y util-fn gp xs ys util-best]} (nth opt idx)
         pairs (map vector (map first xs) ys)
         [x1 x2] bounds
         xtest (map #(m/norm % 0 199 x1 x2) (range 200))
         ms-pairs (reg/predict-all gp xtest true)
         mu (map first ms-pairs)
         stddev (map second ms-pairs)
         s95 (map (partial fast* 1.96) stddev)
         s50 (map (partial fast* 0.67) stddev)]
     (-> (xy-chart {:width 800 :height 500}
                   (b/series [:grid]
                             [:ci [(map vector xtest (map fast- mu s95)) (map vector xtest (map fast+ mu s95))] {:color (c/color :lightblue 120)}]
                             [:ci [(map vector xtest (map fast- mu s50)) (map vector xtest (map fast+ mu s50))] {:color (c/color :lightblue)}]
                             [:function target {:stroke {:size 2} :domain bounds :samples 400}]
                             [:line (map vector xtest mu) {:color :darkblue :stroke {:size 2 :dash [20 3]}}]
                             [:vline (first util-best) {:color :black :size 2 :dash [10 5]}]
                             [:scatter pairs {:size 8 :color :darkcyan}]
                             [:scatter [[(first x) y]] {:color :maroon :size 10}])
                   (b/add-side :top 100 (b/series [:grid nil {:y nil}]
                                                  [:function util-fn {:domain bounds :samples 400}]
                                                  [:vline (first util-best) {:color :black :size 2 :dash [10 5]}]))
                   (b/add-axes :bottom)
                   (b/add-axes :left))
         (show)))))

(draw-bo optimizer 10)

(-> (xy-chart {:width 600 :height 600}
              (b/series [:grid]
                        [:function (:util-fn (nth optimizer 15)) {:domain [-2 10]}])
              (b/add-axes :bottom)
              (b/add-axes :left))
    (show))


(keys (nth optimizer 0));; => (:x :y :util-fn :gp :xs :ys)
