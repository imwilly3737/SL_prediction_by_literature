package correlation;

import geneNormalization.GeneIDAndSymbol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class CorrelationBetweenGenes {
	private String gene1,gene2,disease,dataset;
	private String user,pass,url,database,geneCol;
	private Connection conn = null;
	private String disFile;
	private double[] geneX1,geneX2;
	private HashMap<String,ArrayList<String>> dis2CL;
	private boolean geneXOK;
	private double coe,p;
	private GeneIDAndSymbol gias;
	public CorrelationBetweenGenes() throws IOException, ClassNotFoundException, SQLException{
		this("CCLE");
	}
	public CorrelationBetweenGenes(String dataset) throws IOException, ClassNotFoundException, SQLException{
		if (dataset.equals("CCLE") && dataset.equals("NCI60")){
			System.err.println("Wrong database in CorrelationBetween Genes");
			return;
		}
		this.dataset = dataset;
		if (dataset.equals("NCI60"))
			disFile = "../../nci60_RNA__Agilent_mRNA_log2/output/Disease2NCIcellLine.txt";
		else
			disFile = "../../CCLE/ccleCellLine2Disease.txt";
		geneXOK = false;
		gias = new GeneIDAndSymbol();
		loadDisease();
		connectDB();
	}
	public void setEntities(String gene1,String gene2,String disease) throws SQLException{
		geneXOK = false;
		this.gene1= gene1;
		this.gene2= gene2;
		this.disease = disease;
		readGeneX();
	}
	private void loadDisease() throws IOException{
		dis2CL = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(disFile));
		while (br.ready()){
			String[] eTab = br.readLine().split("\t");
			String key = eTab[0];
			ArrayList<String> temp = new ArrayList<>();
			for (int i=1;i<eTab.length;i++){
				temp.add(eTab[i]);
			}
			dis2CL.put(key, temp);
		}
		br.close();
	}
	private void connectDB() throws IOException, ClassNotFoundException, SQLException{
		Class.forName("com.mysql.jdbc.Driver");
		user = "imwilly37";
		pass = "**********";
		if (dataset.equals("NCI60")){
			url = "jdbc:mysql://140.116.---.---/NCI60";
			database = "`RNA_Agilent_mRNA_log2`";
			geneCol = "`Entrez_gene_id`";
		}
		else{
			url = "jdbc:mysql://140.116.---.---/CCLE";
			database = "`CCLE_Expression_2012-09-29`";
			geneCol = "`Description`";
		}
		conn = DriverManager.getConnection(url,user,pass);
	}
	private void readGeneX() throws SQLException {
		if (!dis2CL.containsKey(disease))
			return;
		geneX1 = readGene(gene1,disease);
		geneX2 = readGene(gene2,disease);
		if (geneX1 != null && geneX2 != null)
			geneXOK = true;
	}
	private double[] readGene(String gene,String disease) throws SQLException{
		ArrayList<String> cellLine = dis2CL.get(disease);
		double[] geneX = new double[cellLine.size()];
		for (int i=0;i<cellLine.size();i++)
			geneX[i]=0;
		int rowCount = 0;
		Statement stmt = null;
		stmt = conn.createStatement();
		String sql;
		if (dataset.equals("CCLE"))
			gene = gias.getSymbolbyID(gene);
		if (gene == null)
			return null;
		sql = "SELECT * FROM "+database+" WHERE "+geneCol+" ='"+gene+"'";
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()){
			for (int i=0;i<cellLine.size();i++){
				geneX[i] += rs.getDouble(cellLine.get(i));
			}
			rowCount++;
		}
		rs.close();
		stmt.close();
		if (rowCount == 0)
			return null;
		for (int i = 0;i<cellLine.size();i++)
			geneX[i]/=rowCount;
		return geneX;
	}
	public void spearman(){
		if (!getGeneXOK()){
			coe = 0;
			p = -1;
			return;
		}
		coe = new SpearmansCorrelation().correlation(geneX1, geneX2);
		int n = dis2CL.get(disease).size();
		double t = Math.abs(coe)*Math.sqrt((n-2)/(1-coe*coe));
		if (n>2){
			p = new TDistribution(n-2).cumulativeProbability(t);
			p = 1-p;
		}
		else{
			p=-1;
		}
	}
	public void pearson(){
		if (!getGeneXOK()){
			coe = 0;
			p = -1;
			return;
		}
		coe = new PearsonsCorrelation().correlation(geneX1, geneX2);
		int n = dis2CL.get(disease).size();
		double t = Math.abs(coe)*Math.sqrt((n-2)/(1-coe*coe));
		if (n>2){
		p = new TDistribution(n-2).cumulativeProbability(t);
		p = 1-p;
		}
		else{
			p=-1;
		}
	}
	public boolean getGeneXOK(){
		return geneXOK;
	}
	public int getCellLineCount(){
		return dis2CL.get(disease).size();
	}
	public double getCoe(){
		return coe;
	}
	public double getP(){
		return p;
	}
	public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException{
		CorrelationBetweenGenes cbg = new CorrelationBetweenGenes();
		cbg.setEntities("100","10001","D011471");
		cbg.pearson();
		System.out.println(cbg.getCoe()+" "+cbg.getP());
	}
}
