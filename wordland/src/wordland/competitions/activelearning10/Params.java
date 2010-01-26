package wordland.competitions.activelearning10;

import java.util.ArrayList;

import liblinear.SolverType;

public class Params {
	//public static final String rootPath = "G:\\!data\\al\\";
	public static final String rootPath = "C:\\corpus\\al\\";
	public static final String passwfile = "C:\\passw.txt";
	
//	public static final String dataSet = "alex";
//	public static final String dataSet = "hiva";
	public static final String dataSet = "ibn_sina";
//	public static final String dataSet = "nova";
//	public static final String dataSet = "orange"; //many NaN's!!!
//	public static final String dataSet = "sylva";
//	public static final String dataSet = "zebra"; //NaN's & Inf's!!!

//	public static final int numFeatures = 50000;
	public static final SolverType LibLinearType = SolverType.L2R_L2LOSS_SVC_DUAL;
}

//SVM (linear):
//====
//alex:
//===============
//Accuracy: 0.869
//Macro F-measure: 0.831504205647966
//Macro Precision: 0.836130090474101
//Macro Recall: 0.8269292245585946
//Tree Induced Error: -1.0
//===============
//hiva:
//===============
//Accuracy: 0.9671962135057875
//Macro F-measure: 0.6853013425825535
//Macro Precision: 0.8585069645072072
//Macro Recall: 0.5702518154091727
//Tree Induced Error: -1.0
//===============
//ibn_sina:
//===============
//Accuracy: 0.9531898465399092
//Macro F-measure: 0.9503363173446164
//Macro Precision: 0.9510678957190701
//Macro Recall: 0.9496058635913305
//Tree Induced Error: -1.0
//===============
//nova:
//===============
//Accuracy: 0.9758553375115586
//Macro F-measure: 0.9701735062210809
//Macro Precision: 0.9723199314828888
//Macro Recall: 0.9680365366811102
//Tree Induced Error: -1.0
//===============
//sylva:
//===============
//Accuracy: 0.9856112136149588
//Macro F-measure: 0.9372175315275275
//Macro Precision: 0.9414359677624307
//Macro Recall: 0.9330367310381835
//Tree Induced Error: -1.0
//===============
