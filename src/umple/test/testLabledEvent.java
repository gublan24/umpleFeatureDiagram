
package umple.test;
import org.junit.jupiter.api.Test;

import cruise.umple.UmpleConsoleMain;
import cruise.umple.compiler.FeatureModel;
import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleInternalParser;
import cruise.umple.compiler.UmpleModel;

class testLabledEvent {

	@Test
	void test() {
		
	UmpleInternalParser l ;
   // String[] args = {"umpleFile.ump","java"};
//	UmpleConsoleMain.main(args );;
	UmpleFile umpfile = new UmpleFile("BerkeleySPL.ump");
    UmpleModel model = new UmpleModel(umpfile);
    
    
    
 
    
	model.setShouldGenerate(true);
    model.run();
	model.generate();
	System.out.println("Done ... "+model.getGeneratedCode());
	}

}
