### Overview
We mine and extract the essentail trigger terms in this file.  
Also, required reference java libraries are:
1. abner.jar
2. commons-math3-3.0.jar
3. dom4j-2.0.0-ALPHA-2.jar
4. jwnl-1.3.3.jar
5. log4j-1.2.17.jar
6. maxent-3.0.0.jar
7. mysql-connector-java-5.1.38-bin.jar
8. opennlp-tools-1.5.0.jar
9. slf4j-api-1.7.18.jar
10. slf4j-log4j12-1.7.18.jar
11. slf4j-simple-1.7.18.jar
12. stanford-english-corenlp-2016-01-10-models.jar
13. stanford-parser.jar

#### EssentialGeneTriggerTerm.java
In this java file, we extract essential trigger terms by providing a disease id (or providing null for all disease).

#### EssentialGeneTriggerTerm.java
In this java file, we rank these extracted trigger terms with a score.  
Detail formula for the score is in the paper. Higher score means the terms occurred more often on essential gene than non-essential gene.
