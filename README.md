# SL_prediction_by_literature
**Literature-based translation from synthetic lethality screening into therapeutics targets: CD82 is a novel target for KRAS mutation in colon cancer**  
We introduced a different approach to prioritize synthetic lethality (SL) gene pairs through literature mining and RAS -mutant high-throughput screening (HTS) data. We matched essential genes from text mining and mutant genes from the COSMIC and CCLE HTS data to build a prediction model of SL gene pairs. CCLE gene expression data were used to enrich the essential-mutant SL gene pairs using Spearman’s correlation coefficient and literature mining. Using RAS -mutant HTS data validation, we identified two potential SL gene pairs, including the CD82 (essential gene)– KRAS (mutant gene) pair and CD82 – NRAS pair in the DLD-1 colon cancer cell line (Spearman correlation p- values = 0.004786 and 0.00249, respectively). Based on further annotations by PubChem, we observed that digitonin targeted the complex comprising CD82 . Moreover, we experimentally demonstrated that CD82 exhibited selective vulnerability in KRAS -mutant colorectal cancer. We identify CD82 as a novel target for RAS- mutant colon cancer.

# IDE and Environment
In this research, we use Microsoft Windows 10, Eclipse and JavaSE-1.8 to build all these codes.  
We listed all needed external libraries in each subdirectory.

# Sub-project (Sub-directory)
There are 2 sub-projects (sub-directories) in this project.  
The main prediction logic and the methodology for downloading artical abstracts are all in **SynLethExtraction**.

## CorrelationBetweenGenes
A small subporect to calculate co-expression between 2 genes.  
Using a self-build and deprecated database to normalize gene symbols and calculate the gene expression.  
See details in the Readme.md in the subfolder.

## SynLethExtraction
A subproject to download abstracts from Pubmed and extract potential synthetic lethality gene pairs.  
Using Pubmed Entrez Programming Utilities (E-utilities) to download abstracts with a specific queries.  
And using Pubtator to select the useful abstracts in this research.  
Finally, excuting some processes to extract these potential synthetic lethality gene pairs.  
See details in the Readme.md in the subfolder
