import java.io.File;
import java.io.Reader;
import java.util.Properties;
import java.io.InputStreamReader;
import java.io.FileInputStream;

public class FileExample {

	public static void main(String[] args) {
		File file=new File("config/site.properties");
		Properties p=new Properties();
		
		
		 Reader reader = null;
	        try {
	        	p.load(new FileInputStream(file));
	        	System.out.println(p);
	        	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	}
	
	

}
