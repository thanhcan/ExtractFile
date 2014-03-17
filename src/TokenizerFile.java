import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.sd.SentenceDetectorFactory;
import vn.hus.nlp.tokenizer.Tokenizer;
import vn.hus.nlp.tokenizer.TokenizerOptions;
import vn.hus.nlp.tokenizer.TokenizerProvider;
import vn.hus.nlp.tokenizer.VietTokenizer;
import vn.hus.nlp.tokenizer.nio.XMLCorpusExporter;
import vn.hus.nlp.tokenizer.tokens.TaggedWord;
import vn.hus.nlp.utils.FileIterator;
import vn.hus.nlp.utils.TextFileFilter;
import vn.hus.nlp.utils.UTF8FileUtility;


public class TokenizerFile {
	
	private static Tokenizer tokenizer = null;
	private static SentenceDetector sentenceDetector = null;
	private static boolean DEBUG = false;
	
	/**
	 * Number of tokens procesed
	 */
	private static int nTokens = 0;
	
	/**
	 * input, output file and option process
	 */
	private String in_file, out_file, op_process1, op_process2;
	
	/**
	 * Default constructor
	 */
	public TokenizerFile() {
		tokenizer = TokenizerProvider.getInstance().getTokenizer();
		
		this.in_file = null;
		this.out_file = null;
		this.op_process1 = null;
		this.op_process2 = null;
	}
	
	/**
	 * Creates a sentence detector.
	 */
	private static void createSentenceDetector() {
		if (sentenceDetector == null) {
			sentenceDetector = SentenceDetectorFactory.create("vietnamese");
		}
	}
	
	/**
	 * A segment method, written for integration with other tools. This method was asked 
	 * to add by Dr. Le Anh Cuong, VNU, Hanoi. 
	 * @param sentence a sentence to be segmented
	 * @return a segmented sentence
	 */
	public String segment(String sentence) {
		StringBuffer result = new StringBuffer(1000);
		StringReader reader = new StringReader(sentence);
		// tokenize the sentence
		try {
			tokenizer.tokenize(reader);
			List<TaggedWord> list = tokenizer.getResult();
			for (TaggedWord taggedWord : list) {
				String word = taggedWord.toString();
				if (TokenizerOptions.USE_UNDERSCORE) {
					word = word.replaceAll("\\s+", "_");
				} else {
					word = "[" + word + "]";
				}
				result.append(word);
				result.append(' ');
			}
			// update nTokens
			nTokens += list.size();
			// 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString().trim();
	}
	
	/**
	 * Tokenizes a text. If the option "use sentence detector" is on, 
	 * the sentence detector is called to segment the text into sentences; then 
	 * the tokenizer is used to tokenize detected sentences. If the option 
	 * is off, the text is directly tokenized by the tokenizer.  
	 * @param text a text to tokenize.
	 * @return an array of tokenized sentences.
	 * @see TokenizerOptions
	 * @see Tokenizer
	 * @see SentenceDetector
	 */
	public String[] tokenize(String text) {
		List<String> result = new ArrayList<String>();
		StringReader reader = new StringReader(text);
		if (TokenizerOptions.USE_SENTENCE_DETECTOR) {
			// use the sentence detector
			createSentenceDetector();
			try {
				String[] sentences = sentenceDetector.detectSentences(reader);
				for (String sentence : sentences) {
					// segment the sentence
					result.add(segment(sentence));
					// add an empty line
					result.add("\n\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// process all the text without detecting sentence
			result.add(segment(text));
		}
		// return the result
		return result.toArray(new String[result.size()]);
	}
	
	/**
	 * Turns the sentence detector on.
	 */
	public void turnOnSentenceDetection() {
		TokenizerOptions.USE_SENTENCE_DETECTOR = true;
	}
	
	/**
	 * Turns the sentence detector off.
	 */
	public void turnOffSentenceDetection() {
		TokenizerOptions.USE_SENTENCE_DETECTOR = false;
	}
	
	
	/**
	 * Tokenizes an input file and write result to an output file.
	 * These files are text files with UTF-8 encoding.
	 * @param inputFile input file
	 * @param outputFile output file
	 */
	public void tokenize2(String inputFile, String outputFile) {
		tokenizer.tokenize(inputFile);
		tokenizer.exportResult(outputFile);
	}
	
	/**
	 * Tokenizes an input file and write result to an output file.
	 * These files are text files with UTF-8 encoding.
	 * @param inputFile input file
	 * @param outputFile output file
	 */
	public void tokenize(String inputFile, String outputFile) {
		UTF8FileUtility.createWriter(outputFile);
		String[] paragraphs = UTF8FileUtility.getLines(inputFile);
		
		if (!TokenizerOptions.XML_OUTPUT) {
			for (String p : paragraphs) {
				String[] sentences = tokenize(p);
				for (String s : sentences) {
					UTF8FileUtility.write(s.trim());
					UTF8FileUtility.write("\n");
				}
			}
		} else { // XML outputer
			List<List<TaggedWord>> list = new ArrayList<List<TaggedWord>>();
			for (String p : paragraphs) {
				try {
					tokenizer.tokenize(new StringReader(p));
				} catch (IOException e) {
					e.printStackTrace();
				}
				// make a copy of the result of tokenization
				List<TaggedWord> result = new ArrayList<TaggedWord>(tokenizer.getResult());
				list.add(result);
				nTokens += result.size();
			}
			String output = new XMLCorpusExporter().export(list);
			UTF8FileUtility.write(output);
		}
		UTF8FileUtility.closeWriter();
	}
	

	/**
	 * Tokenizes all files in a directory.
	 * @param inputDir an input dir
	 * @param outputDir an output dir
	 */
	public void tokenizeDirectory(String inputDir, String outputDir) {
		TextFileFilter fileFilter = new TextFileFilter(TokenizerOptions.TEXT_FILE_EXTENSION);
		File inputDirFile = new File(inputDir);
		// get the current dir 
		String currentDir = new File(".").getAbsolutePath();
		String inputDirPath = currentDir + File.separator + inputDir;
		String outputDirPath = currentDir + File.separator + outputDir;

		if (DEBUG) {
			System.out.println("currentDir = " + currentDir);
			System.out.println("inputDirPath = " + inputDirPath);
			System.out.println("outputDirPath = " + outputDirPath);
		}
		
		// get all input files
		File[] inputFiles = FileIterator.listFiles(inputDirFile, fileFilter);
		System.out.println("Tokenizing all files in the directory, please wait...");
		long startTime = System.currentTimeMillis();
		for (File aFile : inputFiles) {
			// get the simple name of the file
			String input = aFile.getName();
			// the output file have the same name with the automatic file 
			String output = outputDirPath + File.separator + input;
			// tokenize the file
			tokenize(aFile.getAbsolutePath(), output);
		}
		long endTime = System.currentTimeMillis();
		float duration = (float) (endTime - startTime) / 1000;
		System.out.println("Tokenized " + nTokens  + " words of " + inputFiles.length + " files in " + duration + " (s).\n");
	}
	
	
	/**
	 * @return the tokenizer
	 */
	public static Tokenizer getTokenizer() {
		return tokenizer;
	}
	
	public void set_info(String in_file, String out_file)
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try{
			this.in_file = in_file;
			this.out_file = out_file;
			this.op_process1 = "-sd";
			this.op_process2= "";
			/*System.out.println("Nhap cac tuy chon tach tu");
			System.out.print("Tùy chọn 1: ");
			this.op_process1 = in.readLine();
			System.out.print("Tùy chọn 2: ");
			this.op_process2 = in.readLine();*/
		} catch (Exception e){
				System.out.println("Nhập lỗi!");
			}
	}
	
	public void process() {
		Options options = new Options();

		// add options
		options.addOption("nu", false, "Use spaces for separating syllables of words (no underscores)");
		options.addOption("sd", false, "Use sentence detection before tokenizing");
		options.addOption("xo", false, "Use XML output");
		options.addOption("i", true, "Input file or directory");
		options.addOption("o", true, "Output file or directory");
        options.addOption("e", true, "File extension");
		
	    // create a parser for parsing command line
	    CommandLineParser parser = new PosixParser();
	    HelpFormatter formatter = new HelpFormatter();
	    
	    try {
	        // parse the command line arguments
	    	
	    	String[] para = new String[6];
	    	para[0] = "-i";
	    	para[1] = this.in_file;
	    	para[2] = "-o";
	    	para[3] = this.out_file;
	    	para[4] = this.op_process1;
	    	para[5] = this.op_process2;
	    	
	        CommandLine line = parser.parse(options, para);

	        
	        String inputFile = null;
	        String outputFile = null;
	        // get the input file
	        if (line.hasOption("i")) {
	        	inputFile = line.getOptionValue("i");
	        } else {
        		System.err.println("You must provide an input Vietnamese text file (UTF-8 encoding) or an input directory.");
        		formatter.printHelp("vnTokenizer", options);
        		System.exit(1);
	        }
	        // get the output file
	        if (line.hasOption("o")) {
	        	outputFile = line.getOptionValue("o");
	        } else {
        		System.err.println("You must provide an output file name or directory.");
        		formatter.printHelp("vnTokenizer", options);
        		System.exit(1);
	        }
	        
	        if (line.hasOption("nu")) {
	        	TokenizerOptions.USE_UNDERSCORE = false;
	        }
	        
	        if (line.hasOption("sd")) {
	        	TokenizerOptions.USE_SENTENCE_DETECTOR = true;
	        }
	        
	        if (line.hasOption("xo")) {
	        	TokenizerOptions.XML_OUTPUT = true;
	        }
	        
	        // tokenize
	        VietTokenizer vietTokenizer = new VietTokenizer();
	        if (new File(inputFile).isDirectory()) {
		        if (line.hasOption("e")) {
		        	String ext = line.getOptionValue("e");
		        	TokenizerOptions.TEXT_FILE_EXTENSION = ext;
		        }		        
	        	// tokenize the whole directory
	        	vietTokenizer.tokenizeDirectory(inputFile, outputFile);
	        } else {
	        	// clear the old result
	        	nTokens = 0;
	        	// tokenize the file
	    		System.out.println("Tokenizing, please wait...");
	    		long startTime = System.currentTimeMillis();
	        	vietTokenizer.tokenize(inputFile, outputFile);
	    		long endTime = System.currentTimeMillis();
	    		float duration = (float) (endTime - startTime) / 1000;
	    		System.out.println("Tokenized " + nTokens  + " words" +  " in " + duration + " (s).\n");
	        }
	        System.out.println("Done.");
	    }  catch (ParseException exp) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    	formatter.printHelp("vnTokenizer", options);
	    	System.exit(1);
	    }		
	}
}
