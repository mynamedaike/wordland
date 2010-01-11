package wordland.competitions.lsthc09;
import liblinear.*;
import libsvm.*;

import org.jblas.*;

import wordland.classifier.*;
import wordland.cluster.*;
import wordland.data.Hierarchy;
import wordland.data.IndexValue;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.data.input.LibLinearReader;
import wordland.dimred.*;
import wordland.distance.*;
import wordland.experiment.*;
import wordland.experiment.tasks.*;
import wordland.kernels.*;
import wordland.utils.EvalMeasures;
import wordland.utils.HierarchyUtils;
import wordland.utils.ProblemUtils;
import wordland.utils.TFIDF;
import wordland.utils.Utils;

import java.util.*;
import java.io.*;

public class Main {
	private static String rootpath = Params.rootPath;
	private static int codelength = Params.ecocCodeLength; 

	public static void main(String [] args) {
		//main_bz(args);
		//main_mzs(args);
		//main_mzs2(args);
		//main_exp(args);
		//main_real(args);
		//main_real2(args);
		//main_real3(args);
		//main_real4(args);
		//main_real5(args);
		//main_real6(args);
		//main_real7(args);
		//main_real8(args);
		main_real11(args);
	}
	public static void main_exp(String [] args) {
		Experiment exp=new Experiment("exp001");
		
		exp.setStages(5);
		
		//stage 0 - loading
		LoadTask load=new LoadTask();
		TaskParam loadp=new TaskParam();
		loadp.addParam("train", Params.rootPath+"train.txt");
		loadp.addParam("test", Params.rootPath+"test.txt");
		loadp.addParam("hierarchy", Params.hierarchyPath+"cat_hier.txt");
		exp.addTask(0,load);
		exp.addTaskParam(load,loadp);
		
		//stage 1 - dimred
		DimRedTask dr=new DimRedTask();
		TaskParam drp=new TaskParam();
		drp.addParam("dimred", new Frequency());
		exp.addTask(1, dr);
		int [] drps={3000,7000,11000,14000,20000};
		exp.addTaskParam(dr, TaskParam.createNewParams(drp, "dim", drps));
		TaskParam drp2=new TaskParam();
		drp2.addParam("dimred", new ChiSquare());		
		exp.addTaskParam(dr, TaskParam.createNewParams(drp2, "dim", drps));
		
		DimRedClustTask drclusttask=new DimRedClustTask();
		ParameterExt clustpar=new ParameterExt();
		clustpar.clusters=300;
		clustpar.maxpoints=3000;
		clustpar.select_distance=new L0Distance();
		clustpar.normalization=new L1Distance();
		ClusterWrapper cluster=new ClusterWrapper(new KMeans());
		ClusteringMethod drclu=new ClusteringMethod(cluster,new CosDistance(),clustpar);
		TaskParam drclup=new TaskParam();
		drclup.addParam("dimred", drclu);
		drclup.addParam("parameters", clustpar);
		exp.addTask(1, drclusttask);
		int [] drclups={100,200,400,700,1000};
		exp.addTaskParam(drclusttask, TaskParam.createNewParams(drclup, "dim", drclups));
		
		//stage 2 - tfidf
		TFIDFTask tfidf=new TFIDFTask();
		TaskParam tfidfp=new TaskParam();
		tfidfp.addParam("normalize", "yes");
		exp.addTask(2, tfidf);
		exp.addTaskParam(tfidf, tfidfp);
		
		//stage 3 - training & testing
		SimpleTrainTestTask train=new SimpleTrainTestTask();
		Parameter param=new Parameter(Params.LibLinearType,1,0.0001);
		ParameterExt paramecoc=new ParameterExt();
		paramecoc.param=param;
		paramecoc.ecoc_nr=codelength;
		TaskParam trainp=new TaskParam();
		trainp.addParam("classifier", new ECOC2());
		trainp.addParam("parameters", paramecoc);
		exp.addTask(3, train);
		exp.addTaskParam(train, trainp);

		Parameter svmparam=new Parameter(Params.LibLinearType,1,0.0001);
		ParameterExt embparam=new ParameterExt();
		embparam.param=svmparam;
		ParameterExt paramhsvm=new ParameterExt();
		paramhsvm.embeddedparam=embparam;
		paramhsvm.embedded=new SVM();
		TaskParam trainp2=new TaskParam();
		trainp2.addParam("classifier", new HSVM());
		trainp2.addParam("parameters", paramhsvm);
		exp.addTaskParam(train, trainp2);
		
		ParameterExt paramhclass1p=new ParameterExt();
		TaskParam trainp3=new TaskParam();
		trainp3.addParam("classifier", new HierarchicalClassifiers1());
		trainp3.addParam("parameters", paramhclass1p);
		exp.addTaskParam(train, trainp3);
		
		//stage 4 - results
		ResultTask result=new ResultTask();
		exp.addTask(4, result);
		
		exp.run();
	}
	public static void main2(String [] args) { //for testing...
		System.out.println("starting...");
		ProblemExt [] data=loadData1();
		Hierarchy subhier=loadHierarchy1();
		System.out.println(subhier.printHierarchy());
		ProblemExt [] finaldata=transformData1(data[0],data[1]);
		ProblemExt finaltrain=finaldata[0];
		ProblemExt finaltest=finaldata[1];

		ClassDistances a = new ClassDistances();
		a.CalcGaussDistances(finaltrain, 20);
		System.out.println("...OVER");
	}
	/**
	 * real testing on the real dataset
	 */
	public static void main_real(String [] args) {
		System.out.println("starting...");
		ProblemExt [] data=loadData1();
		Hierarchy subhier=loadHierarchy1();
		//System.out.println(subhier.printHierarchy());
		ProblemExt [] finaldata=transformData1(data[0],data[1]);
		//ProblemExt [] finaldata=transformData3(data[0],data[1]);
		//ProblemExt [] finaldata=transformData_NB(data[0],data[1]);
		//ProblemExt [] finaldata=transformData2(data[0],data[1],subhier);

		ProblemExt finaltrain=finaldata[0];
		ProblemExt finaltest=finaldata[1];
		finaldata=null;
		System.gc();
		int [] pred=trainAndTest1(finaltrain,finaltest);
		//int [] pred=trainAndTest_E2(finaltrain,finaltest);
		//int [] pred = trainAndTest_NB(finaltrain, finaltest);
		//int [] pred=trainAndTest_LIBSVM(finaltrain,finaltest);
		//int [] pred=trainAndTest2(finaltrain,finaltest,subhier);
		
		//int [] pred=trainAndTest2(finaltrain,finaltest,subhier);
		//int [] pred=trainAndTest_KNN(finaltrain,finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		saveResults(ProblemUtils.transformNewLabelsToOld(pred,finaltrain),"res.txt");
		saveResults(finaltest.y,"ress.txt");
	}
	/**
	 * real testing on the real dataset 2 (adding validation data to training)
	 */
	public static void main_real2(String [] args) {
		System.out.println("starting...");
		ProblemExt [] data=loadData_valtrain();
		//ProblemExt [] data=loadData1();
		Hierarchy subhier=loadHierarchy1();
		//System.out.println(subhier.printHierarchy());
		//ProblemExt [] finaldata=transformData1(data[0],data[1]);
		ProblemExt [] finaldata=transformData_nofs(data[0],data[1]);
		//ProblemExt [] finaldata=transformData3(data[0],data[1]);
		//ProblemExt [] finaldata=transformData_NB(data[0],data[1]);
		//ProblemExt [] finaldata=transformData2(data[0],data[1],subhier);

		ProblemExt finaltrain=finaldata[0];
		ProblemExt finaltest=finaldata[1];
		finaldata=null;
		System.gc();
		//int [] pred=trainAndTest1(finaltrain,finaltest);
		int [] pred=trainAndTest_E2(finaltrain,finaltest);
		//int [] pred = trainAndTest_NB(finaltrain, finaltest);
		//int [] pred=trainAndTest_LIBSVM(finaltrain,finaltest);
		//int [] pred=trainAndTest2(finaltrain,finaltest,subhier);
		
		//int [] pred=trainAndTest2(finaltrain,finaltest,subhier);
		//int [] pred=trainAndTest_KNN(finaltrain,finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		saveResults(ProblemUtils.transformNewLabelsToOld(pred,finaltrain),"res.txt");
		saveResults(finaltest.y,"ress.txt");
	}
	public static void main_real3(String [] args) {
		System.out.println("starting...");
		ProblemExt [] data=loadData_valtrain();
		//ProblemExt [] data=loadData1();
		Hierarchy subhier=loadHierarchy1();
		//System.out.println(subhier.printHierarchy());
		//ProblemExt [] finaldata=transformData1(data[0],data[1]);
		ProblemExt [] finaldata=transformData_nofs(data[0],data[1]);
		//ProblemExt [] finaldata=transformData3(data[0],data[1]);
		//ProblemExt [] finaldata=transformData_NB(data[0],data[1]);
		//ProblemExt [] finaldata=transformData2(data[0],data[1],subhier);

		ProblemExt finaltrain=finaldata[0];
		ProblemExt finaltest=finaldata[1];
		finaldata=null;
		System.gc();
		//int [] pred=trainAndTest1(finaltrain,finaltest);
		int [] pred=trainAndTest_E2(finaltrain,finaltest);
		//int [] pred = trainAndTest_NB(finaltrain, finaltest);
		//int [] pred=trainAndTest_LIBSVM(finaltrain,finaltest);
		//int [] pred=trainAndTest2(finaltrain,finaltest,subhier);
		
		//int [] pred=trainAndTest2(finaltrain,finaltest,subhier);
		//int [] pred=trainAndTest_KNN(finaltrain,finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		saveResults(ProblemUtils.transformNewLabelsToOld(pred,finaltrain),"res.txt");
		saveResults(finaltest.y,"ress.txt");
	}	
	/**
	 * real testing on the real dataset 2 (adding validation data to training) 4-th (uploaded 3rd, not used :D)
	 * # Accuracy = 0.119323
	 * # F-measure = 0.124253
	 * # Precision = 0.114509
	 * # Recall = 0.13581
	 */
	public static void main_real4(String [] args) {
		memstat("1.");
		System.out.println("starting...");
		ProblemExt [] data=loadData1();
		Hierarchy subhier=loadHierarchy1();
		data[0].hierarchy=subhier;
		data[1].hierarchy=subhier;

		memstat("2.");

		ProblemExt [] finaldata=transformData1(data[0],data[1]);
		ProblemExt finaltrain=finaldata[0];
		ProblemExt finaltest=finaldata[1];
		
		System.gc();
		memstat("3.");
		
		data=null;
		System.gc();

		memstat("4.");
		int [] pred=trainAndTest_HC2(finaltrain,finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		int [] goodlab=ProblemUtils.transformNewLabelsToOld(finaltest.y, finaltrain);
		int [] predlab=ProblemUtils.transformNewLabelsToOld(pred, finaltrain);
		saveResults(predlab,"res.txt");
		Hashtable<Integer,Double> res=countCorrectPredictions(goodlab,predlab,subhier);
		System.out.println("correctly found "+res.get(Hierarchy.root));
	}
	/**
	 * ECOC might be better off without normalization and feature selection :D 
	 * numfeatures = 10000, ecocCodeLength=1000
	 * # Accuracy = 0.239908
	 * # F-measure = 0.0556596
	 * # Precision = 0.0534764
	 * # Recall = 0.0580287 
	 */
	public static void main_real5(String [] args) {
		System.out.println("starting...");
		ProblemExt [] data=loadData_valtrain();


		memstat("before fs ");
		
		ProblemExt [] datafs=transformData_justfs(data[0],data[1]);
		
		ProblemExt finaltrain=datafs[0];
		ProblemExt finaltest=datafs[1];
		
		Utils.normalize(finaltrain, new L1Distance());
		Utils.normalize(finaltrain, new L1Distance());
		
		data=null;
		System.gc();
		
		memstat("after fs  ");

		int [] pred=trainAndTest_E2(finaltrain,finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		saveResults(ProblemUtils.transformNewLabelsToOld(pred,finaltrain),"res.txt");
		saveResults(finaltest.y,"ress.txt");
	}
	/**
	 * now ECOC with 150000 features and with L1 normalization
	 * # Accuracy = 0.248567
	 * # F-measure = 0.0664676
	 * # Precision = 0.0663077
	 * # Recall = 0.0666283
	 */
	public static void main_real6(String [] args) {
		System.out.println("starting...");
		ProblemExt [] data=loadData_valtrain();


		System.gc();
		memstat("before norm ");

		ProblemExt [] datafs=transformData_justfs(data[0],data[1]);

		ProblemExt finaltrain=datafs[0];
		ProblemExt finaltest=datafs[1];
		
		Utils.normalize(finaltrain, new L1Distance());
		Utils.normalize(finaltest, new L1Distance());
		
		data=null;
		System.gc();
		
		memstat("after norm  ");

		int [] pred=trainAndTest_E2(finaltrain,finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		saveResults(ProblemUtils.transformNewLabelsToOld(pred,finaltrain),"res.txt");
		saveResults(finaltest.y,"ress.txt");
	}
	/**
	 * Bipartite Graph based classifier with 150000 features and with L1 normalization
	 * # Accuracy = 0.219037
	 * # F-measure = 0.125199
	 * # Precision = 0.108788
	 * # Recall = 0.147441
	 */
	public static void main_real7(String [] args) {
		System.out.println("starting...");
		ProblemExt [] data=loadData_valtrain();


		System.gc();
		memstat("before norm ");

		ProblemExt [] datafs=transformData_justfs(data[0],data[1]);

		ProblemExt finaltrain=datafs[0];
		ProblemExt finaltest=datafs[1];
		
		//Utils.normalize(finaltrain, new L1Distance());
		//Utils.normalize(finaltest, new L1Distance());
		
		data=null;
		System.gc();
		
		memstat("after norm  ");

		int [] pred=trainAndTest_BG(finaltrain,finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		saveResults(ProblemUtils.transformNewLabelsToOld(pred,finaltrain),"res.txt");
		saveResults(finaltest.y,"ress.txt");
	}

	/**
	 * Bipartite Graph based classifier with 150000 features and with L1 normalization
	 * # Accuracy = 0.219037
	 * # F-measure = 0.125199
	 * # Precision = 0.108788
	 * # Recall = 0.147441
	 */
	public static void main_real8(String [] args) {
		System.out.println("starting...");

		LibLinearReader read=new LibLinearReader();
		ProblemExt [] train = new ProblemExt [2];
		train[0]=read.readProblem(rootpath+"train.txt");
		train[1]=read.readProblemWithOldMaps(rootpath+"validation.txt", train[0]);
		
		Hierarchy hier=loadHierarchy1();
		
		train[0].hierarchy=hier;
		train[1].hierarchy=hier;

		ProblemExt train0=ProblemUtils.mergeProblems(train,true);
		
		Frequency dr=new Frequency();
		dr.setNrFeatures(Params.numFeatures);
		dr.collect(train0);
		dr.process();
		ProblemExt train1=dr.remap(train0);

		train0.x=null; //this is eating up the most memory

		TFIDF tfidf = new TFIDF();
		tfidf.count(train1);
		ProblemExt train2=tfidf.transform(train1);
		
		train1=null;
		
		Utils.norml2(train2);
		
		ProblemExt finaltrain=train2;
		
		//Parameter svmparam=new Parameter(Params.LibLinearType,1,0.01);
		//ParameterExt embparam=new ParameterExt();
		//embparam.param=svmparam;
		ParameterExt finalparam=new ParameterExt();
		//finalparam.embeddedparam=embparam;
		//finalparam.embedded=new SVM();
		
		finaltrain.hierarchy=hier;
		
		//HierarchicalClassifiers2 cls=new HierarchicalClassifiers2();
		HierarchicalClassifiers2 cls=new HierarchicalClassifiers2();
		
		cls.train(finaltrain, finalparam);

		finaltrain.x=null;
		
		ProblemExt test0=read.readProblemWithOldMaps(rootpath+"test.txt", train0);		
		ProblemExt test1=dr.remap(test0);
		ProblemExt test2=tfidf.transform(test1);
		Utils.norml2(test2);
		
		test0=null;
		test1=null;
		
		ProblemExt finaltest=test2;
		
		int [] pred=cls.test(finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		saveResults(ProblemUtils.transformNewLabelsToOld(pred,finaltrain),"res.txt");
		saveResults(finaltest.y,"ress.txt");
	}
	public static void main_real11(String [] args) {
		System.out.println("starting...");

		LibLinearReader read=new LibLinearReader();
		ProblemExt [] train = new ProblemExt [2];
		train[0]=read.readProblem(rootpath+"train.txt");
		train[1]=read.readProblemWithOldMaps(rootpath+"validation.txt", train[0]);
		
		Hierarchy hier=loadHierarchy1();
		
		train[0].hierarchy=hier;
		train[1].hierarchy=hier;
		
		ProblemExt train0=ProblemUtils.mergeProblems(train,true);

		
		DRL1RegSVM dr=new DRL1RegSVM();
		//Frequency dr=new Frequency();
		dr.setNrFeatures(Params.numFeatures);
		dr.collect(train0);
		dr.process();
		ProblemExt train1=dr.remap(train0);

		train0.x=null; //this is eating up the most memory

		TFIDF tfidf = new TFIDF();
		tfidf.count(train1);
		ProblemExt train2=tfidf.transform(train1);
		
		train1=null;
		
		Utils.norml2(train2);
		
		ProblemExt finaltrain=train2;
		
		Parameter svmparam=new Parameter(Params.LibLinearType,1,0.1);
		ParameterExt finalparam=new ParameterExt();
		finalparam.param=svmparam;
		finalparam.ecoc_nr=Params.ecocCodeLength;
		
		finaltrain.hierarchy=hier;
		
		ECOC2 cls=new ECOC2();
		
		cls.train(finaltrain, finalparam);

		finaltrain.x=null;
		
		ProblemExt test0=read.readProblemWithOldMaps(rootpath+"test.txt", train0);		
		ProblemExt test1=dr.remap(test0);
		ProblemExt test2=tfidf.transform(test1);
		Utils.norml2(test2);
		
		test0=null;
		test1=null;
		
		ProblemExt finaltest=test2;
		
		int [] pred=cls.test(finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		saveResults(ProblemUtils.transformNewLabelsToOld(pred,finaltrain),"res.txt");
		saveResults(finaltest.y,"ress.txt");
		System.out.println("feature set size:"+dr.getNrFeatures());
	}
	public static void main_bz(String [] args) {
		System.out.println("starting...");
		ProblemExt [] data=loadData1();
		//ProblemExt [] data1 = GVSMTransform.transform(data);
		//data = GVSMTransform.transform(data);
		
		Hierarchy subhier=loadHierarchy1();
		
		//System.out.println(subhier.printHierarchy());
		//subhier.reduceHierarchy(subhier.root);
		//System.out.println(subhier.printHierarchy());
		
		ProblemExt [] finaldata=transformData1(data[0],data[1]);
		//ProblemExt [] finaldata=transformData1(data1[0],data1[1]);
		
		//ProblemExt [] finaldata=transformData3(data[0],data[1]);
		//ProblemExt [] finaldata=transformData_NB(data[0],data[1]);
		//ProblemExt [] finaldata=transformData2(data[0],data[1],subhier);

		ProblemExt finaltrain=finaldata[0];
		ProblemExt finaltest=finaldata[1];
		//finaltrain.addPlusDimension(); //???
		//finaltest.addPlusDimension(); //???

		//int [] pred=trainAndTest_LP(finaltrain, finaltest);
		//int [] pred=trainAndTest_E4(finaltrain,finaltest);
		//int [] pred=trainAndTest_ESA(finaltrain,finaltest);
		int [] pred=trainAndTest_BatchHieron(finaltrain,finaltest,subhier);
		//int [] pred=trainAndTest_Hieron(finaltrain,finaltest,subhier);
		//int [] pred = trainAndTest_NB(finaltrain, finaltest);
		//int [] pred=trainAndTest_LIBSVM(finaltrain,finaltest);
		//int [] pred=trainAndTest2(finaltrain,finaltest,subhier);
		
		//int [] pred=trainAndTest2(finaltrain,finaltest,subhier);
		//int [] pred=trainAndTest_KNN(finaltrain,finaltest);

		//int corr=countCorrectPredictions(finaltest.y,pred);
		//System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		EvalMeasures e = new EvalMeasures(finaltest, subhier, pred, finaltrain.catnum);
		e.printMeasures();
	}
	public static void main_mzs(String [] args) {
		System.out.println("starting...");
		ProblemExt [] data=loadData1();
		Hierarchy subhier=loadHierarchy1();
		data[0].hierarchy=subhier;
		data[1].hierarchy=subhier;
		
		//System.out.println(subhier.printHierarchy());
		//ProblemExt [] finaldata=transformData1(data[0],data[1]);
		//ProblemExt [] finaldata=transformData3(data[0],data[1]);
		//ProblemExt [] finaldata=transformData_NB(data[0],data[1]);
		//ProblemExt [] finaldata=transformData_Clu(data[0],data[1],subhier);

		ProblemExt [] finaldata=transformData2(data[0],data[1],subhier);
		//ProblemExt [] finaldata=transformData_hfs(data[0],data[1]);
		ProblemExt finaltrain=finaldata[0];
		ProblemExt finaltest=finaldata[1];
		
		//int [] pred=trainAndTest1(finaltrain,finaltest);
		int [] pred=trainAndTest_E2(finaltrain,finaltest);
		//int [] pred = trainAndTest_NB(finaltrain, finaltest);
		//int [] pred=trainAndTest_LIBSVM(finaltrain,finaltest);
		//int [] pred=trainAndTest_LIBSVMp(finaltrain,finaltest);
		//int [] pred=trainAndTest_HSVM(finaltrain,finaltest,subhier);
		
		//int [] pred=trainAndTest_HRefi(finaltrain,finaltest,subhier);
		//int [] pred=trainAndTest_KNN(finaltrain,finaltest);
		//int [] pred=trainAndTest_HRefiE(finaltrain,finaltest,subhier);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		int [] goodlab=ProblemUtils.transformNewLabelsToOld(finaltest.y, finaltrain);
		int [] predlab=ProblemUtils.transformNewLabelsToOld(pred, finaltrain);
		Hashtable<Integer,Double> res=countCorrectPredictions(goodlab,predlab,subhier);
		/*for (int i : res.keySet()) {
			System.out.println("level "+i+" accuracy "+res.get(i));
		}*/
		System.out.println("correctly found "+res.get(Hierarchy.root));
	}
	public static void memstat(String pref) {
		System.out.println(pref+" total mem   "+Runtime.getRuntime().totalMemory());
		System.out.println(pref+" free mem    "+Runtime.getRuntime().freeMemory());
		System.out.println(pref+" difference  "+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()));
	}
	public static void main_mzs2(String [] args) {
		memstat("1.");
		System.out.println("starting...");
		ProblemExt [] data=loadData1();
		Hierarchy subhier=loadHierarchy1();
		data[0].hierarchy=subhier;
		data[1].hierarchy=subhier;

		memstat("2.");

		//System.out.println(subhier.printHierarchy());
		ProblemExt [] finaldata=transformData1(data[0],data[1]);
		//ProblemExt [] finaldata=transformData1_nonorm(data[0],data[1]);
		//ProblemExt [] finaldata=transformData3(data[0],data[1]);
		//ProblemExt [] finaldata=transformData_NB(data[0],data[1]);
		//ProblemExt [] finaldata=transformData_Clu(data[0],data[1],subhier);

		//ProblemExt [] finaldata=transformData2(data[0],data[1],subhier);
		//ProblemExt [] finaldata=transformData_hfs(data[0],data[1]);
		ProblemExt finaltrain=finaldata[0];
		ProblemExt finaltest=finaldata[1];
		
		System.gc();
		memstat("3.");
		
		data=null;
		System.gc();

		memstat("4.");
		
		//int [] pred=trainAndTest1(finaltrain,finaltest);
		//int [] pred=trainAndTest_E2(finaltrain,finaltest);
		//int [] pred = trainAndTest_NB(finaltrain, finaltest);
		//int [] pred=trainAndTest_LIBSVM(finaltrain,finaltest);
		//int [] pred=trainAndTest_LIBSVMp(finaltrain,finaltest);
		//int [] pred=trainAndTest_HSVM(finaltrain,finaltest,subhier);
		
		//int [] pred=trainAndTest_HRefi(finaltrain,finaltest,subhier);
		//int [] pred=trainAndTest_KNN(finaltrain,finaltest);
		//int [] pred=trainAndTest_HRefiE(finaltrain,finaltest,subhier);
		
		int [] pred=trainAndTest_HC2(finaltrain,finaltest);

		int corr=countCorrectPredictions(finaltest.y,pred);
		System.out.println("correctly found "+corr+" out of "+finaltest.y.length + " (" + ((double)corr/finaltest.y.length) + ")");
		int [] goodlab=ProblemUtils.transformNewLabelsToOld(finaltest.y, finaltrain);
		int [] predlab=ProblemUtils.transformNewLabelsToOld(pred, finaltrain);
		saveResults(predlab,"res.txt");
		Hashtable<Integer,Double> res=countCorrectPredictions(goodlab,predlab,subhier);
		System.out.println("correctly found "+res.get(Hierarchy.root));
	}
	/**
	 * loads "text" data to make the main a little cleaner
	 * this function will have a few other versions
	 */
	public static ProblemExt [] loadData1() {
		ProblemExt [] ret = new ProblemExt [2];
		LibLinearReader read=new LibLinearReader();
		ret[0]=read.readProblem(rootpath+"train.txt");
		ret[1]=read.readProblemWithOldMaps(rootpath+"test.txt", ret[0]);
		return ret;
	}
	/**
	 * loads "text" data to make the main a little cleaner
	 * this function will have a few other versions
	 */
	public static ProblemExt [] loadData_valtrain() {
		ProblemExt [] ret = new ProblemExt [2];
		ProblemExt [] train = new ProblemExt [2];
		LibLinearReader read=new LibLinearReader();
		train[0]=read.readProblem(rootpath+"train.txt");
		train[1]=read.readProblemWithOldMaps(rootpath+"validation.txt", train[0]);
		ret[0]=ProblemUtils.mergeProblems(train,true);
		ret[1]=read.readProblemWithOldMaps(rootpath+"test.txt", ret[0]);		
		return ret;
	}
	/**
	 * loads hierarchy to make the main function a little cleaner
	 * this function will have a few other versions
	 */
	public static Hierarchy loadHierarchy1() {
		Hierarchy hier=new Hierarchy();
		hier.load(Params.hierarchyPath+"cat_hier.txt");
		return hier;
	}
	public static Hierarchy loadHierarchy2() {
		Hierarchy hier=new Hierarchy();
		hier.load(Params.hierarchyPath+"cat_hier.txt");
		Hierarchy subhier=hier.getSubHierarchy(2);
		return subhier;
	}
	public static Hierarchy loadHierarchy3() {
		Hierarchy hier=new Hierarchy();
		hier.load(Params.hierarchyPath+"cat_hier.txt");
		Hierarchy subhier=HierarchyUtils.collapseHierarchyToLevels(hier, 2, 2);
		System.out.println(subhier.printHierarchy());
		return subhier;
	}
	/**
	 * transforms the data to make the main function a little cleaner
	 * this function will have LOTS of other versions
	 */
	public static ProblemExt [] transformData1(ProblemExt train,ProblemExt test) {
		//Frequency dr=new Frequency();
		DRL1RegSVM dr=new DRL1RegSVM();
		//ChiSquare dr=new ChiSquare();
		dr.setNrFeatures(Params.numFeatures);
		dr.collect(train);
		dr.process();
		ProblemExt train2=dr.remap(train);
		ProblemExt test2=dr.remap(test);
		TFIDF tfidf = new TFIDF();
		tfidf.count(train2);
		ProblemExt [] ret=new ProblemExt [2];
		ret[0]=tfidf.transform(train2);
		ret[1]=tfidf.transform(test2);
		Utils.norml2(ret[0]);
		Utils.norml2(ret[1]);
		return ret;
	}
	public static ProblemExt [] transformData_justfs(ProblemExt train,ProblemExt test) {
		Frequency dr=new Frequency();
		//ChiSquare dr=new ChiSquare();
		dr.setNrFeatures(Params.numFeatures);
		dr.collect(train);
		dr.process();
		ProblemExt [] ret=new ProblemExt [2];
		ret[0]=dr.remap(train);
		ret[1]=dr.remap(test);
		return ret;
	}
	public static ProblemExt [] transformData1_nonorm(ProblemExt train,ProblemExt test) {
		Frequency dr=new Frequency();
		//ChiSquare dr=new ChiSquare();
		dr.setNrFeatures(Params.numFeatures);
		dr.collect(train);
		dr.process();
		ProblemExt train2=dr.remap(train);
		ProblemExt test2=dr.remap(test);
		TFIDF tfidf = new TFIDF();
		tfidf.count(train2);
		ProblemExt [] ret=new ProblemExt [2];
		ret[0]=tfidf.transform(train2);
		ret[1]=tfidf.transform(test2);
		return ret;
	}
	public static ProblemExt [] transformData_redhier(ProblemExt train,ProblemExt test,int levels) {
		Frequency dr=new Frequency();
		//ChiSquare dr=new ChiSquare();
		dr.setNrFeatures(Params.numFeatures);
		train=ProblemUtils.collapseProblemToLevels(train, Hierarchy.root, levels, null);
		test=ProblemUtils.collapseProblemToLevels(test, Hierarchy.root, levels, train.getCategoryMap());
		System.out.println("training hierarchy "+train.hierarchy.printHierarchy());
		System.out.println("testing hierarchy "+train.hierarchy.printHierarchy());
		dr.collect(train);
		dr.process();
		ProblemExt train2=dr.remap(train);
		ProblemExt test2=dr.remap(test);
		TFIDF tfidf = new TFIDF();
		tfidf.count(train2);
		ProblemExt [] ret=new ProblemExt [2];
		ret[0]=tfidf.transform(train2);
		ret[1]=tfidf.transform(test2);
		Utils.norml2(ret[0]);
		Utils.norml2(ret[1]);
//		ProblemExt [] ret=new ProblemExt [2];
//		ret[0]=train2;
//		ret[1]=test2;
//		Utils.norml2(ret[0]);
//		Utils.norml2(ret[1]);
		return ret;
	}
	/**
	 * transforms data with no feature selection
	 */
	public static ProblemExt [] transformData_nofs(ProblemExt train,ProblemExt test) {
		ProblemExt train2=train;
		ProblemExt test2=test;
		TFIDF tfidf = new TFIDF();
		tfidf.count(train2);
		ProblemExt [] ret=new ProblemExt [2];
		ret[0]=tfidf.transform(train2);
		ret[1]=tfidf.transform(test2);
		Utils.norml2(ret[0]);
		Utils.norml2(ret[1]);
		return ret;
	}
	/**
	 * data transformation for HSVM testing mainly
	 * no feature selection, only tfidf and normalization
	 */
	public static ProblemExt [] transformData2(ProblemExt train,ProblemExt test,Hierarchy subhier) {
		//Frequency dr=new Frequency();
		DRL1RegSVM dr=new DRL1RegSVM();
		dr.setNrFeatures(1000);
		ProblemExt train15 = ProblemUtils.getSubProblemWithOldMap(train, subhier, null);
		ProblemExt test15 = ProblemUtils.getSubProblemWithOldMap(test, subhier, train15);		
		
		dr.collect(train15);
		dr.process();
		ProblemExt train2=dr.remap(train15);
		ProblemExt test2=dr.remap(test15);
		
		//ProblemExt train2=train15;
		//ProblemExt test2=test15;		
		TFIDF tfidf = new TFIDF();
		tfidf.count(train2);
		ProblemExt [] ret=new ProblemExt [2];
		ret[0]=tfidf.transform(train2);
		ret[1]=tfidf.transform(test2);
		Utils.norml2(ret[0]);
		Utils.norml2(ret[1]);
		return ret;
	}
	public static ProblemExt [] transformData3(ProblemExt train,ProblemExt test) {
		ChiSquare dr=new ChiSquare();
		//dr.setNrFeatures(Params.numFeatures);
		dr.collect(train);
		dr.process();
		ProblemExt train2=dr.remap(train);
		ProblemExt test2=dr.remap(test);
		TFIDF tfidf = new TFIDF();
		tfidf.count(train2);
		ProblemExt [] ret=new ProblemExt [2];
		ret[0]=tfidf.transform(train2);
		ret[1]=tfidf.transform(test2);
		Utils.norml2(ret[0]);
		Utils.norml2(ret[1]);
		return ret;
	}
	public static ProblemExt [] transformData_NB(ProblemExt train,ProblemExt test) {
		Frequency dr=new Frequency();
		dr.setNrFeatures(Params.numFeatures);
		dr.collect(train);
		dr.process();
		ProblemExt train2=dr.remap(train);
		ProblemExt test2=dr.remap(test);
		ProblemExt [] ret=new ProblemExt [2];
		ret[0] = train2;
		ret[1] = test2;
		return ret;
	}
	/**
	 * data transformation for testing clustering feature selection, the hierarchy is only needed to cut down the dataset (at first)
	 */
	public static ProblemExt [] transformData_Clu(ProblemExt train,ProblemExt test,Hierarchy subhier) {
		ParameterExt clustpar=new ParameterExt();
		clustpar.clusters=300;
		clustpar.maxpoints=3000;
		clustpar.select_distance=new L0Distance();
		clustpar.normalization=new L1Distance();
		ClusterWrapper cluster=new ClusterWrapper(new KMeans());
		ClusteringMethod dr=new ClusteringMethod(cluster,new CosDistance(),clustpar);
		ProblemExt train15 = ProblemUtils.getSubProblemWithOldMap(train, subhier, null);
		ProblemExt test15 = ProblemUtils.getSubProblemWithOldMap(test, subhier, train15);		
		dr.collect(train15);
		dr.process();
		ProblemExt train2=dr.remap(train15);
		ProblemExt test2=dr.remap(test15);
		TFIDF tfidf = new TFIDF();
		tfidf.count(train2);
		ProblemExt [] ret=new ProblemExt [2];
		ret[0]=tfidf.transform(train2);
		ret[1]=tfidf.transform(test2);
		Utils.norml2(ret[0]);
		Utils.norml2(ret[1]);
		return ret;
	}
	/**
	 * transforms the data to make the main function a little cleaner
	 */
	public static ProblemExt [] transformData_hfs(ProblemExt train,ProblemExt test) {
		Frequency embdr=new Frequency();
		//RankingMethod embdr=new ChiSquare();
		embdr.setNrFeatures(Params.numFeatures / 10);
		HierarchicalMethod dr=new HierarchicalMethod(embdr);
		dr.setNrFeatures(Params.numFeatures);
		dr.collect(train);
		dr.process();
		ProblemExt train2=dr.remap(train);
		ProblemExt test2=dr.remap(test);
		TFIDF tfidf = new TFIDF();
		tfidf.count(train2);
		ProblemExt [] ret=new ProblemExt [2];
		ret[0]=tfidf.transform(train2);
		ret[1]=tfidf.transform(test2);
		Utils.norml2(ret[0]);
		Utils.norml2(ret[1]);
		return ret;
	}
	/**
	 * performs the training and test
	 * will have LOTS of versions
	 * this one uses ECOC SVM 
	 */
	public static int [] trainAndTest1(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param=new Parameter(Params.LibLinearType,1,0.0001);
		ParameterExt paramecoc=new ParameterExt();
		paramecoc.param=param;
		paramecoc.ecoc_nr=codelength;

		ECOC ecoc=new ECOC();
		ecoc.train(finaltrain,paramecoc);
		int [] pred=ecoc.test(finaltest);
		return pred;
	}
	/**
	 * hierarchical SVM
	 */
	public static int [] trainAndTest_HSVM(ProblemExt finaltrain, ProblemExt finaltest,Hierarchy subhier) {
		Parameter svmparam=new Parameter(Params.LibLinearType,1,0.0001);
		ParameterExt embparam=new ParameterExt();
		embparam.param=svmparam;
		ParameterExt paramhsvm=new ParameterExt();
		paramhsvm.embeddedparam=embparam;
		paramhsvm.embedded=new SVM();
		//paramhsvm.embedded=new RemapClassifier(new NaiveBayes());
		//embparam.knn_k=3;
		//paramhsvm.embedded=new KNN();
		
		finaltrain.hierarchy=subhier;
		//HSVM hsvm=new HSVM();
		HierarchicalClassifiers1 hsvm=new HierarchicalClassifiers1();
		hsvm.train(finaltrain, paramhsvm);
		int [] pred=hsvm.test(finaltest);
		return pred;
	}
	/**
	 * ECOC 2
	 */
	public static int [] trainAndTest_E2(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param=new Parameter(Params.LibLinearType,1,0.001);
		ParameterExt paramecoc=new ParameterExt();
		paramecoc.param=param;
		paramecoc.ecoc_nr=codelength;

		ECOC2 ecoc=new ECOC2();
		ecoc.train(finaltrain,paramecoc);
		int [] pred=ecoc.test(finaltest);
		return pred;
	}
	/**
	 * ECOC 3
	 */
	public static int [] trainAndTest_E3(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param = new Parameter(Params.LibLinearType,1,0.1);
		ParameterExt paramecoc = new ParameterExt();
		paramecoc.param = param;
		paramecoc.ecoc_nr = codelength;

		ECOC3 ecoc = new ECOC3();
		ecoc.train(finaltrain, paramecoc);
		int [] pred = ecoc.test(finaltest);
		return pred;
	}
	/**
	 * ECOC 4
	 */
	public static int [] trainAndTest_E4(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param = new Parameter(Params.LibLinearType,1,0.0001);
		ParameterExt paramecoc = new ParameterExt();
		paramecoc.param = param;
		paramecoc.ecoc_nr = codelength;

		ECOC4 ecoc = new ECOC4();
		ecoc.train(finaltrain, paramecoc);
		int [] pred = ecoc.test(finaltest);
		return pred;
	}
	/**
	 * ECOC 5
	 */
	public static int [] trainAndTest_E5(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param = new Parameter(Params.LibLinearType,1,0.0001);
		ParameterExt paramecoc = new ParameterExt();
		paramecoc.param = param;
		paramecoc.ecoc_nr = codelength;

		ECOC5 ecoc = new ECOC5();
		ecoc.train(finaltrain, paramecoc);
		int [] pred = ecoc.test(finaltest);
		return pred;
	}
	/**
	 * Hierarchical Classificatiers 2
	 */
	public static int [] trainAndTest_HC2(ProblemExt finaltrain, ProblemExt finaltest) {
		ParameterExt p = new ParameterExt();

		HierarchicalClassifiers2 hic = new HierarchicalClassifiers2();
		hic.train(finaltrain, p);
		int [] pred = hic.test(finaltest);
		return pred;
	}
	/**
	 * Bipartite Graph
	 */
	public static int [] trainAndTest_BG(ProblemExt finaltrain, ProblemExt finaltest) {
		ParameterExt p = new ParameterExt();

		BipartiteGraph bg = new BipartiteGraph();
		bg.train(finaltrain, p);
		int [] pred = bg.test(finaltest);
		return pred;
	}
	/**
	 * Hieron (online)
	 */
	public static int [] trainAndTest_Hieron(ProblemExt finaltrain, ProblemExt finaltest, Hierarchy subhier) {
		ParameterExt p = new ParameterExt();

		OnlineHieron hieron = new OnlineHieron();
		finaltrain.hierarchy=subhier;
		hieron.train(finaltrain, p);
		int [] pred = hieron.test(finaltest);
		return pred;
	}
	/**
	 * Hieron (batch)
	 */
	public static int [] trainAndTest_BatchHieron(ProblemExt finaltrain, ProblemExt finaltest, Hierarchy subhier) {
		ParameterExt p = new ParameterExt();

		BatchHieron hieron = new BatchHieron();
		finaltrain.hierarchy=subhier;
		hieron.train(finaltrain, p);
		int [] pred = hieron.test(finaltest);
		return pred;
	}
	/**
	 * ECOC_SA
	 */
	public static int [] trainAndTest_ESA(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param=new Parameter(Params.LibLinearType,1,0.0001);
		ParameterExt paramecoc=new ParameterExt();
		paramecoc.param=param;
		paramecoc.ecoc_nr=codelength;

		ECOC_SA ecoc=new ECOC_SA();
		ecoc.train(finaltrain, paramecoc);
		int [] pred=ecoc.test(finaltest);
		return pred;
	}
	/**
	 * Naive Bayes 
	 */
	public static int [] trainAndTest_NB(ProblemExt finaltrain, ProblemExt finaltest) {
		NaiveBayes nb=new NaiveBayes();
		nb.train(finaltrain, null);
		int [] pred = nb.test(finaltest);
		return pred;
	}
	/**
	 * LIBSVM
	 */
	public static int [] trainAndTest_LIBSVM(ProblemExt finaltrain, ProblemExt finaltest) {
		ParameterExt param=new ParameterExt();
		param.libsvm_par = new svm_parameter();
		param.libsvm_par.svm_type = svm_parameter.C_SVC;
		param.libsvm_par.kernel_type = svm_parameter.LINEAR;
		param.libsvm_par.degree = 3;
		param.libsvm_par.gamma = 100;
		param.libsvm_par.coef0 = 1;
		param.libsvm_par.nu = 0.5;
		param.libsvm_par.cache_size = 40;
		param.libsvm_par.C = 1;
		param.libsvm_par.eps = 1e-3;
		param.libsvm_par.p = 0.1;
		param.libsvm_par.shrinking = 1;
		param.libsvm_par.probability = 0;
		param.libsvm_par.nr_weight = 0;
		param.libsvm_par.weight_label = new int[0];
		param.libsvm_par.weight = new double[0];
		LIBSVM svm = new LIBSVM();
		svm.train(finaltrain, param);
		int [] pred = svm.test(finaltest);
		return pred;
	}
	/**
	 * LIBSVM with probabilities
	 */
	public static int [] trainAndTest_LIBSVMp(ProblemExt finaltrain, ProblemExt finaltest) {
		ParameterExt param=new ParameterExt();
		param.libsvm_par = new svm_parameter();
		param.libsvm_par.svm_type = svm_parameter.C_SVC;
		param.libsvm_par.kernel_type = svm_parameter.LINEAR;
		param.libsvm_par.degree = 3;
		param.libsvm_par.gamma = 100;
		param.libsvm_par.coef0 = 1;
		param.libsvm_par.nu = 0.5;
		param.libsvm_par.cache_size = 40;
		param.libsvm_par.C = 1;
		param.libsvm_par.eps = 1e-3;
		param.libsvm_par.p = 0.1;
		param.libsvm_par.shrinking = 1;
		param.libsvm_par.probability = 1;
		param.libsvm_par.nr_weight = 0;
		param.libsvm_par.weight_label = new int[0];
		param.libsvm_par.weight = new double[0];
		LIBSVM svm = new LIBSVM();
		svm.train(finaltrain, param);
		IndexValue [][] pred = svm.testp(finaltest);
		int [] retpred=Utils.convertProbabilitiesToBestClass(pred);
		return retpred;
	}	
	/**
	 * kNN training and testing
	 */
	public static int [] trainAndTest_KNN(ProblemExt finaltrain, ProblemExt finaltest) {
		ParameterExt param=new ParameterExt();
		param.knn_k=11;

		KNN knn=new KNN();
		knn.train(finaltrain,param);
		int [] pred=knn.test(finaltest);
		return pred;
	}
	/**
	 * testing Hierarchical Refinement
	 */
	public static int [] trainAndTest_HRefi(ProblemExt finaltrain, ProblemExt finaltest,Hierarchy subhier) {
		Parameter svmparam=new Parameter(Params.LibLinearType,1,0.0001);
		ParameterExt embparam=new ParameterExt();
		embparam.param=svmparam;
		ParameterExt paramhrefi=new ParameterExt();
		paramhrefi.embeddedparam=embparam;
		paramhrefi.embedded=new SVM();
		//paramhrefi.embedded=new RemapClassifier(new NaiveBayes());
		//embparam.knn_k=3;
		//paramhrefi.embedded=new KNN();
		
		finaltrain.hierarchy=subhier;
		HRefinement hrefi=new HRefinement();
		hrefi.train(finaltrain, paramhrefi);
		int [] pred=hrefi.test(finaltest);
		return pred;
	}
	/**
	 * testing Hierarchical Refined Experts
	 */
	public static int [] trainAndTest_HRefiE(ProblemExt finaltrain, ProblemExt finaltest,Hierarchy subhier) {
		Parameter svmparam=new Parameter(Params.LibLinearType,1,0.0001);
		ParameterExt embparam=new ParameterExt();
		embparam.param=svmparam;
		ParameterExt paramhrefi=new ParameterExt();
		paramhrefi.embeddedparam=embparam;
		paramhrefi.embedded=new SVM();
		//paramhrefi.embedded=new RemapClassifier(new NaiveBayes());
		//embparam.knn_k=3;
		//paramhrefi.embedded=new KNN();
		
		finaltrain.hierarchy=subhier;
		HRefinedExperts hrefi=new HRefinedExperts();
		hrefi.train(finaltrain, paramhrefi);
		int [] pred=hrefi.test(finaltest);
		return pred;
	}
	/**
	 * Label Propagation  
	 */
	public static int [] trainAndTest_LP(ProblemExt finaltrain, ProblemExt finaltest) {
		ParameterExt p = new ParameterExt();
		LabelPropagation lp = new LabelPropagation();
		DistanceMetric d = new CosSimilarity();
		lp.setDistanceMetric(d);
		int [] pred = lp.ttrain(finaltrain, finaltest, p);
		return pred;
	}
	public static int countCorrectPredictions(int [] original,int [] predicted) {
		if (original.length!=predicted.length) {
			return -1;
		}
		int ret=0;
		for (int i=0;i<original.length;i++) {
			if (original[i]==predicted[i]) {
				ret++;
			}
		}
		return ret;
	}
	/**
	 * 
	 */
	public static Hashtable<Integer,Double> countCorrectPredictions(int [] original,int [] predicted,Hierarchy h) {
		//we count the correctly found documents belonging to a category in this Hashtable
		Hashtable<Integer,Double> ret=new Hashtable<Integer,Double>();
		//we count the number of documents belonging to a category in this Hashtable
		Hashtable<Integer,Double> all=new Hashtable<Integer,Double>();
		for (int i=0;i<original.length;i++) {
			int cat=original[i];
			int [] path=h.getPathToRoot(cat);
			for (int x : path) {
				Double d=all.get(x);
				if (d==null) {
					d=new Double(1);
				}
				else {
					d=new Double(d.doubleValue()+1);
				}
				all.put(x, d);
			}
			if (original[i]==predicted[i]) {
				for (int x : path) {
					Double d=ret.get(x);
					if (d==null) {
						d=new Double(1);
					}
					else {
						d=new Double(d.doubleValue()+1);
					}
					ret.put(x, d);
				}				
			}
		}
		for (Integer x : all.keySet()) {
			Double denom=all.get(x);
			Double numer=ret.get(x);
			if (numer==null) {
				ret.put(x, 0.0);
			}
			else {
				ret.put(x, numer.doubleValue()/denom.doubleValue());
			}
		}
		return ret;
	}
	public static void saveResults(int [] l,String fname) {
		try {
			PrintWriter pw=new PrintWriter(fname);
			for (int i : l) {
				pw.println(i);
			}
			pw.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	public static int [] loadResults(String fname) {		
		try {
			BufferedReader in=new BufferedReader(new FileReader(fname));
			ArrayList<Integer> retl=new ArrayList<Integer>();
			String line;
			while ((line=in.readLine())!=null) {
				retl.add(Integer.parseInt(line));
			}
			in.close();
			int [] ret=new int [retl.size()];
			int offs=0;
			for (int i : retl) {
				ret[offs++]=i;
			}
			return ret;
		}
		catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}	
}
