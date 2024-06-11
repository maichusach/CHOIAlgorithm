package hoi_choi;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors; 
 
public class AlgoCHOIBitset {
	/** relative minimum support **/
	private double minHoiRelative;  
	/** the transaction database **/
	protected TransactionDatabase database; 

	/**  start time of the last execution */
	protected long startTimestamp;
	/** end  time of the last execution */
	protected long endTime; 
	
	/** the number of patterns found */
	protected int itemsetCount; 
	
	/** the number of patterns found */
	protected int itemsetClosedCount; 
	
	/** object to write the output file */
	BufferedWriter writer = null; 
 
	public AlgoCHOIBitset() {
		
	} 	
	
	public void runAlgorithmBitset(String output, TransactionDatabase database, double minHoi,  int hashTableSize) throws IOException {
		// Reset the tool to assess the maximum memory usage (for statistics)
		MemoryLogger.getInstance().reset();
		// record the start time
		startTimestamp = System.currentTimeMillis();
		
		// Get data from file
		this.database = database;	
		
		// total size database
		itemsetCount = database.size();

		// convert from an absolute minsup to a relative minsup by multiplying
		// by the database size
		this.minHoiRelative =  minHoi * database.size();
		
		// Define list HOI 
		List<OccupancyList> hois = new ArrayList<OccupancyList>();	// List Hoi
		Map<Integer, OccupancyList> mapItemToOL = new HashMap<Integer, OccupancyList>(); //  Map item to OL
	       
		for (int tid = 0; tid < database.size(); tid++) {
			
			 int len = database.getTransactions().get(tid).size(); 
			 //tidLength = appendItem(tidLength, len);
			 //System.out.println(len) ;
			for (Integer item : database.getTransactions().get(tid)) {
				
				Double occ = 0.0;//itemToOcc.get(item);
                OccupancyList oList = mapItemToOL.get(item);
                if(oList == null)
                {
                    oList = new OccupancyList(item); 
                    mapItemToOL.put(item, oList);
                }
                // we add the current transaction id to the tidset of the item
                oList.bitset.set(tid);
                
                oList.supportBitset++;
                oList.hashtableTidset.put(tid, len);
			} 
		}
		// Define list OP 
		List<OccupancyList> C1 = new ArrayList<OccupancyList>();	// C1 
		
		// for each item
		for(Entry<Integer, OccupancyList> entry : mapItemToOL.entrySet()) {
			// get the support and tidset of that item
			OccupancyList oList = entry.getValue();
			int support = oList.supportBitset;
			int item = entry.getKey();
			// Check Support with minSup 
			if(support >= minHoiRelative) {
				
				// Get list tidset with count
				List<Integer> mapValues = new ArrayList<Integer>(oList.hashtableTidset.values()); 
				Collections.sort(mapValues);
				SortedMap<Integer, Long> counted = new TreeMap<>(mapValues.stream()
			            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));
				// Get UBO
				double ubo = 0.0;
				ubo = getUBOItemsetBitset(counted);
 
				//Check ubo with minsupRelative
				if(ubo >= this.minHoiRelative)
				{
					int[] prefix = new int[] {};
					oList.prefixitem = 0; 
			    	oList.prefix = prefix;
			    	oList.suffix = new int[] {item};
			    	int[] itemsets = new int[] {item};
			    	oList.itemsets = itemsets;
			    	oList.ubo = ubo;
			    	oList.support = support;
					
			    	// Add to C list
			    	C1.add(oList);

					double occ = 0;
			        // Iterating through the Hashtable
			        // object using for-Each loop
			        for (Integer key : oList.hashtableTidset.keySet()) {
			            // get value by key
			            int a = oList.hashtableTidset.get(key);
			            occ += (1.0/a);
			        }
			        
			        //Check occ with minsupRelative
					if(occ >= this.minHoiRelative)
					{
						// Add HOI
						oList.op = occ; 
						hois.add(oList);
					}
				}
			}
		}
		Collections.sort(C1, new Comparator<OccupancyList>(){
			   public int compare(OccupancyList o1, OccupancyList o2){
			      return o1.support - o2.support;
			   }
			});
 		// Call Function Mining
		List<OccupancyList> lsHoik = new ArrayList<OccupancyList>();
		lsHoik = Mine_Depth_CHOIs_Bitset(hois,C1,this.minHoiRelative);
		itemsetClosedCount=  lsHoik.size();
		this.database = null;
		for (OccupancyList ite : lsHoik) {
			System.out.println("ID: " + ite.id + " Item: " + Arrays.toString(ite.itemsets)  +" Support: "+ite.support  +" UBO: "+ite.ubo +" OL: "+ite.op) ;
		}
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
				 
		// we check the memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// record the end time for statistics
		endTime = System.currentTimeMillis();
		
	}
	
	/**
	 * Get List HOI
	 * @param list HOI before
	 * @param list Ck
	 * @param minSup
	 * @return the resulting list HOI
	 * @throws IOException if an error occurs when writing to file
	 */
	
	private List<OccupancyList> Mine_Depth_CHOIs_Bitset(List<OccupancyList> hois, List<OccupancyList> Ck_1, double minHoi) throws IOException {
    	
    	// For from  List Occupancy generation before step last
    	for (int i = 0; i < Ck_1.size(); i++) {
    		// Get info OccupancyList  from Ci input
    		OccupancyList pX = Ck_1.get(i);
    		 
    		//  initialization list OccupancyList 
    		// List this return for works step next
    		List<OccupancyList> Ci = new ArrayList<OccupancyList>();
    		
    		// Get item from set j from i + 1
    		for (int j = i + 1; j < Ck_1.size(); j++) {
    			// Get info OccupancyList 
    			OccupancyList pY = Ck_1.get(j); 
    			
    			// Check is Occupancy Closed
    			//boolean isCheckClose = false;
    			
    			// add prefix item to Occupancy List
    			int[] prefix = appendItem(pX.prefix,pX.item);
    			// add all list item have 
    			int[] prefixSuffix = appendItem(prefix,pY.item);

    			// get lenght total item have
    			// Using Calculated in UBO
    			int length = prefixSuffix.length;
    			
    			// Define new Occupancy 
    			// item pXY from item X and Item Y
    			//OccupancyList pxyOL = new OccupancyList(pY.item);
    			OccupancyList pxyOL = new OccupancyList(prefix,pY.item);
    			pxyOL.prefix = prefix; // Prefix of pXY
    			pxyOL.itemsets = prefixSuffix; // all items of pXY
    			Hashtable<Integer, Integer> hashtableTidsetNew = pX.hashtableTidset;// Get hastable tidset
    			
    			// we perform the intersection bitset PXY
    			pxyOL.bitset = (BitSet)pX.bitset.clone();
    			pxyOL.bitset.and(pY.bitset);
    			 
    			// Check support with minsupRelative
    			if(pxyOL.bitset.cardinality() >= this.minHoiRelative )
    			{ 
    				// Get list tidset in bitset
        			String tidIds = pxyOL.bitset.toString();
        			String my_new_str = tidIds.replace("{", "").replace("}", "");
        			String[] ary = my_new_str.split(",");

    				// Get list hastable tidset with support item
    				for (String tidId : ary) {
    					int tidIdCurrent = Integer.parseInt(tidId.trim()); 
    					var checkByKey = hashtableTidsetNew.get(tidIdCurrent);  
    					pxyOL.hashtableTidset.put(tidIdCurrent, checkByKey) ;
    			    }
    				// Get list 
    				// Calcualator:
    				//   - l({item}): 
    				//   - n({item}):
    				List<Integer> mapValues = new ArrayList<Integer>(pxyOL.hashtableTidset.values()); 
    				Collections.sort(mapValues);
    				SortedMap<Integer, Long> counted = new TreeMap<>(mapValues.stream()
    			            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));
    				
    				// Calcualator UBO 
    				double ubo = 0.0;
    				ubo = getUBOItemsetBitset(counted);
 
    				// Check UBO with minsupRelative
    				if(ubo >= this.minHoiRelative)
    				{ 
    					pxyOL.ubo = ubo;
    					pxyOL.support = pxyOL.bitset.cardinality();
    					// Add C list
    			    	Ci.add(pxyOL);
    			    	
    			    	if(pX.bitset.cardinality() == pY.bitset.cardinality() && 
        						pxyOL.bitset.cardinality() == pX.bitset.cardinality()) {
        					// We remove Xj 
        					//frequentItems.set(j, null);
        					// Then, we calculate the union of X and Xj
        					//int[] realUnion = new int[itemsetX.length + 1];
        					//System.arraycopy(itemsetX, 0, realUnion, 0, itemsetX.length);
        					//realUnion[itemsetX.length] = itemJ;
        					// Then we replace X by the union
        					//itemsetX = realUnion;
    			    		// Remove pX and pY in HOIs list
        					hois.removeIf(obj -> obj.id == pX.id);
        					hois.removeIf(obj -> obj.id == pY.id);
    			    		
        				}else if(pX.bitset.cardinality() < pY.bitset.cardinality()
        						&& pxyOL.bitset.cardinality() == pX.bitset.cardinality()) {
        					// If property 2 holds
        					// Then, we calculate the union of X and Xj
        					//int[] realUnion = new int[itemsetX.length + 1];
        					//System.arraycopy(itemsetX, 0, realUnion, 0, itemsetX.length);
        					//realUnion[itemsetX.length] = itemJ;
        					// Then we replace X by the union
        					//itemsetX = realUnion;
        					// Remove pX in HOIs list
        					hois.removeIf(obj -> obj.id == pX.id);
        				}else if(pX.bitset.cardinality() > pY.bitset.cardinality()
        						&& pxyOL.bitset.cardinality() == pY.bitset.cardinality()) {
        					// If property 3 holds
        					// We remove Xj
        					//frequentItems.set(j, null);
        					// Then, we add the itemset X + J to the equivalence class that
        					// we are building.
        					// Note that we actually only add J because we keep the prefix X for
        					// for the whole equivalence class. Thus X + J can be reconstructed at any time.
        					//equivalenceClassIitemsets.add(new int[] {itemJ});
        					// We also keep the tidset of X + J
        					//equivalenceClassItidsets.add(bitsetSupportUnion);
        					// Step 1 : Add to backlist when to item there --> note check add HOI
        					// Remove pY in HOIs list
        					hois.removeIf(obj -> obj.id == pY.id);
        				}else {  
        					// If property 4 holds
        					// Then, we add the itemset X + J to the equivalence class that
        					// we are building.
        					// Note that we actually only add J because we keep the prefix X for
        					// for the whole equivalence class. Thus X + J can be reconstructed at any time.
        					//equivalenceClassIitemsets.add(new int[] {itemJ});
        					// We also keep the tidset of X + J
        					//equivalenceClassItidsets.add(bitsetSupportUnion);
        					 
        				}
    			    	
    			    	// Check is Occupancy Closed
    	    			boolean isCheckClose = false;
    			    	
    	    			//System.out.println(length);
    			    	List<OccupancyList> listHoisQueryWithMoreThanSupport = hois.stream()
    			                 .filter(p -> p.itemsets.length > length && p.bitset.cardinality() >= pxyOL.bitset.cardinality())
    			                 .toList();
    			    	
    			    	for (OccupancyList ite : listHoisQueryWithMoreThanSupport) {
    			    		boolean checkChild = subset(ite.itemsets,prefixSuffix,ite.itemsets.length,length);
    			    		//System.out.println(checkChild);
    			    		if(checkChild == true)
    			    		{
    			    			//System.out.println(Arrays.toString(ite.itemsets));
    			    			if(pxyOL.bitset.cardinality() > ite.bitset.cardinality()  )
        			    		{
        			    			isCheckClose = false;
        			    		}
    			    			else
    			    			{
    			    				//System.out.println(Arrays.toString(prefixSuffix));
        			    			isCheckClose = true;
    			    			} 
    			    			break;
    			    		}
    					}
    			    	if(isCheckClose == false)
    			    	{
    			    		// Define occ
        					double occ = 0;
        			        //System.out.println(item);
        			        // Iterating through the Hashtable
        			        // object using for-Each loop
        			        for (Integer key : pxyOL.hashtableTidset.keySet()) { 
        			            int valueTid = pxyOL.hashtableTidset.get(key); 
        			            occ += (length*1.0/valueTid); 
        			        }
        			        // Check op with minsupRelative
        					if(occ >= this.minHoiRelative)
        					{
        						// Add to HOI list
        						pxyOL.op = occ;
        						hois.add(pxyOL);
        					}
    			    	}
			    		 
    				} 
    			}
    		} 
    		// Recall mining from result return
    		Mine_Depth_CHOIs_Bitset(hois, Ci, minHoi);
    	}
		return hois;  
    }
 
	 

	/**
	 * Get UBO 
	 * @param SortedMap<Integer, Long> mapSort 
	 * @return the resulting calculator UBO(P)
	 * @throws IOException if an error occurs when writing to file
	 */
	private double getUBOItemsetBitset(SortedMap<Integer, Long> mapSort) {
        double ubo = 0.0; 
        for (var entry : mapSort.entrySet()) {
            int key = entry.getKey();
            double sum = entry.getValue() * 1.0;//sum = (1.0*value*key)/key;
            SortedMap<Integer, Long> subMap = mapSort.tailMap(key + 1);
            for (var entry_sub : subMap.entrySet()) {
                int key1 = entry_sub.getKey();
                long value1 = entry_sub.getValue();
                sum += (1.0 * value1 * key) / key1;
            }
            if (ubo < sum) 
                ubo = sum;
        }
        return ubo;
    }
	
	// Add item to array list
	private int[] appendItem(int[] itemset, int item) {
        int[] newgen = new int[itemset.length + 1];
        System.arraycopy(itemset, 0, newgen, 0, itemset.length);
        newgen[itemset.length] = item;
        return newgen;
    }
	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  CHOI - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ itemsetCount);
		System.out.println(" HOI closed itemsets count : "
				+ itemsetClosedCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out
				.println("===================================================");
	} 
	
	 /* Return true if arr2[] is a subset of arr1[] */
	   private boolean subset(int arr1[], int arr2[], int x, int y) {
	   
	      //declaring hashset
	      HashSet<Integer> hashset = new HashSet<>();
	      
	      // hashset stores all the values of arr1
	      for (int i = 0; i < x; i++) {
	         if (!hashset.contains(arr1[i]))
	            hashset.add(arr1[i]);
	      }
	      
	      // for loop to check if all elements of arr2 also lies in arr1
	      for (int i = 0; i < y; i++) {
	         if (!hashset.contains(arr2[i]))
	            /* return false when arr2[i] is not present in arr1[] */
	         return false;
	      }
	      /* return true when all elements of arr2[] are present in arr1[] */
	      return true;
	   }
}
