package hoi_choi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
 
public class TestCHOIBitset {

	public static void main(String[] args)throws IOException {
		// TODO Auto-generated method stub
		// File path for the input database
		String input = fileToPath("mushroom.txt");  
		// File path for saving the frequent itemsets found
		String output =  ".//output.txt";
		
		// the minsup threshold
		// Note : 0.4 means a minsup of 2 transaction (we used a relative support)
		double minHoi = 0.11;
		
		// Read the input file
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(input); 
		} catch (IOException e) {
			e.printStackTrace();
		} ;
		//database.printDatabase();
		
		AlgoCHOIBitset algo = new AlgoCHOIBitset();		
		
		algo.runAlgorithmBitset(output, database, minHoi,Integer.MAX_VALUE);
		//algo.runAlgorithm(output, database, minsup,Integer.MAX_VALUE);
		// NOTE 1: if you  use "true" in the line above, CHARM will use
		// a triangular matrix  for counting support of itemsets of size 2.
		// For some datasets it should make the algorithm faster.
		algo.printStats();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = TestCHOIBitset.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
