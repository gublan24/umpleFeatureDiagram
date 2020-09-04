package umple.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import cruise.umple.compiler.FeatureModel;
import cruise.umple.compiler.FeatureNode;
import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleInternalParser;
import cruise.umple.compiler.UmpleModel;

class TestUmple {

	@Test
	void test() {
		UmpleFile umpfile = new UmpleFile("reqSt.ump");
		UmpleModel model = new UmpleModel(umpfile);	
		UmpleInternalParser pp;
		FeatureNode d;
		model.setShouldGenerate(true);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();
	}

}
