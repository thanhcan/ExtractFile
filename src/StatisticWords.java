import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;


public class StatisticWords {
	
	private static String[] words = new String[31];
	private static long[] statistic = new long[31];
	
	//Tất cả các ký tự có dấu trong tiếng Việt
	static String SPECIAL_CHARACTERS = "àÀảẢãÃáÁạẠăĂằẰẳẲẵẴắẮặẶâÂầẦẩẨẫẪấẤậẬđĐèÈẻẺẽẼéÉẹẸêÊềỀểỂễỄếẾệỆìÌỉỈĩĨíÍịỊòÒỏỎõÕóÓọỌôÔồỒổỔỗỖốỐộỘơƠờỜởỞỡỠớỚợỢùÙủỦũŨúÚụỤưƯừỪửỬữỮứỨựỰỳỲỷỶỹỸýÝỵỴ";
	//Các ký tự bỏ dấu tương ứng
	static String REPLACEMENTS = "aAaAaAaAaAaAaAaAaAaAaAaAaAaAaAaAaAdDeEeEeEeEeEeEeEeEeEeEeEiIiIiIiIiIoOoOoOoOoOoOoOoOoOoOoOoOoOoOoOoOoOuUuUuUuUuUuUuUuUuUuUuUyYyYyYyYyY";

	public StatisticWords() {
		
		int i = 0;
		for (i = 0; i < 30; i++)
		{
			words[i] = null;
			statistic[i] = 0;
		}	
	}
	
	/**
	 * Thống kê số lần xuất hiện của danh từ trong dữ liệu
	 */
	public void Statistic() throws IOException{
		
		File file = new File("junk3.txt");
		FileInputStream fileIn = new FileInputStream(file);
		InputStreamReader inputStream = new InputStreamReader(fileIn);
		BufferedReader bufferRead = new BufferedReader(inputStream);
		
		String line, noun, noun1;
		int st = 0, fn = 0, index = 0, i = 0, j = 0;
		char replace;
		
		//Sử dụng cây nhị phân tìm kiếm để lưu các từ trong dữ liệu
		BST tree = new BST();
		
		long find;
		
		//Duyệt từng dòng của file các từ đã được phân loại
		while ((line = bufferRead.readLine()) != null)
		{
			//Nếu là danh từ
			if ((st = line.indexOf("<w pos=\"N\">")) != -1)
			{
				st += 11;
				fn = line.indexOf("</w>");
				noun = line.substring(st, fn);
				noun = noun.toLowerCase();
				noun = noun.replaceAll("_", " ");
				
				//thêm từ vào cây
				tree.insert(noun);
				//kiểm tra số lần xuất hiện của từ trong dữ liệu
				find = tree.find(noun);
				
				if (checkWord(noun, find) == false)
				{
					//vị trí từ xuất hiện nhiều thứ 30 trong dữ liệu
					index = MinAppear();
					if (find > statistic[index])
					{
						statistic[index] = find;
						words[index] = noun;
					}
				}
			}
		}
		
		//sap xep thu tu xuat hien 30 tu dau tu lon den nho
		Collocation();
		//ghi ra file output
		WriteOutput();
	}
	
	/**
	 * Tìm vị trí từ xuất hiện nhiều thứ 30 trong dữ liệu
	 */
	private static int MinAppear()
	{
		int i, index;
		long min;
		
		min = statistic[0]; index = 0;
		for (i = 1; i < 30; i++)
			if (statistic[i] < min)
			{
				min = statistic[i];
				index = i;
			}
		
		return index;
	}
	
	/**
	 * Kiểm tra xem có phải ký tự có dấu không và trả về ký tự không dấu
	 * @param x: ký tự cần kiểm tra
	 * @return: nếu là ký tự có dấu trả về ký tự đã bỏ dấu, nếu không trả về '$'
	 */
	public static char Check(char x)
	{
		int i = 0;
		char p, q;
		for (i = 0; i < REPLACEMENTS.length(); i++)
		{	
			p = SPECIAL_CHARACTERS.charAt(i);
			q = REPLACEMENTS.charAt(i);
			if (p == x)
				return q;
		}
		return '$';	
	}
	
	private static boolean checkWord(String x, long find)
	{
		int i = 0;
		while (words[i] != null)
		{
			if (words[i].compareTo(x) == 0)
			{
				statistic[i] = find;
				return true;
			}
			i++;
		}
		return false;
	}
	
	/**
	 * Sắp xếp 30 từ xuất hiện nhiều nhất từ nhỏ đến lớn theo số lần xuất hiện
	 */
	private static void Collocation() {
		int i, j;
		long swap;
		String swapStr;
		
		for (i = 0; i < 29; i++)
			for (j = i+1; j < 30; j++)
				if (statistic[i] < statistic[j])
				{
					swap = statistic[i]; statistic[i] = statistic[j]; statistic[j] = swap;
					swapStr = words[i]; words[i] = words[j]; words[j] = swapStr;
				}
	}
	
	private static void WriteOutput() throws IOException {
		
		String noun, outFile;
		int i, j;
		char replace;
		
		//Ten file output
		Date today = new Date(System.currentTimeMillis());
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
		String todayS = dayFormat.format(today.getTime());
		outFile = "ThanhBTT_" + todayS + "_NOUN.tsv"; 
		
		File file = new File(outFile);
		FileOutputStream fileIn = new FileOutputStream(file);
		OutputStreamWriter inputStream = new OutputStreamWriter(fileIn);
		BufferedWriter bufferWrite = new BufferedWriter(inputStream);
		
		for (i = 0; i < 30; i++)
		{
			noun = words[i];
			for (j = 0; j < words[i].length(); j++)
			{
				replace = Check(noun.charAt(j));
				if (replace != '$')
					noun = noun.replace(noun.charAt(j), replace);
			}
			bufferWrite.write(words[i] + "\t" + noun + "\t" + statistic[i] + "\n");	
		}
		
		bufferWrite.close();
	}
}
