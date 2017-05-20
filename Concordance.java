import java.io.*;
import java.util.*;

public class Concordance extends Object {
	protected String omit,permit;
	protected StringBuffer text;
	protected TreeMap tm;
	public Concordance(){
		omit="'";
		permit="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	}
	public Concordance(InputStream in) throws IllegalArgumentException, IOException {
		omit="'";
		permit="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		this.readFile(in);
	}
	public void readFile(InputStream in) throws IllegalArgumentException, IOException {
		// The concordance is built with only the string as the key.
		// The count of ConcordanceElement is a product of the building process.
		tm=new TreeMap<String,ConcordanceElement>();
		text=new StringBuffer();
		while(in.available() > 0){
			text.append(Character.toChars(in.read()));
		}
	}
	public String getOmit(){
		return omit;
	}
	public void setOmit(String _omit){
		omit=_omit;
	}
	public String getPermit(){
		return permit;
	}
	public void setPermit(String _permit){
		permit=_permit;
	}
	public void removeOmitted(){
		int i;
		for(i=0;i<omit.length();i++){
			String s=omit.substring(i,i+1);
			int getIndex=text.indexOf(s);
			while(getIndex >= 0){
				text.deleteCharAt(getIndex);
				getIndex=text.indexOf(s);
			}
		}
	}
	public void splitPermitted(){
		int i=0,start=-1,nend=-1;
		int st;
		while(i < text.length()){
			st=permit.indexOf(text.charAt(i));
			if(st >= 0){
				if(start < 0){
					nend=start=i;
				}
				else {
					nend=i;
				}
			}
			else if(start >= 0){
				ConcordanceElement ce=new ConcordanceElement(text.substring(start,nend+1));
				
				ConcordanceElement cmpr=(ConcordanceElement)tm.get(ce.getText());
				if(cmpr == null){
					tm.put(ce.getText(),ce);
				}
				else {
					ce.increment(cmpr.getCount());
					if(tm.replace(ce.getText(),cmpr,ce) == false){
						System.err.println("Failed to update..." + ce.getText());
					}
				}
				start=nend=-1;
			}
			i++;
		}
	}
	public void dump(String sortOption){
		Iterator iterator;
		Collection<ConcordanceElement> collection=tm.values();

		// *numeric* sorting requires ConcordanceElement to be the key, not String
		if(sortOption.equalsIgnoreCase("numeric")){
			TreeMap<ConcordanceElement,ConcordanceElement> ttm;
			ttm=new TreeMap();
			iterator=collection.iterator();
			while(iterator.hasNext()){
				ConcordanceElement ce=(ConcordanceElement)iterator.next();
				ttm.put(ce,ce);
			}
			collection=ttm.values();
		}
		iterator=collection.iterator();
		while(iterator.hasNext()){
			System.out.println(iterator.next());
		}
	}
	public static void usage(String issue){
		System.err.println(" Usage:   java Concordance alpha|numeric filename.ext");
		System.err.println(" Purpose: Compile a concordance of the file, then print it in alpha or numeric order.");
		if(issue != null){
			System.err.println(" Error:   " + issue);
		}
	}
	public static void main(String argv[]) throws FileNotFoundException, IOException {
		if(argv.length != 2){
			usage("There must be exactly two parameters.  The first is \"numeric\" or \"alpha\", the second is the filename.");
		}
		else if(!argv[0].equalsIgnoreCase("numeric") && !argv[0].equalsIgnoreCase("alpha")){
			usage("The first parameter must be \"numeric\" or \"alpha\".  It is " + argv[0]);
		}
		else {
			Concordance c=new Concordance();
			try {
				c.readFile(new FileInputStream(argv[1]));
				c.removeOmitted();
				c.splitPermitted();
				c.dump(argv[0]);
			}
			catch (Exception e){
				usage(e.getMessage());
			}
		}
	}
}
class ConcordanceElement implements Comparable {
	protected String text;
	private int count;

	// Logic is case-insensitive
	public ConcordanceElement(String _text){
		text=_text.toLowerCase();
		count=1;
	}
	void increment(int additional){
		count+=additional;
	}
	public String getText(){
		return this.text;
	}
	public int getCount(){
		return this.count;
	}
	// This compareTo always sorts numerically by count first, then by text.
	public int compareTo(Object o){
		int tresult,nresult;
		if(o instanceof ConcordanceElement){
			ConcordanceElement ce=(ConcordanceElement)o;
			nresult=count - ce.count;
			if(nresult != 0){
				return nresult;
			}
			tresult=text.compareTo(ce.text);
			return tresult;
		}
		return 0;
	}
	// Format for the output of dump()
	public String toString(){
		return text + ", " + Integer.toString(count);
	}
}
