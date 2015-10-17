package ai.logic;

import java.util.Arrays;

public class Weights {
	/** AC = config[0];  ==> Adjacent Count 		**/
	/** MB = config[1];  ==> Missing Below  		**/
	/** EC = config[2];  ==> Edge Count     		**/
	/** DG = config[3];  ==> Distance From Bottom 	**/
	/** RC = config[4];  ==> Rows Cleared   		**/ 
	/** RD = config[5];  ==> Density				**/
	/** BF = config[6];  ==> Buried Factor  		**/
	/** BG = config[7];  ==> Bad Gap Beside Shape	**/
	/** CF = config[8];  ==> Covered Factor			**/
	/** PS = config[9];  ==> ProximityToSpawn			**/
	private boolean configLoaded = false;
	protected double AC, MB, EC, DG, RC, DS, BF, BG, CF, PS;
	//protected static double[] defaults = new double[] {10,10,10,1, 2.5, 3, 1,2.5, 2.};
	//protected static double[] defaults = new double[] {10,20,8,1, 2.5, 3, 1,4,0};
	//protected static double[] defaults = new double[] {10,11,10,0, 1, 0, 0,0,0};
	//protected static double[] defaults = new double[] {10,300,10,1, 2.5, 3, 1,5,0};
	//protected static double[] defaults = new double[] {20,30,21,0,430000, 0, 0,0,1, 0}; //Holly cow 95 000
//	protected static double[] defaults = new double[] {20,30,21,0, 4300, 0, 0, 2,1,0}; //36000
	//protected static double[] defaults = new double[] {1,1,1,1, 5, 0, 0, 1,0,0}; 
	
	//protected static double[] defaults = new double[] {10,12,10,3, 15, 0, 0,0,0,0}; //36000, not 36 000
	protected static double[] defaults = new double[] {15,16,11,0.5, 3, 0, 0,1,2,5};
	//protected static double[] defaults = new double[] {10,11,10,3, 20, 0, 0,0,0,0}; //20 000
//	protected static double[] defaults = new double[] {10,11,10,0,10,0,0,4,4}; //Holly cow 95 000
//	protected static double[] defaults = new double[] {10,12,11,1, 10, 3, 1,5,0,0}; //Holly cow 95 000
	//protected static double[] defaults = new double[] {4,9,5,1, 2, 0, 0,1};
	//protected static double[] defaults = new double[] {5,5,65,0,1,0 , 1,0};	
	//protected static double[] defaults = new double[] {10,10,8,1,0,0 , 1,0};
	double [] config;
	public Weights (double [] weights) {
		config = Arrays.copyOf(weights, defaults.length);
	}
	public Weights () {
		config = defaults;
	}
	public double [] getConfig () {
		return config;
	}
}
