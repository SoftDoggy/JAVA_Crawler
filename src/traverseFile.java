import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class traverseFile {
	static boolean existChinese(String str){
		String reg = "[\u4e00-\u9fa5]";
    	Pattern pat = Pattern.compile(reg);  
    	Matcher mat = pat.matcher(str);
    	while(mat.find()){
    		return true;
    	}
		return false;
	}
	//得到regex里面的内容
	static String getReg(String input,String regex,String seperator){
		String result="";
		Pattern pat=Pattern.compile(regex);
		Matcher mat=pat.matcher(input);
		while(mat.find()){
			result+=mat.group()+seperator;
		}
		return result;
	}
	public static void main(String[] args) {
		String path="D:/Desktop/JAVA程序/sp-lab1/WebContent/html/news163/";
		File dir=new File(path);
		File[] files=dir.listFiles();	
		int count=0;
		for(File file : files){				
			String url2="";
			String title="";
			String newsClass="";
			int u=0,t=0;
	        try{
	        	newsClass=getReg(file.getAbsolutePath(),"[\\u4e00-\\u9fa5]*?.txt","");
	        	newsClass=newsClass.replaceAll(".txt", "");
	        	FileReader fr = new FileReader(file.getAbsolutePath());
	            BufferedReader br = new BufferedReader(fr);//构造一个BufferedReader类来读取文件
	            String s = null;
	            while((s = br.readLine())!=null&&s!="\r\n"){//使用readLine方法，一次读一行
	                if(s.indexOf("\"")!=-1){
	                	url2=s.replaceAll("\"", "");
	                	u=1;
	                }else if(existChinese(s)&&u==1){
	                	title=s;
	                	t=1;
	                }
	                if(t==1){
	                	System.out.println(count+url2+"--"+title+"--"+newsClass);
	                	t=0;u=0;
	                	count++;
	                }
	            }
	            br.close();    
	        }catch(Exception e){
	            e.printStackTrace();
	        }	        
		}
	}
}
