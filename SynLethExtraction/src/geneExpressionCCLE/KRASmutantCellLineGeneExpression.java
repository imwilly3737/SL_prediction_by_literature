package geneExpressionCCLE;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import object.CosmicMutantGene;

public class KRASmutantCellLineGeneExpression {
	private HashSet<String> KRASmclSet;
	private HashMap<String,ArrayList<String>> CellLineName2ID;
	private String COSMICFile,CellLineFile,CellLineIDFile;
	private ArrayList<Boolean> KRASmArray;
	private ArrayList<String> ColonCellLine;
	private String user,pass,url,database,geneCol;
	private double[] finalGeneX;
	private Connection conn = null;
	public KRASmutantCellLineGeneExpression() throws ClassNotFoundException, SQLException{
		KRASmclSet = new HashSet<>();
		ColonCellLine = new ArrayList<>();
		CellLineName2ID = new HashMap<>();
		COSMICFile = "../../Cosmic/CosmicMutantExport.tsv";
		CellLineFile = "../../CCLE/ccleCellLine2Disease.txt";
		CellLineIDFile = "../../NERsuite/cell_line_dictionary_resources.txt";
		connectDB();
	}
	private void connectDB() throws ClassNotFoundException, SQLException{
		Class.forName("com.mysql.jdbc.Driver");
		user = "imwilly37";
		pass = "**********";
		url = "jdbc:mysql://140.116.---.---/CCLE";
		database = "`CCLE_Expression_2012-09-29`";
		geneCol = "`Description`";
		conn = DriverManager.getConnection(url,user,pass);
	}
	public void getKRASmutantCellLine() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(COSMICFile));
		while(br.ready()){
			CosmicMutantGene cmg = new CosmicMutantGene(br.readLine(),"\t");
			if (cmg.Gene_name.equals("KRAS")){
				KRASmclSet.add(cmg.ID_sample);
			}
		}
		br.close();
	}
	public void getCellLineName2ID() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(CellLineIDFile));
		String CCLEid=null,CCLEname=null;
		while(br.ready()){
			String[] eTab = br.readLine().split("\t");
			if (eTab[1].equals("CCLE") && eTab[2].contains("_LARGE_INTESTINE")){
				CCLEid = eTab[0];
				CCLEname = eTab[2];
				CellLineName2ID.put(CCLEname, new ArrayList<String>());
			}
			if (eTab[1].equals("Cosmic") && eTab[0].equals(CCLEid)){
				CellLineName2ID.get(CCLEname).add(eTab[2]);
			}
				
		}
		br.close();
	}
	public void getColonCancerCellLine() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(CellLineFile));
		while (br.ready()){
			String[] eTab = br.readLine().split("\t");
			String key = eTab[0];
			if (key.equals("D003110")){
				KRASmArray = new ArrayList<>();
				for (int i=1;i<eTab.length;i++){
					boolean mutantKRAS = false;
					ColonCellLine.add(eTab[i]);
					if (CellLineName2ID.containsKey(eTab[i]))
						for (String e:CellLineName2ID.get(eTab[i])){
							if (KRASmclSet.contains(e))
								mutantKRAS = true;
							break;
						}
					else
						System.out.println(eTab[i]+ "is an unknown cell line");
					System.out.println(mutantKRAS);
					KRASmArray.add(mutantKRAS);
				}
			}
		}
		br.close();
	}
	private void readCD82FromColonCancer() throws SQLException{
		double[] geneX = new double[ColonCellLine.size()];
		for (int i=0;i<ColonCellLine.size();i++)
			geneX[i]=0;
		int rowCount = 0;
		Statement stmt = null;
		stmt = conn.createStatement();
		String sql;
		sql = "SELECT * FROM "+database+" WHERE "+geneCol+" ='CD82'";
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()){
			for (int i=0;i<ColonCellLine.size();i++){
				geneX[i] += rs.getDouble(ColonCellLine.get(i));
			}
			rowCount++;
		}
		rs.close();
		stmt.close();
		if (rowCount == 0)
			return;
		for (int i = 0;i<ColonCellLine.size();i++)
			geneX[i]/=rowCount;
		finalGeneX = geneX;
	}
	public void printGeneExpression(){
		System.out.println("Mutant on KRAS:");
		for (int i=0;i<ColonCellLine.size();i++){
			if (KRASmArray.get(i)){
				System.out.println(ColonCellLine.get(i)+"\t"+finalGeneX[i]);
			}
		}
		System.out.println("Not Mutant on KRAS:");
		for (int i=0;i<ColonCellLine.size();i++){
			if (!KRASmArray.get(i)){
				System.out.println(ColonCellLine.get(i)+"\t"+finalGeneX[i]);
			}
		}
	}
	static public void main (String[] args) throws IOException, SQLException, ClassNotFoundException{
		KRASmutantCellLineGeneExpression kmclge = new KRASmutantCellLineGeneExpression();
		kmclge.getKRASmutantCellLine();
		kmclge.getCellLineName2ID();
		kmclge.getColonCancerCellLine();
		kmclge.readCD82FromColonCancer();
		kmclge.printGeneExpression();
	}
}

