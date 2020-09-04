package umple.test;

import cruise.umple.compiler.FeatureModel;
import cruise.umple.compiler.FeatureNode;
import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleInternalParser;
import cruise.umple.compiler.UmpleModel;

public class TestReqStatement {

	public static void main(String[] args) {
		UmpleFile umpfile = new UmpleFile("reqSt.ump");
		UmpleModel model = new UmpleModel(umpfile);	
		UmpleInternalParser pp;
		FeatureNode d;
		model.setShouldGenerate(true);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();

	}

}
