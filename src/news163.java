import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class news163 {
	static List<String> pictureURL=new ArrayList<String>();
	static List<String> titleGlobal=new ArrayList<String>();
	static List<String> contentGlobal=new ArrayList<String>();
	//store with one url with quote and one class
	static List<String> urlAndClass=new ArrayList<String>();
	static String SendGet(String url,String encode) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.connect();
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream(),encode));
			String line;
			while ((line = in.readLine()) != null) {
				result += line+'\n';
			}			
		} catch (Exception exc) {
			System.out.println("����GET��������쳣��" + exc);
			exc.printStackTrace();
		}
		// ʹ��finally���ر�������
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}
	//�õ���ͬ����ҳ��
	static void getClass(String str){
		String regex="<ul class=\"clearfix\">[\\s\\S]*?</ul>";
		String div="";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		if(matcher.find()){
			div+=matcher.group().replaceAll("(<ul class=\"clearfix\">[\\s\\S]*?�ټ�|�����Ƽ�[\\s\\S]*?</ul>)", "");
		}
//		System.out.println(div);
		regex="(<a href.*?</a>)";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(div);
		while(matcher.find()){
			urlAndClass.add(matcher.group().replaceAll("(</a|>|<a href=)", ""));
		}		
	}
	//�õ�ĳ��url�ı��뷽ʽ
	static String getEncode(String url){
		String result = "";
		String encode="utf-8";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.connect();
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream(),"utf-8"));
			String line;
			int i=0;
			while ((line = in.readLine()) != null) {
				result += line+'\n';
				i++;
				if(i==20)
					break;
			}
			//System.out.println(result);
			String reg="charset=.*?\"";
			Pattern pat=Pattern.compile(reg);
			Matcher mat=pat.matcher(result);
			while(mat.find()){
				encode=mat.group().replaceAll("(charset=|\")", "");
			}
		} catch (Exception exc) {
			System.out.println("����GET��������쳣��" + exc);
			exc.printStackTrace();
		}
		// ʹ��finally���ر�������
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return encode;
	}
	//��һ��urlAndClass�����ڵ�������ַ�����Ҵ�����Ӧ���ļ�
	static void searchClass() throws IOException{
		String path="http://news.baidu.com";
		String rootPath="D:/Desktop/JAVA����/sp-lab1/WebContent/html/news163/";
		//�洢��Ҫ�������ļ���·��
		String dirPath="";
		//�˱����洢����·���������·��
		String newspath="";
		//���ĵ�������ʽ
    	String reg = "[\u4e00-\u9fa5]";
    	Pattern pat = Pattern.compile(reg);  
    	//��ȡ��������
    	String className=null;
		int i=0;
		while(i<urlAndClass.size()){
			className="";
			newspath=urlAndClass.get(i);
	    	Matcher mat=pat.matcher(newspath); 
	    	while(mat.find()){
	    		className+=mat.group();
	    	}
	    	newspath=mat.replaceAll("");
	    	newspath=newspath.replaceAll("\"", "");
			if(newspath.indexOf("http")==-1){
				newspath=path+newspath;
			}			
			dirPath=rootPath+className+".txt";
			System.out.println(newspath+"--"+className);
			File txt=new File(dirPath);
			if(!txt.exists()){
				txt.createNewFile();
			}
			getContent2(newspath,dirPath);
//			if(i==1)
//				break;
			i++;
		}
		//System.out.println(urlAndClass.get(1));		
	}
	
	//�õ�regex���������
	static String getReg(String input,String regex,String seperator){
		String result="";
		Pattern pat=Pattern.compile(regex);
		Matcher mat=pat.matcher(input);
		while(mat.find()){
			result+=mat.group()+seperator;
		}
		return result;
	}
	
	static String removeATag(String str){
		str=getReg(str,"(href=\".*?\"|>[\\s\\S]*?<)","\r\n");
		str=str.replaceAll("(href=|</a>|<|>)", "");
		return str;
	}
	
	//�õ�ĳ������url�ʹ洢���ļ���Ϣ����href���Ӻͱ��⣬�����Ӧtxt��
	static void getContent2(String newspath,String path) throws IOException{
		String result=SendGet(newspath,getEncode(newspath));
    	FileWriter fw=new FileWriter(path);
    	String reg = "id=\"body\"[\\s\\S]*?id=\"goTop\"";
    	result=getReg(result,reg,"");
    	reg="<a href=\"http[\\s\\S]*?</a>";
    	result=getReg(result,reg,"");
    	fw.write(removeATag(result));
    	fw.close();
	}	
	//updateDB��Ҫ��
	static boolean existChinese(String str){
		String reg = "[\u4e00-\u9fa5]";
    	Pattern pat = Pattern.compile(reg);  
    	Matcher mat = pat.matcher(str);
    	while(mat.find()){
    		return true;
    	}
		return false;
	}
	//�������ݿ�
	public static void updateDB() throws SQLException{
		String url = "jdbc:mysql://localhost:3306/mynews";
		String username = "yyj";
		String pwd = "yang-123";
		Connection conn=null;
		ResultSet res = null;
		Statement stm = null;
		PreparedStatement pst = null;
		String sql;
		String path="D:/Desktop/JAVA����/sp-lab1/WebContent/html/news163/";
		File dir=new File(path);
		File[] files=dir.listFiles();	
		int id=2;
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url,username,pwd);
			stm = conn.createStatement();
			System.out.println("Connect success!");	
		}catch(Exception e){
			e.printStackTrace();
		}		
		for(File file : files){	
			//�õ�����txt�ļ�������
			String url2="";
			String title="";
			String newsClass="";
			int u=0,t=0;
			int count=0;
	        try{
	        	newsClass=getReg(file.getAbsolutePath(),"[\\u4e00-\\u9fa5]*?.txt","");
	        	newsClass=newsClass.replaceAll(".txt", "");
	        	FileReader fr = new FileReader(file.getAbsolutePath());
	            BufferedReader br = new BufferedReader(fr);//����һ��BufferedReader������ȡ�ļ�
	            String s = null;
	            while((s = br.readLine())!=null&&s!="\r\n"){//ʹ��readLine������һ�ζ�һ��
	                if(s.indexOf("\"")!=-1){
	                	url2=s.replaceAll("\"", "");
	                	u=1;
	                }else if(existChinese(s)&&u==1){
	                	title=s;
	                	t=1;
	                }
	                if(t==1){
	                	//System.out.println(url2+"--"+title);
	                	t=0;u=0;
	                	//ÿ��������������Ϣ
	                	if(count>=5){
	                		continue;
	                	}
	                	count++;
	                	try{	
	                		//ɾ�������ݿ�
//	    					sql = "delete from news where id > 1;";
//	    					stm.executeUpdate(sql);
	    					//�����µ���
	    					sql = "insert into news values(?,?,?,?,?,?,?,?);";
	    					pst=conn.prepareStatement(sql);
	    					pst.setInt(1, id);
	    					pst.setString(2, title);
	    					pst.setString(3, "yyj");
	    					pst.setDate(4, new Date(2017-1900,5-1,14));
	    					pst.setString(5, url2);
	    					pst.setString(6, "");//picture
	    					pst.setBoolean(7, false);//star
	    					pst.setString(8, newsClass);//class
	    					pst.executeUpdate();
	    					id++;
	    					//ѡ�����е������
	    					sql = "select * from news where id >?;";
	    					pst=conn.prepareStatement(sql);
	    					pst.setString(1,"1");
	    					res=pst.executeQuery();

	    					while(res.next()){
	    						System.out.println(res.getString("id")+"\t"+res.getString("title")+"\t"+res.getString("class"));
	    					}
	    				}catch (SQLException e){
	    					System.out.println("sql error!");
	    					e.printStackTrace();
	    				}catch (Exception e){
	    					e.printStackTrace();
	    				} 
	                }
	            }
	            br.close();    
	        }catch(Exception e){
	            e.printStackTrace();
	        }	  
				
			
		}
		conn.close();
	}
	
	public static void main(String[] args) throws IOException,SQLException {
		// ���弴�����ʵ�����
		String url=new String("http://news.baidu.com/");
		//��URL�����ļ�����
		getPic getPic=new getPic();
		//�õ�������ַ���
		String result = SendGet(url,getEncode(url));
//		//�������,��ֵ��ȫ�ֱ���urlAndClass��
//		getClass(result);
//		//��urlAndClass�����ڵ�����url����һ��
//		searchClass();
		try{
			updateDB();
		}catch (SQLException e){
			System.out.println("sql error!");
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}

	}
}
