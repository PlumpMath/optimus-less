(ns optimus-less.test-helper
  (:require [clojure.java.io :as io]))

(def tmp-dir "test/resources/with-files-tmp")

(def public-dir "with-files-tmp")

(def ^:dynamic *last-modified*)

(defmacro with-tmp-dir [& body]
  `(do
     (.mkdirs (io/file tmp-dir))
     (let [result# (do ~@body)]
       (delete-file-recursively tmp-dir)
       result#)))

(defn create-file-and-dirs [path contents]
  (let [full-path (str tmp-dir path)]
    (.mkdirs (.getParentFile (io/file full-path)))
    (spit full-path contents)))

(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its contents.
Raise an exception if any deletion fails unless silently is true."
  [f & [silently]]
  (let [f (io/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child silently)))
    (io/delete-file f silently)))

(defmacro with-files [files & body]
  `(do
     (binding [*last-modified* (* (.intValue (/ (System/currentTimeMillis) 1000)) 1000)]
       (doseq [[path# contents#] ~files] (create-file-and-dirs path# contents#))
       (let [result# (do ~@body)]
         (delete-file-recursively tmp-dir)
         result#))))

