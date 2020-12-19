import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WordCounter implements Runnable{

        // The following are the ONLY variables we will modify for grading.
        // The rest of your code must run with no changes.
        public static final Path FOLDER_OF_TEXT_FILES  = Paths.get("txtFiles2"); // path to the folder where input text files are located
        public static final Path WORD_COUNT_TABLE_FILE = Paths.get("tableOutput.txt"); // path to the output plain-text (.txt) file
        public static final int  NUMBER_OF_THREADS     = 1;                // max. number of threads to spawn 
        
        static ExecutorService executor; //= Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        
        private File instanceFile;
        private static int longestWord;
        private static int totalFiles = 0;
        private static int totalExecuted = 0;
        static TreeMap<String, TreeMap<String, Integer>> treeMapList = new TreeMap<String, TreeMap<String, Integer>>();
        static TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>();
        
        public WordCounter(File currentFile) {
        	instanceFile = currentFile;
		}
		@SuppressWarnings("unused")
		public static void main(String... args) {
        	//long startTime = System.currentTimeMillis();
        	longestWord = 0;
        	File folder = new File(FOLDER_OF_TEXT_FILES.toString());
        	File[] listOfFiles = folder.listFiles();
        	if(NUMBER_OF_THREADS == 0 || NUMBER_OF_THREADS == 1) {
            	TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>();
            	TreeMap<String, TreeMap<String, Integer>> treeMapList = new TreeMap<String, TreeMap<String, Integer>>();
            	//ArrayList<TreeMap<String, Integer>> treeMapList = new ArrayList<TreeMap<String, Integer>>();
            	int longestWord = 0;
            	for(File thisFile :listOfFiles) {
            		if(thisFile.isFile()) {
            			TreeMap<String, Integer> tempTreeMap = new TreeMap<String, Integer>();
            			try {
            				BufferedReader reader;
            				reader = new BufferedReader(new FileReader(thisFile.toString()));
            				String line = reader.readLine();
            				String tempString = "";
            				while(line != null) {
            					tempString += " " + line;
            					line = reader.readLine();
            				}
            				reader.close();
            				for(int i = 0; i < tempString.length(); i++) {
            					if(!Character.isAlphabetic(tempString.charAt(i)) && tempString.charAt(i) != ' ') {
            						tempString = tempString.replace(String.valueOf(tempString.charAt(i)), "");
            					}
            				}
            				tempString = tempString.toLowerCase();
            				String[] tokens = tempString.split(" ");
            				for(String word : tokens) {
            					if(word.length() > longestWord) {
            						longestWord = word.length() + 1;
            					}
            					if(tempTreeMap.containsKey(word)) {
            						tempTreeMap.replace(word, tempTreeMap.get(word) + 1);
            					}
            					else if(!word.equals("")){
            						tempTreeMap.put(word, 1);
            					}
            				}
            				for(String word : tempTreeMap.keySet()) { //updates the encompassing treeMap
            					if(treeMap.containsKey(word)) {
            						treeMap.replace(word, treeMap.get(word) + 1);
            					}
            					else {
            						treeMap.put(word, 1);
            					}
            				}
            				treeMapList.put(thisFile.getName().substring(0, thisFile.getName().length()-4), tempTreeMap); //for printing at the tend
            			}
            			catch(Exception e){
            				e.printStackTrace();
            			}
            		}
            	}
            	//System.out.println(treeMapToString(treeMapList, treeMap, longestWord));
            	writeToFile(treeMapToString(treeMapList, treeMap, longestWord));
        	}
        	else {
	        		executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
	        	for(File currentFile :listOfFiles) {
	        		if(currentFile.isFile()) {
	        			totalFiles++;
	        			Runnable worker = new WordCounter(currentFile);
	        			executor.execute(worker);
	        		}
	        	}
	        	while(totalFiles != totalExecuted) {
	        		try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
	        	}
	        	try {
	        		executor.shutdown();
	        		executor.awaitTermination(1, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					//System.out.println("tasks interrupted");
				}
	        	finally {
	        	    if (!executor.isTerminated()) {
	        	    }
	        	    executor.shutdownNow();
	        	}
	        	//System.out.println(treeMapToString(treeMapList, treeMap, longestWord));
	        	writeToFile(treeMapToString(treeMapList, treeMap, longestWord)); // this should be done by the main thread...
        	}
        }
		public static void writeToFile(String s) {
        	try {
				 File myObj = new File(WORD_COUNT_TABLE_FILE.toString());
				 //myObj.createNewFile();
				 if(myObj.createNewFile()) {
					 myObj.createNewFile();
				 }
				 FileWriter myWriter = new FileWriter(WORD_COUNT_TABLE_FILE.toString());
				 myWriter.write(s);
				 myWriter.close();
        	}
        	catch(Exception e) {
        		
        	}
        }
        
		public static String treeMapToString(TreeMap<String, TreeMap<String, Integer>> treeMapList, TreeMap<String, Integer> treeMap, int longestWord) {
			for(TreeMap<String, Integer> storedMap: treeMapList.values()) {
				for(String word : storedMap.keySet()) {
					if(treeMap.containsKey(word)) {
						treeMap.replace(word, treeMap.get(word) + 1);
					}
					else {
						treeMap.put(word, 1);
					}
				}
			}
			//System.out.println("Longest word: " + longestWord);
			String temp = "";
			temp += addZeros(longestWord + 1, "");
			for(String fileNames : treeMapList.keySet()) {
				temp += fileNames + "    ";
			}
			temp += "total\n";
			for(String word: treeMap.keySet()) {
				temp +=  word + addZeros(longestWord + 1, word);
				for(String treeMapsKey : treeMapList.keySet()) {
					
					if(treeMapList.get(treeMapsKey).containsKey(word)) {
						temp += treeMapList.get(treeMapsKey).get(word) + addZeros(treeMapsKey.length() + 4, treeMapList.get(treeMapsKey).get(word).toString());
					}
					else {
						temp += "0" + addZeros(treeMapsKey.length() + 4, "0");
					}
				}
				int total = 0;
				for(String files : treeMapList.keySet()) {
					if(treeMapList.get(files).containsKey(word)) {
						total += treeMapList.get(files).get(word);
					}
				}
				temp += total + "\n";
			}
			return temp;
		}
		
		public static String addZeros(int longestWord, String word) {
			String temp = "";
			for(int i = 0; i < longestWord - word.length(); i++) {
				temp += " ";
			}
			return temp;
		}
		@Override
		public void run() {
			//System.out.println("Running this instanceFile: " + instanceFile.toString());
			executor.execute(task);
		}
		Runnable task = () -> { // read a file here
			TreeMap<String, Integer> tempTreeMap = new TreeMap<String, Integer>();
			try {
				BufferedReader reader;
				reader = new BufferedReader(new FileReader(instanceFile.toString()));
				String line = reader.readLine();
				String tempString = "";
				while(line != null) {
					tempString += " " + line;
					line = reader.readLine();
				}
				reader.close();
				for(int i = 0; i < tempString.length(); i++) {
					if(!Character.isAlphabetic(tempString.charAt(i)) && tempString.charAt(i) != ' ') {
						tempString = tempString.replace(String.valueOf(tempString.charAt(i)), "");
					}
				}
				tempString = tempString.toLowerCase();
				String[] tokens = tempString.split(" ");
				for(String word : tokens) {
					if(word.length() > longestWord) {
						longestWord = word.length() + 1;
						//System.out.println("New longest word: " + longestWord);
					}
					if(tempTreeMap.containsKey(word)) {
						tempTreeMap.replace(word, tempTreeMap.get(word) + 1);
					}
					else if(!word.equals("")){
						tempTreeMap.put(word, 1);
					}
				}
				totalExecuted++;
				}
			catch(Exception e){
				e.printStackTrace();
			}
			treeMapList.put(instanceFile.getName().substring(0, instanceFile.getName().length()-4), tempTreeMap);
			//System.out.println("Finished adding: " + instanceFile);
		};
    }