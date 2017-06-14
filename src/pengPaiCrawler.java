import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.File;
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

public class pengPaiCrawler {
	static List<String> pictureURL=new ArrayList<String>();
	static List<String> titleGlobal=new ArrayList<String>();
	static List<String> contentGlobal=new ArrayList<String>();
	static String SendGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.connect();
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream(),"utf-8"));
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
	//�õ���ҳ��ı���
	static List<String> getTitle(String str){
		String regex="<a target=\"_blank\" href=\"newsDetail(.*)</a>";
		List<String> title=new ArrayList<String>();  
		Pattern pattern = Pattern.compile(regex);
		// ����һ��matcher������ƥ��
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			title.add(removeATag(matcher.group()));
		}
		titleGlobal=title;
		return title;
	}
	
	static String removeATag(String str){
		String tmp;
		tmp=str.replaceAll("<a target(.*)\">", "");
		tmp=tmp.replaceAll("</a>", "<br>");
		return tmp;
	}
	//�����ҳ�����ϸ��ҳ����Ϣ
	static List<String> getHref(String str){
		String regex="<a target=\"_blank\" href=\"newsDetail(.*)\"";
		List<String> href=new ArrayList<String>();  
		Pattern pattern = Pattern.compile(regex);
		// ����һ��matcher������ƥ��
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			href.add((matcher.group().replaceAll("\"", "")).replaceAll("<a(.*)href=", "http://www.thepaper.cn/"));
		}
		return href;
	}
	//��þ���ҳ�������
	static List<String> getDetail(String result){
		List<String> href=new ArrayList<String>();
		List<String> detail=new ArrayList<String>();
		href=getHref(result);
		for(int i=0;i<10;i++){
			//���ž�������ҳ���ȫ���ַ������
			String detailResult=SendGet(href.get(i));
			String content=getContent(detailResult);
			detail.add(content.replaceAll("<[^b].*?>", ""));	
			pictureURL.add(getImgUrl(content));
		}
		contentGlobal=detail;
		return detail;
	}
	//�õ������ŵ�ͼƬurl
	static String getImgUrl(String str){
		String regex="<img.*?>";
		String imgUrl="";  
		Pattern pattern = Pattern.compile(regex);
		// ����һ��matcher������ƥ��
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			//��ʾ����b��ͷ�ı�ǩ�����ƥ�䶼����
			imgUrl=matcher.group();
			imgUrl=imgUrl.replaceAll("<img.*?src=\"", "");
			imgUrl=imgUrl.replaceAll("\".*>", "");
			//System.out.println(imgUrl);
			break;
		}		
		return imgUrl;
	}
	//�õ���ҳ���������Ҫ��������
	static String getContent(String str){
		String regex="<div class=\"news_txt\"(.*<div.*</div>)*";
		String content="";  
		Pattern pattern = Pattern.compile(regex);
		// ����һ��matcher������ƥ��
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			content+=matcher.group();
		}
		return content;
	}
	
	public static void updateDB() throws SQLException{
		String url = "jdbc:mysql://localhost:3306/mynews";
		String username = "yyj";
		String pwd = "yang-123";
		Connection conn=null;
		ResultSet res = null;
		Statement stm = null;
		PreparedStatement pst = null;
		String sql;
		String path="D:\\Desktop\\JAVA����\\sp-lab1\\WebContent\\html\\details\\";
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url,username,pwd);
			stm = conn.createStatement();
			System.out.println("Connect success!");	
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(int i=0;i<10;i++){			
			String picFile=path+"picture"+i+".jpg";
			File pic=new File(picFile);
			try{	
				//�����µ���
				sql = "insert into news values(?,?,?,?,?,?,?,?);";
				pst=conn.prepareStatement(sql);
				pst.setInt(1, 2);
				pst.setString(2, titleGlobal.get(i));
				pst.setString(3, "yyj");
				pst.setDate(4, new Date(2017-1900,5-1,14));
				pst.setString(5, contentGlobal.get(i));
				if(pic.exists())
					pst.setString(6, "picture"+i+".jpg");
				else
					pst.setString(6, "");
				pst.setBoolean(7, false);
				pst.setString(8, "����");
				pst.executeUpdate();
				//ѡ�����е������
				sql = "select * from news where id >?;";
				pst=conn.prepareStatement(sql);
				pst.setString(1,"1");
				res=pst.executeQuery();
				sql = "delete from news where id > 1;";
				stm.executeUpdate(sql);
				while(res.next()){
					System.out.println(res.getString("id")+"\t"+res.getString("title")+"\t"+res.getString("author")
					+"\t"+res.getDate("date")+"\t"+res.getString("picture"));
				}
			}catch (SQLException e){
				System.out.println("sql error!");
				e.printStackTrace();
			}catch (Exception e){
				e.printStackTrace();
			} 
		}
		conn.close();
	}
	
	public static void main(String[] args) throws IOException,SQLException {
		// ���弴�����ʵ�����
		String url=new String("http://www.thepaper.cn/");
		//�����ļ����������ҳ������б���
		FileWriter fw = new FileWriter("D:\\Desktop\\JAVA����\\sp-lab1\\WebContent\\html\\details\\titles.txt");	
		//��URL�����ļ�����
		getPic getPic=new getPic();
		//�õ�������ַ���
		String result = SendGet(url);
		//���ű���
		List<String> title= getTitle(result);
		//���ž�������
		List<String> detail=getDetail(result);
//		try{
//			updateDB();
//		}catch (SQLException e){
//			System.out.println("sql error!");
//			e.printStackTrace();
//		}catch (Exception e){
//			e.printStackTrace();
//		}
		for(int i=0;i<10;i++){
			//�����ļ����������ҳ������ݣ���0-9���
			FileWriter fw2 = new FileWriter("D:\\Desktop\\JAVA����\\sp-lab1\\WebContent\\html\\details\\newsContent"+i+".txt");
			fw.write(title.get(i));	
			fw2.write(detail.get(i));
			fw2.close();
			try{
				String picName = "picture" + i + ".jpg";
				File pic=new File("D:\\Desktop\\JAVA����\\sp-lab1\\WebContent\\html\\details\\"+picName);
				//���ԭ����ͼƬ����ɾ��
				if(pic.exists()){
					pic.delete();
				}
				//�����ͼƬ������һ��ͼƬ
				if(pictureURL.get(i)!=""){
					getPic.downLoadFromUrl(pictureURL.get(i), picName, "D:\\Desktop\\JAVA����\\sp-lab1\\WebContent\\html\\details");
				}
			}catch(Exception exc){
				System.out.println(exc);
			}
		}
		fw.close();			
	}
}
