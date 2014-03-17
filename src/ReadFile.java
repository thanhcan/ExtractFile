import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;


public class ReadFile {
	
	private static String nameFile;
	
	public ReadFile() {
		nameFile = null;
	}
	
	public String readWrite(String inFile) throws IOException {
	
		int i = 0, st, fn;
		Date today = new Date(System.currentTimeMillis());
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
		String todayS = dayFormat.format(today.getTime());
		//System.out.println("Ngay gio he thong: " + todayS);
		
		String line;
		nameFile = new String("baomoi.content." + todayS + ".txt");
		//output = new String("output.txt");
		
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		BufferedWriter out = new BufferedWriter(new FileWriter(nameFile));
		
		try {
			while ((line = in.readLine()) != null)
			{
				st = line.indexOf("\"content\"") + 11;
				line = line.substring(st, line.length());
				fn = line.indexOf("\"crawl\"") - 2;
				line = line.substring(0, fn) + '\n';
				out.write(line);
				i++;
			}
		}
		catch (IOException e) {
			System.out.println("Read error!");
		}
		
		in.close();
		out.close();
		
		return nameFile;
	}
}
