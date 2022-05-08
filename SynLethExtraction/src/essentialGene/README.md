### Overview
We rank our potential essential genes and trigger terms in this package.  
Beforce using this package, we need to process PubMed abstract data in src/pubmed package and extract trigger terms in src/triggerTermMining.  
Also, required reference java libraries are:
1. abner.jar
2. commons-math3-3.0.jar
3. dom4j-1.6.1.jar
4. dom4j-2.0.0-ALPHA-2.jar
5. jaxen-1.1.1.jar
6. jwnl-1.3.3.jar
7. log4j-1.2.17.jar
8. maxent-3.0.0.jar
9. mysql-connector-java-5.1.38-bin.jar
10. opennlp-tools-1.5.0.jar
11. slf4j-api-1.7.18.jar
12. slf4j-log4j12-1.7.18.jar
13. slf4j-simple-1.7.18.jar
14. stanford-english-corenlp-2016-01-10-models.jar
15. stanford-parser.jar

#### EssentialGenePatternSentence.java
In this java file, we extract the sentences including at least one disease and at least one gene.  
In default, we use 4 threads to process all abstracts.

#### EssentialTriggerTermMultiCancer.java
In this java file, we rank all essential trigger terms by those given trigger term dataset.

#### EvaluationEssentialGene.java
In this java file, we evaluate our trigger terms and calculate the false positive and true positive by compare with COLT-Cancer dataset.

#### ExtractGeneFromTrigger.java
In this java file, we extract potential essential genes from pubmed abstracts with essential trigger terms.

#### ExtractSentencesFromGeneAndDisease.java
In this java file, we extract the sentences with a given disease id and another given gene id.  
We use this java code to analysis the text data, so there is no restriction between gene id and disease id in this file.
