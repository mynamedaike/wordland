package wordland.classifier;
import wordland.*;
import wordland.data.BidirectionalMap;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;

/**
 * some classifiers need the category labels to start with 0 and last until ProblemExt.catnum-1
 * this class remaps all the problem instances that the embedded classifier is fed with to comply
 * also it remaps the output
 */
public class RemapClassifier implements Classifier{
	//oldcat=first, newcat=second
	private BidirectionalMap<Integer> catmap=new BidirectionalMap<Integer>();
	private Classifier cls;
	public RemapClassifier(Classifier c) {
		cls=c;
	}
	public void train(ProblemExt prob,ParameterExt param) {
		int numcat=0;
		int [] yy=new int [prob.l];
		for (int i=0;i<prob.l;i++) {
			Integer newcat=catmap.getSecond(prob.y[i]); 
			if (newcat==null) {
				catmap.addFirstSecond(prob.y[i], numcat);
				newcat=new Integer(numcat++);				
			}
			yy[i]=newcat.intValue();
		}
		ProblemExt fakeprob=new ProblemExt(prob);
		fakeprob.y=yy;
		//fakeprob.setCategoryMap(prob.getCategoryMap().joinFirstSecond(catmap));
		//fakeprob.setCategoryMap(catmap.joinFirstFirst(prob.getCategoryMap()));
		fakeprob.setCategoryMap(catmap.joinFirstSecond(prob.getCategoryMap()).reverse());
		cls.train(fakeprob, param);
	}
	public int [] test(ProblemExt prob) {
		prob.setCategoryMap(catmap.joinFirstSecond(prob.getCategoryMap()).reverse());
		int [] pred=cls.test(prob);
		int [] ret=new int [prob.l];
		for (int i=0;i<prob.l;i++) {
			Integer oldcat=catmap.getFirst(pred[i]);
			if (oldcat!=null) {
				ret[i]=oldcat.intValue();
			}
			else {
				System.out.println("[RemapClassifier.train] there's something wrong");
			}
		}
		return ret;
	}
	public Classifier newInstance() {
		return new RemapClassifier(cls.newInstance());
	}
}
