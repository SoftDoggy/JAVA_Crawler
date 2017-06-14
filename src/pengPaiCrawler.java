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
			System.out.println("发送GET请求出现异常！" + exc);
			exc.printStackTrace();
		}
		// 使用finally来关闭输入流
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
	//得到子页面的标题
	static List<String> getTitle(String str){
		String regex="<a target=\"_blank\" href=\"newsDetail(.*)</a>";
		List<String> title=new ArrayList<String>();  
		Pattern pattern = Pattern.compile(regex);
		// 定义一个matcher用来做匹配
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
	//获得主页面的详细子页面信息
	static List<String> getHref(String str){
		String regex="<a target=\"_blank\" href=\"newsDetail(.*)\"";
		List<String> href=new ArrayList<String>();  
		Pattern pattern = Pattern.compile(regex);
		// 定义一个matcher用来做匹配
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			href.add((matcher.group().replaceAll("\"", "")).replaceAll("<a(.*)href=", "http://www.thepaper.cn/"));
		}
		return href;
	}
	//获得具体页面的内容
	static List<String> getDetail(String result){
		List<String> href=new ArrayList<String>();
		List<String> detail=new ArrayList<String>();
		href=getHref(result);
		for(int i=0;i<10;i++){
			//新闻具体内容页面的全部字符串结果
			String detailResult=SendGet(href.get(i));
			String content=getContent(detailResult);
			detail.add(content.replaceAll("<[^b].*?>", ""));	
			pictureURL.add(getImgUrl(content));
		}
		contentGlobal=detail;
		return detail;
	}
	//得到子新闻的图片url
	static String getImgUrl(String str){
		String regex="<img.*?>";
		String imgUrl="";  
		Pattern pattern = Pattern.compile(regex);
		// 定义一个matcher用来做匹配
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			//表示不是b开头的标签的最短匹配都代替
			imgUrl=matcher.group();
			imgUrl=imgUrl.replaceAll("<img.*?src=\"", "");
			imgUrl=imgUrl.replaceAll("\".*>", "");
			//System.out.println(imgUrl);
			break;
		}		
		return imgUrl;
	}
	//得到子页面里面的主要新闻内容
	static String getContent(String str){
		String regex="<div class=\"news_txt\"(.*<div.*</div>)*";
		String content="";  
		Pattern pattern = Pattern.compile(regex);
		// 定义一个matcher用来做匹配
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
		String path="D:\\Desktop\\JAVA程序\\sp-lab1\\WebContent\\html\\details\\";
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
				//插入新的行
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
				pst.setString(8, "娱乐");
				pst.executeUpdate();
				//选择所有的行输出
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
		// 定义即将访问的链接
		String url=new String("http://www.thepaper.cn/");
		//创建文件用来存放子页面的所有标题
		FileWriter fw = new FileWriter("D:\\Desktop\\JAVA程序\\sp-lab1\\WebContent\\html\\details\\titles.txt");	
		//从URL下载文件的类
		getPic getPic=new getPic();
		//得到结果的字符串
		String result = SendGet(url);
		//新闻标题
		List<String> title= getTitle(result);
		//新闻具体内容
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
			//创建文件用来存放子页面的内容，以0-9编号
			FileWriter fw2 = new FileWriter("D:\\Desktop\\JAVA程序\\sp-lab1\\WebContent\\html\\details\\newsContent"+i+".txt");
			fw.write(title.get(i));	
			fw2.write(detail.get(i));
			fw2.close();
			try{
				String picName = "picture" + i + ".jpg";
				File pic=new File("D:\\Desktop\\JAVA程序\\sp-lab1\\WebContent\\html\\details\\"+picName);
				//如果原来有图片，先删除
				if(pic.exists()){
					pic.delete();
				}
				//如果有图片则下载一张图片
				if(pictureURL.get(i)!=""){
					getPic.downLoadFromUrl(pictureURL.get(i), picName, "D:\\Desktop\\JAVA程序\\sp-lab1\\WebContent\\html\\details");
				}
			}catch(Exception exc){
				System.out.println(exc);
			}
		}
		fw.close();			
	}
}
