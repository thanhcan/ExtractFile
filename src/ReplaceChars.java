import java.io.*;
import java.nio.charset.Charset;

public class ReplaceChars {
	
	public ReplaceChars() {
		
	}
	
	public void read_write_file (String fileName, Charset charSet) throws IOException {
		
		File file1 = new File(fileName);
		File file2 = new File("junk2.txt");
		
		FileInputStream fileIn = new FileInputStream(file1);
		InputStreamReader inputStream = new InputStreamReader(fileIn, charSet);
		BufferedReader bufferRead = new BufferedReader(inputStream);
		
		FileOutputStream fileOut = new FileOutputStream(file2);
		OutputStreamWriter outputStream = new OutputStreamWriter(fileOut, charSet);
		BufferedWriter bufferWrite = new BufferedWriter(outputStream);
		
		String line = null, ln;
		
		while ((line = bufferRead.readLine()) != null)
		{
			line = line.replaceAll("\\\\ n", "");
			line = line.replace('\\', ' ');
			line = line.replace('_', ' ');
			bufferWrite.write(line + "\n");
		}
		
		bufferRead.close();
		bufferWrite.close();
	}
}



