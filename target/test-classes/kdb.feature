Feature: Interacting with KDB

  Background:
    Given the Q server is started on port 0
    And a connection can be made to the Q server
    Then confirm Q is listening on a random port

  Scenario: Create a table called tab which contains a hundred rows and 4 columns of random time-series sales data
    Given the following query is executed:
      | n:100;                                             |
      | item:`apple`banana`orange`pear;                    |
      | city:`beijing`chicago`london`paris;                |
      | tab:([]time:asc n?0D0;n?item;amount:n?100;n?city); |
#    When the following query is executed:
      | select from tab where item=`banana                 |
#    Then the following data is returned:
#      |  |
#      | time                 | item   | amount | city    |
#      | 0D00:00:00.466201454 | banana | 31     | london  |
#      | 0D00:00:00.712388008 | banana | 86     | london  |
#      | 0D00:00:00.952962040 | banana | 20     | london  |
#      | 0D00:00:01.036425679 | banana | 49     | chicago |
#      | 0D00:00:01.254006475 | banana | 94     | beijing |

#  select from t where upper[name] in upper `$("Coca Cola";"Pepsi")

  Scenario: Loading a CSV file into KDB
    Given the following CSV file is loaded into KDB as 'data': src/test/resources/pres.elect16.results.csv
    When the metadata for table 'data' is queried
#      | select from data where upper[lead] in upper `$("Donald Trump") |
#    Then the following data is returned:
#      ||
#    When the metadata for table 'data' is queried
#    Then the following data is returned:
#    ||
