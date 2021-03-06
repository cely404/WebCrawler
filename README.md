
Jar file name: WebCrawler.jar 
Example Input: java -jar WebCrawler.jar "http://www.cnn.com/2013/06/10/politics/edward-snowden-profile/"

My general approach to solve the problem was to implement a
modified version of the Apiori Algorithm explained below.

https://www.youtube.com/watch?v=TcUlzuQ27iQ
https://en.wikipedia.org/wiki/Apriori_algorithm#Overview

"It is an algorithm for frequent item set mining and association rule learning 
over transactional databases."

The apriori algorithm starts with 1 element sets and verifies that all the elements 
are present in the data set. Each data element must meet a certain requirement to 
move onto the next level of processing. My requirement was that each 
element had to appear at least twice in order to move to the next round. This value 
can be modified by changing the value of "MIN_VALUE_FOR_PROCESSING" in WebCrawler.java

The algorithm then takes the values that met the minimum requirement and combines 
them to form a new elment that is one size larger. These become the candidates that 
will then be checked and verified to see if they exist in the text. The qualifications
to become a candidate are that the suffix of one element must be the prefix of another.  

For example. 
If we have elements 
A 
B
C
D 
then we can create 
AB 
AC
AD 
BC
BD
CD 
this is because we are working with one element sets.
When we work with two element sets for example 
AB and BC would become ABC as AB has suffix B and AB has prefix B. 

Elements are eleminated after they fail to meet the basic requirements of frequency. And 
the data set we work with at first large, becomes very small very quickly. 

I used jsoup as an external library to parse the URl content. 

The data also had to be preprocessed as many websites contain non characters or 
irrelevant words and punctuation. 
To that end, the text was stripped of characters that were not alphanumeric, _, and -. 
Words such as "the", "in", "when", "a" were also removed. I used a list from 
http://www.ranks.nl/stopwords 
to get my stopwords. 

The frequency count was contained in a hashmap data structure for quick insertion and removal. 
ArrayLists<String> were created for each round of candidate creation. 

In the end I was left with a HashMap with unsorted values. I wanted to only return 
the top 10 or so values. This value can be modified in WebCrawler with 
the constant "NUM_VALUES_TO_PRINT". I waited until the end to create a 
TreeMap because a HashMap is much faster in insertion and lookup than a treemap 
O(1) vs. O(logn)
and I only wanted to do the sorting once. 

I created a Comparator class, CustomComparator that implements the Comparator interface 
in order to create my TreeMap and thus only return the necessary values. 
=======
# WebCrawler
Program that finds popular topics on a webpage given a valid URL
