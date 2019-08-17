# Either

[![CircleCI](https://circleci.com/gh/darkleaf/either/tree/master.svg?style=svg)](https://circleci.com/gh/darkleaf/either/tree/master)

Реализация монады Either.

Моделирует вычисления, которые могут закончитьсь неудачей.

Either не противопоставляется исключениям.
Either стоит использовать, когда ошибки являются не исключительными случаями.
Например, невалидные параметры - обычный случай.

# Пример

```clojure
(ns example.app
  (:require
   ;;...
   [darkleaf.either :as e]))

;;...

(defn- check-logged-out= []
  (if (user-session/logged-out?)
    (e/right)
    (e/left [::already-logged-in])))

(defn- check-params= [params]
  (if-let [exp (s/explain-data ::params params)]
    (e/left [::invalid-params exp])
    (e/right)))

(defn- check-not-registered= [params]
  (if (user-q/get-by-login (:login params))
    (e/left [::already-registered])
    (e/right)))

(defn- create-user [params]
  (storage/tx-create (user/build params)))

(defn process [params]
  (e/extract
   (e/let= [ok   (check-logged-out=)
            ok   (check-params= params)
            ok   (check-not-registered= params)
            user (create-user params)]
     (user-session/log-in! user)
     [::processed user])))
```

# Api

`right` и `left` - конструкторы соответствующих типов.
Могут принимать один аргумент или не принимать аргументов вовсе.
Если вызваны без агрументов, то оборачивают `nil`.

***

`extract` - извлекает значение

***

Макрос `let=` реализует всю монадическую "магию".

Работает он следующим образом. Допустим, есть выражение:

```clojure
(let= [val (fn-1)]
  (fn-2 val)
  (fn-3 val))
```

+ Если `fn-1` вернула `(left 1)`, то результатом всего выражения будет `(left 1)`
+ Если `fn-1` вернула `(right 1)`, то `val` связывается с `1`
+ Eсли `fn-1` вернула `1`, то `val` связывается с `1`
+ Значение выражения `(fn-2 val)` всегда игнорируется, по аналогии с обычным `let`
+ Если `fn-3` вернула `(left 3)`, то результатом всего выражения будет `(left 3)`
+ Если `fn-3` вернула `(right 3)`, то результатом всего выражения будет `(right 3)`
+ Если `fn-3` вернула `3`, то результатом всего выражения будет `(right 3)`

***

Предикаты `left?` и `right?` тестируют значение на соотвутствующий тип.

***

`invert` меняет обертку на противоположную. Например, `(invert (left 1))` возвращает `(right 1)`.

***

`bimap`, `map-left`, `map-right` - применяют функцию к содержимому контейнера.

***

`>>=` позволяет строить цепочки вызовов. Результат функции предается в другую функцию.
Например `(>>= (fn-1) fn-2 fn-3)`.
Правила аналогичны `let=`, и данную цепочку можно записать как

```clojure
(let [v (fn-1)
      v (fn-2 v)
      v (fn-3 v)]
  v)
```

***

Макрос `>>` строит цепочки из значений: `(>> (fn-1) (fn-2) (fn-3))`. Подобен `and` для булевых значений.

***

Все случаи использования смотрите в тестах.

# Install

deps.edn:

```edn
{:deps {darkleaf/either
        {:git/url "https://github.com/darkleaf/either.git"
         :sha     "b84fedd52afef27f938531a4836202512ae987e2"}}}
```
