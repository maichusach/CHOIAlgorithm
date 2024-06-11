package hoi_choi;
  
import java.util.BitSet; 
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
 

public class OccupancyList {
	public Integer item;  				// the item
	int support;
	double ubo;
	double op;
	public int prefixitem;  	 
	public int[] itemsets;  				// the item
	public int[] prefix;  	
	public int[] suffix;  // the item 
    BitSet bitset = new BitSet();
    BitSet bitsetItemset = new BitSet();
	int supportBitset;
	int[] itemsInBitset;
	Hashtable<Integer, Integer> hashtableTidset = new Hashtable<>();
	private static final AtomicInteger IDAuto = new AtomicInteger(0);
	public final int id;
	
    public OccupancyList(Integer item) {
        this.item = item;
        this.id = IDAuto.incrementAndGet();
    }
    public OccupancyList(int[] itemset,Integer item) {
        this.itemsets = itemset;
        this.item = item;
        this.id = IDAuto.incrementAndGet();
    } 
    
    public String getStringItemset() {
    	if(this.itemsets.length>0) {
    		//String joinedString = String.join(this.itemsets);
    		 
    		return "";
    	}
    	else
    	{
    		return "";
    	} 
    }
} 
