import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class ExtractFile {
	
	private static String content_file;
	
	private ExtractFile() {
		
		this.content_file = null;
	}
	
	/**
	 * The starting point of the programme.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		/**
		 * Lay du lieu trong the content
		 */
		ReadFile readFile = new ReadFile();
		content_file = readFile.readWrite("baomoi.tsv");
		//content_file = "1.txt";
		
		/**
		 * Tách từ
		 */
		TokenizerFile file = new TokenizerFile();
		file.set_info(content_file, "junk1.txt");
		file.process();
		
		/**
		 * Thay cac ky tu _ bang khoang trang
		 */
		System.out.println("ReplaceChars is starting...");
		ReplaceChars replaceChars = new ReplaceChars();
		replaceChars.read_write_file("junk1.txt", StandardCharsets.UTF_8);
		System.out.println("ReplaceChars is done!!!");
		
		/**
		 * Đánh chỉ mục từ
		 */
		TaggerFile tagger_file = new TaggerFile();
		tagger_file.set_info("junk2.txt", "junk3.txt");
		tagger_file.process();
		
		/**
		 * Thống kê từ
		 */
		System.out.println("\nStatisticWords starting....");
		StatisticWords statistic = new StatisticWords();
		statistic.Statistic();
		System.out.println("StatisticWords is done!!!");
	}
	
}
