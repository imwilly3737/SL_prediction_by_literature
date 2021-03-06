package geneNormalization;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GeneIDAndSymbol {
	private String user;
	private String pass;
	private String url;
	private Connection conn;
	private boolean printFlag;
	public GeneIDAndSymbol() throws ClassNotFoundException, SQLException{
		this(false);
	}
	public GeneIDAndSymbol(boolean printFlag) throws ClassNotFoundException, SQLException{
		
		Class.forName("com.mysql.jdbc.Driver");
		System.out.println("Connecting to database...");
		user = "ytwong";
		pass = "**********";
		url = "jdbc:mysql://140.116.---.---/ytwong";
		conn = DriverManager.getConnection(url,user,pass);
		this.printFlag = printFlag;
	}
	public String getIDbySymbol(String symbol) throws SQLException{
		Statement stmt = null;
		stmt = conn.createStatement();
		String sql;
		sql = "SELECT GeneID FROM CTD_Vocabulary_Gene WHERE GeneSymbol ='"+symbol+"'";
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()){
			String id = rs.getString("GeneID");
			rs.close();
			stmt.close();
			return id;
		}
		rs.close();
		stmt.close();
		if (printFlag)
			System.err.println("No gene symbol: "+symbol);
		return null;
	}
	public String getSymbolbyID(String id) throws SQLException{
		Statement stmt = null;
		stmt = conn.createStatement();
		String sql;
		sql = "SELECT GeneSymbol FROM CTD_Vocabulary_Gene WHERE GeneID ='"+id+"'";
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()){
			String symbol = rs.getString("GeneSymbol");
			rs.close();
			stmt.close();
			return symbol;
		}
		rs.close();
		stmt.close();
		if (printFlag)
			System.err.println("No gene id: "+id);
		return null;
	}
}
