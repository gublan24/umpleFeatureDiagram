package umple.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.logicng.datastructures.Assignment;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.io.parsers.ParserException;
import org.logicng.io.parsers.PropositionalParser;
import org.logicng.solvers.MiniSat;
import org.logicng.solvers.SATSolver;

import cruise.umple.compiler.FeatureModel;
import cruise.umple.compiler.FeatureNode;
import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleInternalParser;
import cruise.umple.compiler.UmpleModel;
import umple.featureDiagram.FeatureModelPropositionalGenerator;

class TestTestFeatureModelToPropositionalLogic {
	
	final FormulaFactory f = new FormulaFactory();
	final PropositionalParser p = new PropositionalParser(f);

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
	
	@Test
	public void testOptinalSPLPropositinalGenerator()
	{
		UmpleFile umpfile = new UmpleFile("HelloWorldSPL.ump");
		UmpleModel model = new UmpleModel(umpfile);	
		model.setShouldGenerate(false);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();
		FeatureNode rootNode = fmodel.getRootFeatures().get(0);
		String propositionalFormula = FeatureModelPropositionalGenerator.getPropostionalFormualFromRootFeatureNode(rootNode);
		System.out.println(propositionalFormula);
		try {
			final Formula formula = p.parse(propositionalFormula);
			final SATSolver miniSat = MiniSat.miniSat(f);
			miniSat.add(formula);
			List<Assignment> assignList= miniSat.enumerateAllModels();
			int solutionCount = assignList.size();
			assertEquals(16, solutionCount);
		} catch (ParserException e) {
			  fail(e.getMessage());
		}
		
	}
	@Test
	public void testDisjunctiveSPLPropositinalGenerator()
	{
		UmpleFile umpfile = new UmpleFile("HelloWorldSPL_or.ump");
		UmpleModel model = new UmpleModel(umpfile);	
		model.setShouldGenerate(false);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();
		FeatureNode rootNode = fmodel.getRootFeatures().get(0);
		String propositionalFormula = FeatureModelPropositionalGenerator.getPropostionalFormualFromRootFeatureNode(rootNode);
		System.out.println(propositionalFormula);
		try {
			final Formula formula = p.parse(propositionalFormula);
			final SATSolver miniSat = MiniSat.miniSat(f);
			miniSat.add(formula);
			List<Assignment> assignList= miniSat.enumerateAllModels();
			int solutionCount = assignList.size();
			assertEquals(15, solutionCount);			
		} catch (ParserException e) {
			  fail(e.getMessage());
		}
		
	}
	@Test
	public void testConjuntiveSPLPropositinalGenerator()
	{
		UmpleFile umpfile = new UmpleFile("HelloWorldSPL_and.ump");
		UmpleModel model = new UmpleModel(umpfile);	
		model.setShouldGenerate(false);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();
		FeatureNode rootNode = fmodel.getRootFeatures().get(0);
		String propositionalFormula = FeatureModelPropositionalGenerator.getPropostionalFormualFromRootFeatureNode(rootNode);
		System.out.println(propositionalFormula);
		try {
			final Formula formula = p.parse(propositionalFormula);
			final SATSolver miniSat = MiniSat.miniSat(f);
			miniSat.add(formula);
			List<Assignment> assignList= miniSat.enumerateAllModels();
			int solutionCount = assignList.size();
			assertEquals(1, solutionCount);
			
		} catch (ParserException e) {
			  fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testMultipleSPLPropositinalGenerator()
	{
		UmpleFile umpfile = new UmpleFile("FeatureModelWith_opt_xor.ump");
		UmpleModel model = new UmpleModel(umpfile);	
		model.setShouldGenerate(false);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();
		FeatureNode rootNode = fmodel.getRootFeatures().get(0);
		String propositionalFormula = FeatureModelPropositionalGenerator.getPropostionalFormualFromRootFeatureNode(rootNode);
		System.out.println(propositionalFormula);
		
		try {
			final Formula formula = p.parse(propositionalFormula);
			final SATSolver miniSat = MiniSat.miniSat(f);
			miniSat.add(formula);
			List<Assignment> assignList= miniSat.enumerateAllModels();
			int solutionCount = assignList.size();
			assertEquals(10, solutionCount);			
		} catch (ParserException e) {
			  fail(e.getMessage());
		}
		
	}

}
