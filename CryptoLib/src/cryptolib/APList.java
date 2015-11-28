package cryptolib;
/**
 * Super simple data structure for storing a list of APInts 
 */
public class APList{ 		
	
	 private APNode root;
	 private APNode next;
	 private APInt size;
	 
	 APList(){size = APInt.zero;}
	 
	 APList(APInt api){
		 APNode root = new APNode(api);
		 this.root = root;
		 next = root;
		 root.setChild(null);
		 size = APInt.one;
	 }

	 APInt peekFront(){return root.data;}
	 APInt popFront(){
		 APInt n = root.data;
		 root = root.next;
		 return n;
	 }
	 void addFront(APInt api){
		 APNode n = new APNode(api);
		 n.setChild(root);
		 root = n;
		 incrementSize();		 
	 }
	 
	 APInt newIterator(){
		 next = root;
		 return root.data;
	 }
	 
	 APInt next(){
		 next = next.getNext();
		 if(next == null)
			 return null;
		 return next.data;
	 }
	 
	 void incrementSize(){size = size.plus(APInt.one);}
	
	 public String toString(){
		 String out = "[";
		 APNode n = root;
		 do{
			 out += n.toString();
			 out += ",";
		 }while((n = n.getNext()) != null);
		 
		 return out + "]";
	 }
	 public class APNode {
		private APInt data;
		private APNode next;
		
		APNode(APInt data){this.data = data;}
		
		void setChild(APNode n){next = n;} 
		APNode getNext(){return next;}
		
		public String toString(){return data.toString();}
	}

}
