package wordland.data.input;

import java.io.*;
import java.util.*;

import wordland.data.ProblemExt;
import wordland.data.*;

public class Dense2DenseReader {
	private int dim = 0;
	public Dense2DenseReader() {
	}
	public ProblemDense readProblem(String xfname, String yfname, String sfname) {
		ProblemDense problem = new ProblemDense();
		ArrayList<Double []> instances = new ArrayList<Double []>();
		//X:
		try {
			BufferedReader in = new BufferedReader(new FileReader(xfname));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					String [] words = line.split(" ");
					if (dim == 0)
						dim = words.length;
					else if (dim != words.length)
						throw new Exception("different number of dimensions");
					ArrayList<Double> instance = new ArrayList<Double>(); 
					for (int i=0; i<words.length; i++) {
						double d; 
						if (words[i].equalsIgnoreCase("inf") || words[i].equalsIgnoreCase("+inf"))
							d = Double.POSITIVE_INFINITY;
						else if (words[i].equalsIgnoreCase("-inf"))
							d = Double.NEGATIVE_INFINITY;
						else if (words[i].equalsIgnoreCase("nan"))
							d = Double.NaN;
						else
							d = Double.parseDouble(words[i]);
						instance.add(d);
					}
					Double [] inst = new Double[instance.size()];
					instance.toArray(inst);
					instances.add(inst);
				}
			}
			System.out.println("done reading x");
			Double [][] problemx = new Double [instances.size()][];
			for (int i=0; i<instances.size(); i++) {
				problemx[i] = instances.get(i);
			}
			problem.x = problemx;
			problem.y = null;
			problem.l = instances.size();
			problem.n = dim;
			problem.catnum = 0;
			problem.seedData = new HashMap<Integer, Integer>();
		} catch (Exception e) {
			System.out.println(e);
		}
		//Y: (multi-class, single label)
		if (yfname != null)
			try {
				BufferedReader in = new BufferedReader(new FileReader(yfname));
				String line;
				HashSet<Integer> categories = new HashSet<Integer>();
				int [] labels = new int[problem.l];
				int i = 0;
				while ((line = in.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0) {
						labels[i++] = (Integer.parseInt(line)+1)/2;
						categories.add(labels[i-1]);
					}
				}
				System.out.println("done reading y");
				problem.y = labels;
				problem.catnum = categories.size();
			} catch (Exception e) {
				System.out.println(e);
			}
		if (sfname != null)
			try {
				BufferedReader in = new BufferedReader(new FileReader(sfname));
				String line;
				while ((line = in.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0) {
						String [] words = line.split(" ");
						if (words.length != 2)
							throw new Exception("line length problem in seed file");
						addSeed(problem, Integer.parseInt(words[0]), Integer.parseInt(words[1]));
					}
				}
				System.out.println("done reading seed");
			} catch (Exception e) {
				System.out.println(e);
			}
		return problem;
	}
	private int addSeed(ProblemDense problem, int index, int label) {
		if (problem.seedData == null)
			return -1;
		problem.seedData.put(index-1, label);
		return 0;
	}
	public static void splitTrainTest(ProblemDense problem, ProblemDense train, ProblemDense test) {
//		train = new ProblemDense();
//		test = new ProblemDense();
		train.catnum = test.catnum = problem.catnum;
		train.n = test.n = problem.n;
		train.seedData = problem.seedData;
		test.seedData = null;
		train.l = test.l = problem.l/2;
		train.x = new Double[train.l][];
		test.x = new Double[test.l][];
		for (int i=0; i<train.l; i++) {
			train.x[i] = problem.x[i];
			test.x[i] = problem.x[train.l+i];
		}
		if (problem.y != null) {
			train.y = new int[train.l];
			test.y = new int[test.l];
			for (int i=0; i<train.l; i++) {
				train.y[i] = problem.y[i];
				test.y[i] = problem.y[train.l+i];
			}
		}
	}
}
