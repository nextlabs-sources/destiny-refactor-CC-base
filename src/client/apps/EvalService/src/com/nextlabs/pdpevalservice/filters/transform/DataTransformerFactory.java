package com.nextlabs.pdpevalservice.filters.transform;

public class DataTransformerFactory {
	
	private static DataTransformerFactory instance = new DataTransformerFactory();

	private IDataTransformer jsonTransformer = null;
	
	private IDataTransformer xacmlTransformer = null;

	public static final String CONTENTTYPE_XML = "application/xml";
	
	public static final String CONTENTTYPE_XACML_XML = "application/xacml+xml";

	public static final String CONTENTTYPE_JSON = "application/json";
	
	public static final String DATATYPE_JSON = "JSON";
	
	public static final String DATATYPE_XACML = "XACML";
	
	private DataTransformerFactory(){
		jsonTransformer =  new JSONDataTransformer();
		xacmlTransformer = new XACMLDataTransformer();
	}
	
	public static DataTransformerFactory getInstance(){
		return instance;
	}
	
	public IDataTransformer getDataTransformer(String contentType){
		if(contentType.equalsIgnoreCase(CONTENTTYPE_JSON)){
			return jsonTransformer;
		}else if(contentType.equalsIgnoreCase(CONTENTTYPE_XML) ||
				contentType.equalsIgnoreCase(CONTENTTYPE_XACML_XML)){
			return xacmlTransformer;
		}
		return null;
	}

}
