package com.nextlabs.pdpevalservice.eval;

public class EvaluationAdapterFactory {
	
	public static final String COMM_TYPE_RMI = "COMM_TYPE_RMI";
	
	public static final String COMM_TYPE_API = "COMM_TYPE_API";
	
	private static EvaluationAdapterFactory instance = new EvaluationAdapterFactory();
	
	private IEvalAdapter rmiEvalAdapter = null;

	private IEvalAdapter apiEvalAdapter = null;
	
	private EvaluationAdapterFactory(){
	}
	
	public static EvaluationAdapterFactory getInstance(){
		return instance;
	}
	
	public IEvalAdapter getAdapter(String commType){
		if(COMM_TYPE_API.equalsIgnoreCase(commType)){
			if(apiEvalAdapter==null){
				apiEvalAdapter = new APIEvalAdapter();
			}
			return apiEvalAdapter;
		}else if(COMM_TYPE_RMI.equalsIgnoreCase(commType)){
			if(rmiEvalAdapter==null){
				rmiEvalAdapter = new RMIEvalAdapter();
			}
			return rmiEvalAdapter;
		}
		return null;
	}

}
