
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import vn.hus.nlp.corpus.IConstants;
import vn.hus.nlp.tagger.TaggerOptions;
import vn.hus.nlp.tagger.VietnameseMaxentTagger;
import vn.hus.nlp.tagger.VietnameseMaxentTaggerProvider;
import vn.hus.nlp.tagger.io.IOutputer;
import vn.hus.nlp.tagger.io.PlainOutputer;
import vn.hus.nlp.tagger.io.XMLOutputer;
import vn.hus.nlp.tokenizer.VietTokenizer;
import vn.hus.nlp.utils.UTF8FileUtility;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TaggerConfig;

public class TaggerFile {
	/**
	 * The underlying tokenizer
	 */
	private static VietTokenizer tokenizer = null;

	/**
	 * The maxent tagger.
	 */
	private MaxentTagger tagger;
	
	/**
	 * input, output file and option process
	 */
	String in_file, out_file, op_process1, op_process2;
	
	/**
	 * Initializes the tagger using the default model resource.
	 */
	public TaggerFile() {
		tagger = VietnameseMaxentTaggerProvider.getInstance();
		
		this.in_file = null;
		this.out_file = null;
		this.op_process1 = null;
		this.op_process2 = null;
	}
	
	/**
	 * Initializes the tagger using a model file.
	 * @param modelFile a tagger model.
	 */
	public TaggerFile(String modelFile) {
		tagger = VietnameseMaxentTaggerProvider.getInstance(modelFile);
	}
	
	/**
	 * Tags a list of words. Each word in the list may be a normal Vietnamese word with spaces separating 
	 * syllables.  
	 * @param words a list of words.
	 * @return a list of tagged words
	 * @throws Exception 
	 */
	public List<WordTag> tagList(List<String> words) throws Exception {
		List<WordTag> tokens = new  ArrayList<WordTag>();
		// replace the space characters of the word in the word list by 
		// underscores chars and build a string containing all the words
		StringBuffer buffer = new StringBuffer(words.size()*5);
		for (String word : words) {
			buffer.append(word.replace(' ', '_'));
			buffer.append(" ");
		}
		// tag the tokenized string 
		// changed from version 2.0 of Stanford Maxent Tagger
		String taggedString = MaxentTagger.tagTokenizedString(buffer.toString());
		// split the tagged string using the word/tag delimiter
		String[] pairs = taggedString.split("\\s+");
		String word, tag;
		String xxx = new String("/");
		for (String pair : pairs) {
			
			String[] wt = pair.split(xxx);
			if (wt.length == 2) {
				word = wt[0];
				// recover the space character if user don't want underscores
				if (!TaggerOptions.UNDERSCORE) {
					word = wt[0].replaceAll("_", " ");
				}
				tag = wt[1];
			} else if (wt.length > 2) {
				// the case of date with / separator, for example 20/10/1980/N
				// the word is 20/10/1980
				word = wt[0];
				for (int j = 1; j < wt.length - 1; j++) {
					word += xxx + wt[j];
				}
				// the tag is the last part
				tag = wt[wt.length-1]; 
			} else { // wt.length < 2
				word = "";
				tag = "";
				System.err.println("There is an error.");
			}
			tokens.add(new WordTag(word, tag));
		}
		return tokens;
	}
	
	/**
	 * Tags a text.
	 * @param text a text to tag
	 * @return a list of word/tag pairs.
	 */
	public List<WordTag> tagText2(String text) {
		try {
			// tokenizer the reader
			String tokenizedString = getTokenizer().segment(text);
			String[] arr = tokenizedString.split("\\s+");
			List<String> words = new ArrayList<String>(Arrays.asList(arr));
			return tagList(words);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Tags a text
	 * @param text
	 * @return an array of tags
	 */
	public String[] tagText3(String text) {
		String[] arr = text.split("\\s+");
		List<String> words = new ArrayList<String>(Arrays.asList(arr));
		List<WordTag> list;
		try {
			list = tagList(words);
			List<String> tags = new ArrayList<String>();
			for (WordTag wt : list) {
				tags.add(wt.tag());
			}
			return tags.toArray(new String[list.size()]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Tags a text.
	 * @param text a text to tag.
	 * @see #tagText2(String)
	 * @return a string
	 */
	public String tagText(String text) {
		StringBuffer result = new StringBuffer(1024);
		List<WordTag> list = tagText2(text);
		
		String xxx = new String("/");
		
		for (WordTag wordTag : list) {
			result.append(wordTag.word());
			result.append(xxx);
			result.append(wordTag.tag());
			result.append(" ");
		}
		return result.toString().trim();
	}
	/**
	 * Tags a text file and write the result to an output file. All files are written in UTF-8 encoding.
	 * @param inputFile input file
	 * @param outputFile output file.
	 * @param outputer an outputer 
	 */
	public void tagFile(String inputFile, String outputFile, IOutputer outputer) {
		// get all lines of the input file
		String[] lines = UTF8FileUtility.getLines(inputFile);
		// create output file
		UTF8FileUtility.createWriter(outputFile);
		if (outputer instanceof XMLOutputer) {
			UTF8FileUtility.write("<doc>\n");
		}
		for (String line : lines) {
			List<WordTag> list = tagText2(line);
			UTF8FileUtility.write(outputer.output(list));
		}
		if (outputer instanceof XMLOutputer) {
			UTF8FileUtility.write("</doc>");
		}
		UTF8FileUtility.closeWriter();
	}
	
	/**
	 * Tags a text file and write the result to an output file using a plain output format.
	 * @param inputFile input file
	 * @param outputFile output file.
	 */
	public void tagFile(String inputFile, String outputFile) {
		if (TaggerOptions.PLAIN_TEXT_FORMAT) {
			tagFile(inputFile, outputFile, new PlainOutputer());
		} else {
			tagFile(inputFile, outputFile, new XMLOutputer());
		}
	}

	/**
	 * Gets the Vietnamese tokenizer.
	 * @return the tokenizer
	 */
	public static VietTokenizer getTokenizer() {
		if (tokenizer == null) {
			tokenizer = new VietTokenizer();
		}
		return tokenizer;
	}
	
	/**
	 * @return the tagger in use
	 */
	public MaxentTagger getTagger() {
		return tagger;
	}
	
	/**
	 * Tests a file.
	 * @param filename a file to test. This file contains words which are correctly 
	 * tagged by human annotators. 
	 */
	public void testFile(String filename) {
		TaggerConfig config;
		// create an array of arguments
		String[] arguments = {"-model", "resources/models/vtb.tagger", "-testFile", filename};
		config = new TaggerConfig(arguments);
		if (config.getMode() == TaggerConfig.Mode.TEST) {
			try {
				// build the tagger
				MaxentTagger tagger = new MaxentTagger(config.getModel(), config);
				// test the file 
				tagger.runTestPublic(config);
			} catch (Exception e) {
				e.printStackTrace();
			} 		
		}
		
	}
	
	/**
	 * Nhập thông tin
	 */
	public void set_info(String in_file, String out_file)
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try{
			this.in_file = in_file;
			this.out_file = out_file;
			this.op_process1 = "-u";
			this.op_process2= "";
			/*System.out.println("Nhap cac tuy chon danh chi muc tu");
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
		// create boolean options
		Option underscoreOpt = new Option("u", "Use underscore character for separating syllables of words");
		options.addOption(underscoreOpt);
		//
		Option plainTextFormatOpt = new Option("p", "Use plain text format for saving tagging results.");
		options.addOption(plainTextFormatOpt);
		
		// create obligatory input/output options
		Option inpOpt = new Option("i", true, "Input filename");
		options.addOption(inpOpt);
		
		Option outOpt = new Option("o", true, "Output filename");
		options.addOption(outOpt);
		
		// create a test option
		Option testOpt = new Option("t", true, "Test filename");
		options.addOption(testOpt);
		
		// a help formatter
		HelpFormatter formatter = new HelpFormatter();;
		
		/*if (args.length < 1) {
			// automatically generate the help statement
			formatter.printHelp( "VietnameseMaxentTagger", options );
			System.exit(1);
		}*/
		
		String[] para = new String[6];
		para[0] = "-i";
		para[1] = this.in_file;
		para[2] = "-o";
		para[3] = this.out_file;
		para[4] = this.op_process1;
		para[5] = this.op_process2;
		
		CommandLineParser commandLineParser = new PosixParser();
		try {
			CommandLine commandLine = commandLineParser.parse(options, para);
			
			String testFile = commandLine.getOptionValue("t");
			if (testFile == null) {
				// we are in tag mode
				if (commandLine.hasOption("u")) {
					TaggerOptions.UNDERSCORE = true;
				}
				
				if (commandLine.hasOption("p")) {
					TaggerOptions.PLAIN_TEXT_FORMAT = true;
				}
				
				String inputFile = commandLine.getOptionValue("i");
				if (inputFile == null) {
					System.err.println("Input filename is required.");
					formatter.printHelp( "VietnameseMaxentTagger", options );
					System.exit(1);
				}
				String outputFile = commandLine.getOptionValue("o");
				if (outputFile == null) {
					System.err.println("Output filename is required.");
					formatter.printHelp( "VietnameseMaxentTagger", options );
					System.exit(1);
				}
				// create and run the tagger
				VietnameseMaxentTagger tagger = new VietnameseMaxentTagger();
				System.out.println("Tagging the file. Please wait...");
				tagger.tagFile(inputFile, outputFile);
			} else {
				// we are in test mode
				// create and run the tagger
				VietnameseMaxentTagger tagger = new VietnameseMaxentTagger();
				System.out.println("Testing the file. Please wait...");
				tagger.testFile(testFile);
			}
			System.err.println("Done.");
		} catch (ParseException exp) {
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage());
		}
	}
}
