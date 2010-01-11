package wordland.data.input;
import liblinear.*;
import java.io.*;
import java.util.*;

import wordland.data.FeatureNodeComp;
import wordland.data.ProblemExt;

/*
 * class to read data into liblinear format extended by us to support remap of words and
 * categories so that indices are smaller than those found in the original data files
 */
public class LibLinearReader {	
	private int maxindex=1;
	public LibLinearReader() {		
	}
	/*
	 * reads a dataset from a file, constructing its own term and category map
	 */
	public ProblemExt readProblem(String fname) {
		ProblemExt problem=new ProblemExt();
		readProblem(fname,problem,true);
		return problem;		
	}
	/*
	 * read a dataset from a file while taking the term map and category map of
	 * another problem without adding any new data to these
	 */
	public ProblemExt readProblemWithOldMaps(String fname,ProblemExt old) {
		ProblemExt problem=new ProblemExt();
		problem.copyHashes(old);
		readProblem(fname,problem,false);
		return problem;
	}
	private void readProblem(String fname,ProblemExt problem,boolean extendhash) {
		try {
			BufferedReader in=new BufferedReader(new FileReader(fname));
			String line;
			ArrayList<Integer> targets=new ArrayList<Integer>();
			ArrayList<FeatureNode []> instances=new ArrayList<FeatureNode []>();
			int inst=0;
			int cats=0;
			while ((line=in.readLine())!=null) {
				if (line.length()>0) {
					String [] words=line.split(" ");
					int cat=Integer.parseInt(words[0]);
					if (cat==0) {  //ugly hack to make the reading work for the test data
						cat=5;
					}
					int newcat=problem.getNewCategory(cat);
					if (newcat==-1) {
						if (extendhash) {
							problem.addCategoryMap(cat, cats);
							newcat=cats++;
						}
						else {
							//throw (new Exception("unknown category found"));
						}
					}
					if (newcat!=-1) {
						FeatureNode[] instance=parseInstance(problem,words,extendhash);
						if (instance!=null) {
							targets.add(newcat);
							instances.add(instance);
						}
					}
					//System.out.println("read instance nr. "+(inst++));
				}
			}
			System.out.println("done reading");
			FeatureNode [][] problemx=new FeatureNode [instances.size()][];
			int [] problemy = new int [instances.size()];			
			for (int i=0;i<instances.size();i++) {
				problemx[i]=instances.get(i);
				problemy[i]=targets.get(i).intValue();
			}
			problem.catnum=cats;
			System.out.println("number of categories "+cats);
			problem.l=instances.size();
			System.out.println("number of instances "+problem.l);
			problem.n=maxindex;
			System.out.println("number of dimensions "+problem.n);
			problem.x=problemx;
			problem.y=problemy;
			problem.bias=0;
			in.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}		
	}
	public FeatureNode [] parseInstance(ProblemExt problem,String [] words,boolean extendhash) throws Exception {
		ArrayList<FeatureNodeComp> ret=new ArrayList<FeatureNodeComp>();
		for (int i=1;i<words.length;i++) {
			String [] ww=words[i].split(":");
			int index=Integer.parseInt(ww[0]);			
			double value=Integer.parseInt(ww[1]);
			if (index>0) {
				int newindex=problem.getNewTerm(index);
				if (newindex==-1) {
					if (extendhash) {
						problem.addTerm(index, maxindex);
						newindex=maxindex++;
					}
					else {
						//throw (new Exception("unknown dimension found"));
					}
				}
				if (newindex!=-1) {
					ret.add(new FeatureNodeComp(newindex,value));
				}
			}
		}
		if (ret.size()==0) {
			return null;
		}
		Collections.sort(ret);
		FeatureNodeComp [] retarray=new FeatureNodeComp [ret.size()];
		ret.toArray(retarray);
		return ((FeatureNode [])retarray);
	}
}
