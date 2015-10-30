package cn.edu.whu.sklse.greentrie.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileUtil {
	
	
public static  void saveObject(String dir, String fileName, Object obj) {
		try {
			File d = new File(dir);
			if (!d.exists())
				d.mkdirs();
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(dir + "/" + fileName));
			os.writeObject(obj);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Object readObject(String dir,String fileName) {
		try {
			File d = new File(dir);
			if (!d.exists()) d.mkdirs();
			if (!new File(dir+"/"+fileName).exists()) {
				return null;
			}
			ObjectInputStream oi = new ObjectInputStream(new FileInputStream(dir+"/"+fileName));
			Object s = oi.readObject();
			oi.close();
			return s;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
