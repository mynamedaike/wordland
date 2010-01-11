package wordland.competitions.lsthc09;

import liblinear.SolverType;

public class Params {
	public static final String rootPath = "C:\\corpus\\large_lshtc_dataset\\Task1_Train_CrawlData_Test_CrawlData\\";
	public static final String hierarchyPath = "C:\\corpus\\large_lshtc_dataset\\";
	//public static final String rootPath = "C:\\corpus\\dry-run_lshtc_dataset\\Task1_Train_CrawlData_Test_CrawlData\\";
	//public static final String hierarchyPath = "C:\\corpus\\dry-run_lshtc_dataset\\";

	//public static final String rootPath = "G:\\!data\\lshtc\\dry-run_lshtc_dataset\\Task1_Train_CrawlData_Test_CrawlData\\";
	//public static final String hierarchyPath = "G:\\!data\\lshtc\\dry-run_lshtc_dataset\\";
	//public static final String rootPath = "G:\\!data\\lshtc\\proba1\\";
	//public static final String hierarchyPath = "G:\\!data\\lshtc\\proba1\\";

	//public static final String rootPath = "G:\\!data\\lshtc\\reuters\\";
	//public static final String rootPath = "G:\\!data\\lshtc\\proba\\";

	public static final int numFeatures = 50000;
	public static final double probEcoc = 0.5;
	public static final int ecocCodeLength = 1000;
	public static final double sigmaClassDist = 2;
	public static final SolverType LibLinearType = SolverType.L2R_L2LOSS_SVC_DUAL;
	public static final int Chi2Type = 0; // max/0 or avg/1
	public static final int normSVM = 0; // 0-false, 1-true; used in ECOC2
}
