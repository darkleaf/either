(ns darkleaf.either)

(defprotocol Either
  (extract [this])
  (left? [this])
  (right? [this])
  (invert [this])
  (-bimap [this leftf rightf]))

(alter-meta! #'Either assoc :private true)
(alter-meta! #'-bimap assoc :private true)

(declare left)
(declare right)

(extend-protocol Either
  Object
  (extract [this] this)
  (left? [this] false)
  (right? [this] true)
  (invert [this] (left this))
  (-bimap [this leftf rightf] (-> this rightf))

  nil
  (extract [this] this)
  (left? [this] false)
  (right? [this] true)
  (invert [this] (left this))
  (-bimap [this leftf rightf] (-> this rightf)))

(deftype Left [value]
  Either
  (extract [this] value)
  (left? [this] true)
  (right? [this] false)
  (invert [this] value)
  (-bimap [this leftf rightf] (-> value leftf left))

  Object
  (equals [this other]
    (and
     (left? other)
     (= value (extract other))))
  (hashCode [_] (hash value))

  clojure.lang.IHashEq
  (hasheq [_] (hash value)))

(defmethod print-method Left [v ^java.io.Writer w]
         (doto w
           (.write "#<Left ")
           (.write (pr-str (extract v)))
           (.write ">")))

(defn right
  ([] nil)
  ([val] val))

(defn left
  ([] (->Left nil))
  ([val] (->Left val)))

(defn bimap [leftf rightf mv]
  (-bimap mv leftf rightf))

(defn map-left [f mv]
  (bimap f identity mv))

(defn map-right [f mv]
  (bimap identity f mv))

(defmacro let= [bindings & body]
  (assert (-> bindings count even?))
  (if (empty? bindings)
    `(do ~@body)
    (let [[name expr & bindings] bindings]
      `(let [~name ~expr]
         (if (left? ~name)
           ~name
           (let= [~@bindings]
             ~@body))))))

(defn >>=
  ([mv f=] (let= [v mv] (f= v)))
  ([mv f= & fs=] (reduce >>= mv (cons f= fs=))))

(defmacro >> [& mvs]
  (assert (seq mvs))
  (let [val (gensym "val")]
    `(let= [~@(interleave (repeat val) mvs)]
       (right ~val))))
