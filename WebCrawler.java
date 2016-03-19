import org.jsoup.*;
import java.util.*;
import org.jsoup.nodes.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.String;

/**
*	Web Crawler class that takes in a URL and outputs common topics
*   on the page
*   @author Aracely Payan 
*/
public class WebCrawler{
	//Document parsed from the URL given 		
	private static Document doc;	
	//text from the document parsed 
	String originalText; 
	ArrayList<String> body;
	//contains the words that are unlikely to matter for the algorithm
	HashSet<String> stopWords;
	//Map that will contain the words and their frequency in the text 
	HashMap<String, Integer> frequencyCounter = new HashMap<String, Integer>();

	//this number can be modified and represents the number of items
	//to be printed. Lower value for higher precision. 
	static final int NUM_VALUES_TO_PRINT = 10; 

	//number is the min value in the apriori algorithm 
	static final int MIN_VALUE_FOR_PROCESSING = 2; 


	public static void main(String args[]){

		String URL; 
		WebCrawler wC = new WebCrawler();

		try{
			
			URL = args[0];
			//Removing quotes from beginning and end of string if present 
			if(URL.charAt(0) == '"'){
				URL = URL.substring(1, URL.length()-1);
			}
			doc = Jsoup.connect(URL).get();
			wC.createStopWords();
			wC.removeStopWords();

			List<String> values = wC.getWordDenisty();

			//prints out a maximum of NUM_VALUES_TO_PRINT 
			for(String word : values){
				System.out.println(word);
			}

		}
		catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Please enter a valid URL");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	*	Function gets the wods density of the text in the url and returns
	* 	a List of strings equal to NUM_VALUES_TO_PRINT which can be modified above
	*/
	List<String> getWordDenisty(){
		List<String> values = new ArrayList<String>();

		/** 
		* wordsPresent represents the strings in the original text that are present
		* in the body of the text. This call will simply populate hashmap with individual
		* word frequency 
		*/
		ArrayList<String> wordsPresent = updateFrequencyCounter(body, MIN_VALUE_FOR_PROCESSING);

		//Creates new candidates by creating permutations of the wordsPresent 
		ArrayList<String> candidates = createCandidates(wordsPresent);

		while(candidates!=null){
			wordsPresent = updateFrequencyCounter(candidates, MIN_VALUE_FOR_PROCESSING);
			candidates = createCandidates(wordsPresent);
		}
		//creating a comparator to compare the values in the hashmap 
		CustomComparator comparator = new CustomComparator(frequencyCounter);

		//Treemap was created only once to avoid higher look up times 
    	Map<String, Integer> treeMap = new TreeMap<String, Integer>(comparator);
    	treeMap.putAll(frequencyCounter);

    	int counter =0;
    	for(Map.Entry<String,Integer> entry : treeMap.entrySet()) {
    		if(counter >=NUM_VALUES_TO_PRINT){
  				break;
  			}
  			values.add(entry.getKey());
   			counter++;
		}

		return values;
	}

	/**
	*	Stop words are words like "a", "the" "in" that 
	* 	don't add content to the page. The list of stopwords was 
	* 	taken from the following website http://www.ranks.nl/stopwords
	*/
	private void createStopWords(){

		//a hashset is used in order to decrease lookup time 
		stopWords = new HashSet<String>();
		try{

			// New BufferedReader.
			BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"));

			// Add all lines from file to HashSet.
			while (true) {
	    		String line = reader.readLine();
	    		if (line == null) {
					break;
	   			}
	   			line.trim();
	    		stopWords.add(line);
	    	}
	    	reader.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	/**
	*  This function will format the strings that will
	*  be considered the text body of the URL we are observing 
	*/
	private void removeStopWords(){
		
		//removing all non words including spaces and punctuation 
		originalText = doc.text();
		originalText = originalText.toLowerCase();
		//keep only alpha numeric characters, _ and -
		originalText = originalText.replaceAll("[^\\w\\-]", " ");
		//create an array of strings with a delimiter of a space character and convert to arraylist 
		body = new ArrayList<String>(Arrays.asList(originalText.split("\\s+")));

		for(int i=0; i < body.size(); i++){
			//remove words of length <=2 and any words that are a stopword 
			if(body.get(i).length() <= 2 || stopWords.contains(body.get(i))){
				body.remove(i);
			}
		}

	}

	/**
	*	Function takes in the current candidates and tests to see if they are in 
	* 	the original text. If they are and they are not stopwords, update their 
	*	frequency in the hashmap. Returns the list of strings of new candidates that 
	*	appear a minimum number of times in the hashmap. 
	*/
	ArrayList<String> updateFrequencyCounter(ArrayList<String> candidates, int minNumofApperances){

		if(candidates == null || candidates.size() < 1){
			return null;
		}

		ArrayList<String> candidatesPresent = new ArrayList<String>();

		for(String element : candidates){
			//if the string is in the original text and its not a stopword 
			if(element.length() >2 && originalText.contains(element) && !stopWords.contains(element)){

				Integer previousValue = frequencyCounter.get(element); 
				//if it wasn't present before, add it 
				if(previousValue == null){
					frequencyCounter.put(element, 1);
				}
				//if it was present, update the frequency 
				else{
					frequencyCounter.put(element, previousValue + 1);
				}
				//check if minimum number of appearances has been met, if so, add to 
				// new candidate list 
				if(frequencyCounter.get(element) == minNumofApperances){
					candidatesPresent.add(element);
				}
			}
		}

		return candidatesPresent;
	}

	/**
	*	Method takes in a list of the values in the original text that 
	*	appear a minimum number of times and combines them in order 
	*	to create a new string, one string element longer. 
	* 	For example: if list contains two strings "hello", "world"
	*	function returns "hello world"
	*	If function has two strings "hello my", "my world"
	*	Function will return "hello my world" because the first strings 
	*	suffix is the second word's prefix 
	*/	
	ArrayList<String> createCandidates(ArrayList<String> list){
		if(list == null || list.size() < 1){
			return null;
		}

		ArrayList<String> combinedCandidates = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++){
			String element = list.get(i);
			for(int j = i+1; j < list.size(); j++){

				//combining strings to form a new longer string or null if no string can be made 
				String combinedElement = validateCandidacy(element, list.get(j));
				if(combinedElement != null){
					combinedCandidates.add(combinedElement);
				}
			}
		}
		return combinedCandidates;
	}

	/**
	*	Helper function that takes in two strings and returns a new string 
	*	if they can be combined. If they cannot be combined, returns null
	*	Criteria for combining is if either string has been previously combined, 
	*	the last few characters of the first string must be the prefix of the 
	*	second string.
	*	 
	*/
	String validateCandidacy(String first, String second){
		if(first == null || second == null){
			return null;
		}
		//No string is a combination of other strings yet, concatenate
		if(!first.contains(" ")){
			return first + " " + second;
		}
		//check if criteria is met 
		else{
			int lastSpaceIndex = first.lastIndexOf(" ");
			String prefix = first.substring(lastSpaceIndex + 1);
			if(second.startsWith(prefix)){
				return first.substring(0, lastSpaceIndex) + " "+ second;
			}
		}

		return null;

	}

}