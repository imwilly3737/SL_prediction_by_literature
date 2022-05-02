### Overview
Using a self-build and deprecated database to normalize gene symbols and calculate the gene expression.  
There are 2 java files in this folder.  
Requried 2 additional referenced java libraries: commons-math3-3.0.jar and mysql-connector-java-5.1.38-bin.jar
### correlationBetweenGenes
#### CorrelationBetweenGenes.java
Executing main function in this file will print out the coefficient and p-value of the relation between two given genes on the specific disease from CCLE database.  
Supporting both spearman and pearson

``` java
public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException{
		CorrelationBetweenGenes cbg = new CorrelationBetweenGenes();
		cbg.setEntities("100","10001","ALL");  // gene 1 id, gene 2 id, disease id
		cbg.spearman();
		if (cbg.getGeneXOK())
			System.out.println("Coeffiecient: "+cbg.getCoe()+"\np-value: "+cbg.getP());
		else
			System.out.println("Cannot compute the correlation between two genes!\nBecause "+cbg.getErrMessage());
	}
```

### geneNormalization
#### GeneIDAndSymbol.java
Using by above java file. Supporting two major function:
1. getIDbySymbol: Can get the NCBI gene id (3732) by a given gene symbol (CD82)
2. getSymbolbyID: Can get the Symbol (CD82) by a given NCBI gene id (3732)
