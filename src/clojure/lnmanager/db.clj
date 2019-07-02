(ns ln.db
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as hsql]
            [honeysql.helpers :refer :all :as helpers]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
           
  (:import java.sql.DriverManager))


(def pg-db  {:dbtype "postgresql"
            :dbname "lndb"
            :host "127.0.0.1"
            :user "ln_admin"
             :password "welcome"
             :port "5432"
            :ssl false
            :sslfactory "org.postgresql.ssl.NonValidatingFactory"})


(def all-table-names
  ;;for use in a map function that will delete all tables
  ;;single command looks like:  (jdbc/drop-table-ddl  :lnuser {:conditional? true } )
  [ :layout_source_dest :plate_layout_name :plate_format :plate_type :project :lnsession :lnuser :lnuser_groups] )

(map #(jdbc/db-do-commands pg-db (jdbc/drop-table-ddl % {:conditional? true } )) all-table-names)


(def all-tables
  ;;for use in a map function that will create all tables
  ;; example single table:
  ;;;     [(jdbc/create-table-ddl :lnsession
  ;;                 [[:id "SERIAL PRIMARY KEY"]
  ;;                  [:lnuser_id :int]
  ;;                  [:updated  :timestamp "with time zone not null DEFAULT current_timestamp"]
 ;;                   ["FOREIGN KEY (lnuser_id) REFERENCES lnuser(id)"]]) ]

  [ 
   [(jdbc/create-table-ddl :lnuser_groups
                         [[:id "SERIAL PRIMARY KEY"]
                          [:usergroup "varchar(250)"]
                          [:updated  :timestamp "with time zone not null DEFAULT current_timestamp"]])]
   
   [(jdbc/create-table-ddl :lnuser
                          [[:id "SERIAL PRIMARY KEY"]
                           [:usergroup :int]
                           [:lnuser_name "VARCHAR(250) not null unique"]
                           [:tags "varchar(250)"]
                           [:password "varchar(64) not null"]
                           [:updated  :timestamp "with time zone not null DEFAULT current_timestamp"]
                           ["FOREIGN KEY (usergroup) REFERENCES lnuser_groups(id)"]])]

 
   [(jdbc/create-table-ddl :lnsession
                         [[:id "SERIAL PRIMARY KEY"]
                          [:lnuser_id :int]
                            [:updated  :timestamp "with time zone not null DEFAULT current_timestamp"]
                          ["FOREIGN KEY (lnuser_id) REFERENCES lnuser(id)"]]) ]
   
   [(jdbc/create-table-ddl :project
                           [[:id "SERIAL PRIMARY KEY"]
                            [:project_sys_name "varchar(30)"]
                            [:descr "varchar(250)"]
                            [:project_name "varchar(250)"]
                           [:lnsession_id :int]
                            [:updated  :timestamp "with time zone not null DEFAULT current_timestamp"]
                            ["FOREIGN KEY (lnsession_id) REFERENCES lnsession(id)"]]
                           )]
   ;;CREATE INDEX ON project(lnsession_id);
   [(jdbc/create-table-ddl :plate_type
                           [[:id "SERIAL PRIMARY KEY"]
                            [:plate_type_name "varchar(30)"] ])]
   
   [(jdbc/create-table-ddl :plate_format
                           [[:id "SERIAL PRIMARY KEY"]
                            [:format "varchar(6)"]
                            [:rownum :int]
                            [:colnum :int]])]
   [(jdbc/create-table-ddl :plate_layout_name
                           [[:id "SERIAL PRIMARY KEY"]
                            [:sys_name "varchar(30)"]
                            [:name "varchar(250)"]
                            [:descr "varchar(250)"]
                            [:plate_format_id :int]
                            [:replicates :int]
                            [:targets :int]
                            [:use_edge :int]
                            [:num_controls :int]
                            [:unknown_n :int]
                            [:control_loc "varchar(30)"]
                            [:source_dest "varchar(30)"]
                            ["FOREIGN KEY (plate_format_id) REFERENCES plate_format(id)"]])]
   [(jdbc/create-table-ddl :layout_source_dest
                           [[:src :int "NOT NULL"]
                            [:dest :int "NOT NULL"]
                            ])]
   ])

;;CREATE INDEX ON plate_layout_name(plate_format_id);

(map #(jdbc/db-do-commands pg-db %) all-tables)

 
(def required-data
  ;;inserts required data into table using jdbc/insert-multi!
  ;;this is data that should not be deleted when repopulating with example data
  ;;this is data that is needed for basic functionality
  [[ :lnuser_groups 
    [ :usergroup ]
    [["administrator"]
     ["user" ]]]
                     
   [ :lnuser 
    [ :lnuser_name :tags :usergroup :password ]
    [["ln_admin" "ln_admin@labsolns.com" 1  "welcome"]
     ["ln_user" "ln_user@labsolns.com" 1 "welcome"]
     ["klohymim" "NA" 1 "hwc3v4_rbkT-1EL2KI-JBaqFq0thCXM_"]]]
   
   [ :plate_type [:plate_type_name]
    [["assay"]["rearray"]["master"]["daughter"]["archive"]["replicate"]]]
   
   [ :plate_format [:id :format :rownum :colnum]
    [[ 96, "96", 8, 12]
     [384, "384",16, 24]
     [1536, "1536", 32, 48]]]

   [ :plate_layout_name [:sys_name :name :descr :plate_format_id :replicates :targets :use_edge :num_controls :unknown_n :control_loc :source_dest]
    [
      ["LYT-1" "4 controls col 12" "1S1T" 96 1 1 1 4 92 "E12-H12" "source"]
 ["LYT-2" "4 controls cols 23 24" "1S4T" 384  1 4 1 4 368 "I23-P24" "dest"]
     ["LYT-3" "4 controls cols 23 24" "2S2T" 384  2 2 1 4 368 "I23-P24" "dest"]
     ["LYT-4" "4 controls cols 23 24" "2S4T" 384  2 4 1 4 368 "I23-P24" "dest"]
 ["LYT-5" "4 controls cols 23 24" "4S1T" 384  4 1 1 4 368 "I23-P24" "dest"]
 ["LYT-6" "4 controls cols 23 24" "4S2T" 384  4 2 1 4 368 "I23-P24" "dest"]
 ["LYT-7" "8 controls col 12" "1S1T" 96  1 1 1 8 88 "A12-H12" "source"]
 ["LYT-8" "8 controls cols 23 24" "1S4T" 384  1 4 1 8 352 "A23-P24" "dest"]
 ["LYT-9" "8 controls cols 23 24" "2S2T" 384  2 2 1 8 352 "A23-P24" "dest"]
 ["LYT-10" "8 controls cols 23 24" "2S4T" 384  2 4 1 8 352 "A23-P24" "dest"]
 ["LYT-11" "8 controls cols 23 24" "4S1T" 384  4 1 1 8 352 "A23-P24" "dest"]
 ["LYT-12" "8 controls cols 23 24" "4S2T" 384  4 2 1 8 352 "A23-P24" "dest"]
 ["LYT-13" "8 controls col 24" "1S1T" 384  1 1 1 8 376 "I24-P24" "source"]
 ["LYT-14" "8 controls cols 47 48" "1S4T" 1536  1 4 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-15" "8 controls cols 47 48" "2S2T" 1536  2 2 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-16" "8 controls cols 47 48" "2S4T" 1536  2 4 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-17" "8 controls cols 47 48" "4S1T" 1536  4 1 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-18" "8 controls cols 47 48" "4S2T" 1536  4 2 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-19" "16 controls col 24" "1S1T" 384  1 1 1 16 368 "A24-P24" "source"]
 ["LYT-20" "16 controls cols 47 48" "1S4T" 1536  1 4 1 16 1472 "A47-AF48" "dest"]
 ["LYT-21" "16 controls cols 47 48" "2S2T" 1536  2 2 1 16 1472 "A47-AF48" "dest"]
 ["LYT-22" "16 controls cols 47 48" "2S4T" 1536  2 4 1 16 1472 "A47-AF48" "dest"]
 ["LYT-23" "16 controls cols 47 48" "4S1T" 1536  4 1 1 16 1472 "A47-AF48" "dest"]
 ["LYT-24" "16 controls cols 47 48" "4S2T" 1536  4 2 1 16 1472 "A47-AF48" "dest"]
 ["LYT-25" "7 controls col 23" "1S1T" 384  1 1 0 7 301 "I23-O23" "source"]
 ["LYT-26" "7 controls cols 46 47" "1S4T" 1536  1 4 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-27" "7 controls cols 46 47" "2S2T" 1536  2 2 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-28" "7 controls cols 46 47" "2S4T" 1536  2 4 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-29" "7 controls cols 46 47" "4S1T" 1536  4 1 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-30" "7 controls cols 46 47" "4S2T" 1536  4 2 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-31" "14 controls col 23" "1S1T" 384  1 1 0 14 294 "B23-O23" "source"]
 ["LYT-32" "14 controls cols 46 47" "1S4T" 1536  1 4 0 14 1176 "B46-AE47" "dest"]
 ["LYT-33" "14 controls cols 46 47" "2S2T" 1536  2 2 0 14 1176 "B46-AE47" "dest"]
 ["LYT-34" "14 controls cols 46 47" "2S4T" 1536  2 4 0 14 1176 "B46-AE47" "dest"]
 ["LYT-35" "14 controls cols 46 47" "4S1T" 1536  4 1 0 14 1176 "B46-AE47" "dest"]
 ["LYT-36" "14 controls cols 46 47" "4S2T" 1536  4 2 0 14 1176 "B46-AE47" "dest"]
 ["LYT-37" "8 controls cols 47 48" "1S1T" 1536  1 1 1 8 1504 "Q47-AF48" "source"]
 ["LYT-38" "16 controls cols 47 48" "1S1T" 1536  1 1 1 16 1472 "A47-AF48" "source"]
 ["LYT-39" "7 controls cols 46 47" "1S1T" 1536  1 1 0 7 1204 "Q46-AE47" "source"]

     
     ["LYT-40" "14 controls cols 46 47" "1S1T" 1536  1 1 0 14 1176 "B46-AE47" "source"]
     ["LYT-41" "all blanks" "not reformattable" 1536  1 1 0 0 0 "none" "dest"]
     
      ]]
  
   ])



(map #(jdbc/db-do-commands pg-db (jdbc/drop-table-ddl % {:conditional? true } )) all-table-names)
(map #(jdbc/db-do-commands pg-db %) all-tables)
;; errors because brackets not stripped
;;(map #(jdbc/insert-multi! pg-db %) required-data)
(map #(apply jdbc/insert-multi! pg-db % ) required-data)





 
      ]]]



 ["LYT-5" "4 controls cols 23 24" "4S1T" 384  4 1 1 4 368 "I23-P24" "dest"]
 ["LYT-6" "4 controls cols 23 24" "4S2T" 384  4 2 1 4 368 "I23-P24" "dest"]
 ["LYT-7" "8 controls col 12" "1S1T" 96  1 1 1 8 88 "A12-H12" "source"]
 ["LYT-8" "8 controls cols 23 24" "1S4T" 384  1 4 1 8 352 "A23-P24" "dest"]
 ["LYT-9" "8 controls cols 23 24" "2S2T" 384  2 2 1 8 352 "A23-P24" "dest"]
 ["LYT-10" "8 controls cols 23 24" "2S4T" 384  2 4 1 8 352 "A23-P24" "dest"]
 ["LYT-11" "8 controls cols 23 24" "4S1T" 384  4 1 1 8 352 "A23-P24" "dest"]
 ["LYT-12" "8 controls cols 23 24" "4S2T" 384  4 2 1 8 352 "A23-P24" "dest"]
 ["LYT-13" "8 controls col 24" "1S1T" 384  1 1 1 8 376 "I24-P24" "source"]
 ["LYT-14" "8 controls cols 47 48" "1S4T" 1536  1 4 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-15" "8 controls cols 47 48" "2S2T" 1536  2 2 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-16" "8 controls cols 47 48" "2S4T" 1536  2 4 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-17" "8 controls cols 47 48" "4S1T" 1536  4 1 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-18" "8 controls cols 47 48" "4S2T" 1536  4 2 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-19" "16 controls col 24" "1S1T" 384  1 1 1 16 368 "A24-P24" "source"]
 ["LYT-20" "16 controls cols 47 48" "1S4T" 1536  1 4 1 16 1472 "A47-AF48" "dest"]
 ["LYT-21" "16 controls cols 47 48" "2S2T" 1536  2 2 1 16 1472 "A47-AF48" "dest"]
 ["LYT-22" "16 controls cols 47 48" "2S4T" 1536  2 4 1 16 1472 "A47-AF48" "dest"]
 ["LYT-23" "16 controls cols 47 48" "4S1T" 1536  4 1 1 16 1472 "A47-AF48" "dest"]
 ["LYT-24" "16 controls cols 47 48" "4S2T" 1536  4 2 1 16 1472 "A47-AF48" "dest"]
 ["LYT-25" "7 controls col 23" "1S1T" 384  1 1 0 7 301 "I23-O23" "source"]
 ["LYT-26" "7 controls cols 46 47" "1S4T" 1536  1 4 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-27" "7 controls cols 46 47" "2S2T" 1536  2 2 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-28" "7 controls cols 46 47" "2S4T" 1536  2 4 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-29" "7 controls cols 46 47" "4S1T" 1536  4 1 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-30" "7 controls cols 46 47" "4S2T" 1536  4 2 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-31" "14 controls col 23" "1S1T" 384  1 1 0 14 294 "B23-O23" "source"]
 ["LYT-32" "14 controls cols 46 47" "1S4T" 1536  1 4 0 14 1176 "B46-AE47" "dest"]
 ["LYT-33" "14 controls cols 46 47" "2S2T" 1536  2 2 0 14 1176 "B46-AE47" "dest"]
 ["LYT-34" "14 controls cols 46 47" "2S4T" 1536  2 4 0 14 1176 "B46-AE47" "dest"]
 ["LYT-35" "14 controls cols 46 47" "4S1T" 1536  4 1 0 14 1176 "B46-AE47" "dest"]
 ["LYT-36" "14 controls cols 46 47" "4S2T" 1536  4 2 0 14 1176 "B46-AE47" "dest"]
 ["LYT-37" "8 controls cols 47 48" "1S1T" 1536  1 1 1 8 1504 "Q47-AF48" "source"]
 ["LYT-38" "16 controls cols 47 48" "1S1T" 1536  1 1 1 16 1472 "A47-AF48" "source"]
 ["LYT-39" "7 controls cols 46 47" "1S1T" 1536  1 1 0 7 1204 "Q46-AE47" "source"]



(clojure.pprint/pprint (first required-data))

(def wells96 ["A01" "B01" "C01" "D01" "E01" "F01" "G01" "H01" "A02" "B02" "C02" "D02" "E02" "F02" "G02" "H02" "A03" "B03" "C03" "D03" "E03" "F03" "G03" "H03" "A04" "B04" "C04" "D04" "E04" "F04" "G04" "H04" "A05" "B05" "C05" "D05" "E05" "F05" "G05" "H05" "A06" "B06" "C06" "D06" "E06" "F06" "G06" "H06" "A07" "B07" "C07" "D07" "E07" "F07" "G07" "H07" "A08" "B08" "C08" "D08" "E08" "F08" "G08" "H08" "A09" "B09" "C09" "D09" "E09" "F09" "G09" "H09" "A10" "B10" "C10" "D10" "E10" "F10" "G10" "H10" "A11" "B11" "C11" "D11" "E11" "F11" "G11" "H11" "A12" "B12" "C12" "D12" "E12" "F12" "G12" "H12"])

(def wells384 ["A01" "B01" "C01" "D01" "E01" "F01" "G01" "H01" "I01" "J01" "K01" "L01" "M01" "N01" "O01" "P01" "A02" "B02" "C02" "D02" "E02" "F02" "G02" "H02" "I02" "J02" "K02" "L02" "M02" "N02" "O02" "P02" "A03" "B03" "C03" "D03" "E03" "F03" "G03" "H03" "I03" "J03" "K03" "L03" "M03" "N03" "O03" "P03" "A04" "B04" "C04" "D04" "E04" "F04" "G04" "H04" "I04" "J04" "K04" "L04" "M04" "N04" "O04" "P04" "A05" "B05" "C05" "D05" "E05" "F05" "G05" "H05" "I05" "J05" "K05" "L05" "M05" "N05" "O05" "P05" "A06" "B06" "C06" "D06" "E06" "F06" "G06" "H06" "I06" "J06" "K06" "L06" "M06" "N06" "O06" "P06" "A07" "B07" "C07" "D07" "E07" "F07" "G07" "H07" "I07" "J07" "K07" "L07" "M07" "N07" "O07" "P07" "A08" "B08" "C08" "D08" "E08" "F08" "G08" "H08" "I08" "J08" "K08" "L08" "M08" "N08" "O08" "P08" "A09" "B09" "C09" "D09" "E09" "F09" "G09" "H09" "I09" "J09" "K09" "L09" "M09" "N09" "O09" "P09" "A10" "B10" "C10" "D10" "E10" "F10" "G10" "H10" "I10" "J10" "K10" "L10" "M10" "N10" "O10" "P10" "A11" "B11" "C11" "D11" "E11" "F11" "G11" "H11" "I11" "J11" "K11" "L11" "M11" "N11" "O11" "P11" "A12" "B12" "C12" "D12" "E12" "F12" "G12" "H12" "I12" "J12" "K12" "L12" "M12" "N12" "O12" "P12" "A13" "B13" "C13" "D13" "E13" "F13" "G13" "H13" "I13" "J13" "K13" "L13" "M13" "N13" "O13" "P13" "A14" "B14" "C14" "D14" "E14" "F14" "G14" "H14" "I14" "J14" "K14" "L14" "M14" "N14" "O14" "P14" "A15" "B15" "C15" "D15" "E15" "F15" "G15" "H15" "I15" "J15" "K15" "L15" "M15" "N15" "O15" "P15" "A16" "B16" "C16" "D16" "E16" "F16" "G16" "H16" "I16" "J16" "K16" "L16" "M16" "N16" "O16" "P16" "A17" "B17" "C17" "D17" "E17" "F17" "G17" "H17" "I17" "J17" "K17" "L17" "M17" "N17" "O17" "P17" "A18" "B18" "C18" "D18" "E18" "F18" "G18" "H18" "I18" "J18" "K18" "L18" "M18" "N18" "O18" "P18" "A19" "B19" "C19" "D19" "E19" "F19" "G19" "H19" "I19" "J19" "K19" "L19" "M19" "N19" "O19" "P19" "A20" "B20" "C20" "D20" "E20" "F20" "G20" "H20" "I20" "J20" "K20" "L20" "M20" "N20" "O20" "P20" "A21" "B21" "C21" "D21" "E21" "F21" "G21" "H21" "I21" "J21" "K21" "L21" "M21" "N21" "O21" "P21" "A22" "B22" "C22" "D22" "E22" "F22" "G22" "H22" "I22" "J22" "K22" "L22" "M22" "N22" "O22" "P22" "A23" "B23" "C23" "D23" "E23" "F23" "G23" "H23" "I23" "J23" "K23" "L23" "M23" "N23" "O23" "P23" "A24" "B24" "C24" "D24" "E24" "F24" "G24" "H24" "I24" "J24" "K24" "L24" "M24" "N24" "O24" "P24" ])



