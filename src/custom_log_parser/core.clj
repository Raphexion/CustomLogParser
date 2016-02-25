(ns custom-log-parser.core
  (:import [java.io RandomAccessFile ByteArrayInputStream])
  (:import [java.text SimpleDateFormat])
  (:require [clojure.xml :as xml])
  (:gen-class))

;; Only construct on SimpleDateFormat. Hide it from other uses.
(let [log-date-format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss,S")]
  (defn parse-date
  "Assuming that the string follows a given format,
  read out the date and time."
  [date]
  (if date
    (.parse log-date-format date)
    nil)))

(defn read-size!
  "Read a int (32-bit) at the current
  file position. This operation will
  move the fileposition forward with
  4 bytes."
  [file]
  (try
    (let [size (.readInt file)]
      (if (>= size 0)
        size
        nil))
    (catch Exception e nil)))

(defn read-string!
  "Read size number of bytes into an array.
  The file position will automatically get
  incremented."
  [file size]
  (let [tmp (byte-array size)]
    (doall
     (.read file tmp)
     tmp)))

(defn parse-xml
  [s]
  (xml/parse (ByteArrayInputStream. (.getBytes s))))

(defn read-message-date!
  "Assume that the following block of text
  contains a date."
  [file size]
  (parse-date (String. (read-string! file size))))

(defn read-message-text!
  "Assume that the following block of text
  is text.
  CAVEAT:
  The string is incorrectly placed
  int the block, we therefore drop the first
  2 characters and remove the very last
  character."
  [file size]
  (let [tmp (read-string! file size)]
    (apply str (map char (drop-last (drop 2 tmp))))))

(defn read-one-message!
  "One message is composed of two parts:
  1) A block that contains the date
  2) A block that contains the XML message.

  Design choice:
  We will not actuall load the XML message.
  Only the position and size is noted.
  The actual XML message is later extracted.
  The rationale is that we don't want to
  load and parse XML for message that
  are are too old or too new for our
  later analysis."
  [file]
  (if-let [dsize (read-size! file)]
    (let [date (read-message-date! file dsize)]
      (if-let [tsize (read-size! file)]
        (let [pos (.getFilePointer file)]
          (.skipBytes file tsize)
          {:date date
           :pos pos
           :size tsize})
        nil))
    nil))

(defn extract-block!
  "Given a file and information about the
  position of the XML data. Extract the
  XML data."
  [file {date :date pos :pos size :size}]
  (.seek file pos)
  (let [s (read-message-text! file size)]
    {:date date
     :text (parse-xml s)}))

(defn read-all-messages!
  "Given a file with many messages,
  read all messages one-by-one."
  [file]
  (loop [messages []]
    (if-let [m (read-one-message! file)]
      (recur (conj messages m))
      messages)))

(defn -main
  [& args]
  (println "Hello World!"))
