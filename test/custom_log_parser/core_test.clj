(ns custom-log-parser.core-test
  (:import [java.io RandomAccessFile ByteArrayInputStream])
  (:require [clojure.test :refer :all]
            [custom-log-parser.core :refer :all]))

(deftest test-read-size!
  (testing "fixed"
    (let [fp (RandomAccessFile. "test/custom_log_parser/test1.log" "r")]
      (is (= 23 (read-size! fp))))))
