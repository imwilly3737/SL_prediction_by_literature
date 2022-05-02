### Overview
There are totally 13 java packages in this folder.  
All log files and intermediate processing files are removed. If you execute the code yourself, some new files will be generated in this folder.  
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
Below is descriptions of all packages.

### correlation
Calculate the correlation coeffient between two genes by two given genes on the specific disease from CCLE database.  
Details in another folder SL_prediction_by_literature/CorrelationBetweenGenes.
### cosmic
Print the Primary_site of the given Cosmic database.  
### essentialGene
**One of the major packages**  
Extract essential genes with a given trigger term dataset.  
See details in the REAMDME of the package (in the subfolder)
### evaluation
Evaluate and filter our potential synthetic lathality pairs with correlation.
### geneExpressionCCLE
Process CCLE database and print the gene expression on specific cell line.
### geneNormalization
Normalize gene by converting between gene NCBI id (3732) and gene symbol (CD82).  
Details in another folder SL_prediction_by_literature/CorrelationBetweenGenes.
### mutantGene
Collect and process the mutant genes in cosmic dataset.
### negex
Third party java project implement Wendy Chapman's NegEx algorithm to identify negated relationship in abstract.  
https://github.com/chapmanbe/negex/tree/master/GeneralNegEx.Java.v.1.2.05092009
### object
Define some basic class objects used in other projects.
### pairRanking
Rank the pairs by cooccurence within all abstracts.
### pubmed
**One of the major packages**  
Download and process abstracts from Pubmed.  
See details in the README of the package (in the subfolder).
### syntheticLethal
**One of the major packages**  
Extract potential synthetic lethality pair from our extracted essential genes.  
See details in the README of the package (in the subfolder).
### triggerTermMining
**One of the major packages**  
Extract and rank the trigger terms from Pubmed.  
See details in the README of the package (in the subfolder).
