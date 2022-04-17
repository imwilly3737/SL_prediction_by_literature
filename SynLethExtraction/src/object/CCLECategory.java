package object;

public class CCLECategory {
	public String id;
	public String resource;
	public String name;
	public String cancer;
	public String cancerid;
	public CCLECategory(){
		
	}
	public CCLECategory(String id,String resource,String name,String cancer,String cancerid){
		this.id = id;
		this.resource = resource;
		this.name = name;
		this.cancer = cancer;
		this.cancerid = cancerid;
	}
}
