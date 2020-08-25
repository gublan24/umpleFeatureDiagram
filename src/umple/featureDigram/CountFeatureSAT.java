package umple.featureDigram;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import cruise.umple.compiler.FeatureLink;
import cruise.umple.compiler.FeatureLink.FeatureConnectingOpType;
import cruise.umple.compiler.FeatureModel;
import cruise.umple.compiler.FeatureNode;
import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleModel;
import java.util.logging.Logger;
public class CountFeatureSAT {


	public static void main(String[] args) {


		UmpleFile umpfile = new UmpleFile("BerkeleySPL.ump");
		UmpleModel model = new UmpleModel(umpfile);

		model.setShouldGenerate(true);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();

		FeatureNode rootNode = fmodel.getNode().get(0);
		String s = getAllValid(rootNode);


		FeatureLink fLink;
		model.generate();
		
	    try {
	        FileWriter myWriter = new FileWriter("logBerkeleySPL.txt");
	        myWriter.write(s);
	        myWriter.close();
	        System.out.println("Successfully wrote to the file.");
	      } catch (IOException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	      }  


		

	}



	public static String getAllValid(FeatureNode featureNode)
	{
		String requiredFeatures=" ";		

		if(featureNode == null)
			return "";
		else if(featureNode.getSourceFeatureLink() == null)
			return "";

		ArrayList<String> optionalFeatures = new ArrayList<String>();
		ArrayList<String> orFeatures = new ArrayList<String>();
		ArrayList<String> xorFeatures = new ArrayList<String>();


		List<FeatureLink> outgoingLinks = featureNode.getSourceFeatureLink();

		String andGroup ="";
		String extaAnd = "^";

		for(FeatureLink link : outgoingLinks)
		{
			if(link.getFeatureConnectingOpType().equals(FeatureConnectingOpType.Include))
			{
				String inner = getAllValid(link.getTargetFeature().get(0));
				andGroup +=link.getTargetFeature().get(0).getName()+" ^ ";		
				if(! inner.trim().equals(""))
					andGroup +=" ( "+inner  +") ^ ";

			}
			if(link.getFeatureConnectingOpType().equals(FeatureConnectingOpType.Optional))
			{
				String inner = getAllValid(link.getTargetFeature().get(0));
				if(!inner.trim().equals(""))
					optionalFeatures.add(link.getTargetFeature().get(0).getName() + " ^ { "+inner+"}");
				else
					optionalFeatures.add(link.getTargetFeature().get(0).getName() );
			}
			if(link.getFeatureConnectingOpType().equals(FeatureConnectingOpType.Disjunctive))
			{
				String inner = getAllValid(link.getTargetFeature().get(0));
				if(!inner.trim().equals("or"))
				{
					orFeatures.add(link.getTargetFeature().get(0).getName() );
				}
				else 
					orFeatures.add( " ^ ("+inner+")");
			
			}
			
			if(link.getFeatureConnectingOpType().equals(FeatureConnectingOpType.XOR))
			{
				String inner = getAllValid(link.getTargetFeature().get(0));
				if(!link.getTargetFeature().get(0).getName().trim().equals("xor"))
				{
					xorFeatures.add(link.getTargetFeature().get(0).getName() );
				}
				else 
					xorFeatures.add( " ^ ( "+inner+") ");
			
			}


		}
		if(andGroup.length() > 2 && optionalFeatures.isEmpty() && orFeatures.isEmpty() )
		{
			andGroup = andGroup.substring(0, andGroup.lastIndexOf("^")-1);
			requiredFeatures+= " "+andGroup;
		}
		else
		{
			requiredFeatures+= " "+andGroup ;	
		}
		
	//	requiredFeatures = obatinFromList(requiredFeatures, optionalFeatures,  a -> optCombinations(a));
		requiredFeatures += combinationsAsString( optionalFeatures.toArray(new String[optionalFeatures.size()]), " v TRUE \n");
	//	requiredFeatures = obatinFromList(requiredFeatures, orFeatures,  a -> combinations(a));	//combinationsAsString
		requiredFeatures += combinationsAsString(orFeatures.toArray(new String[orFeatures.size()]) , "\n");
		requiredFeatures = obatinFromList(requiredFeatures, xorFeatures,  a -> combinations(a));	//xorFeatures, 

		
		return requiredFeatures;

	}



	private static String obatinFromList(String predicateString, ArrayList<String> featureArrayList, Function<String[] , List<ArrayList<String>>> combinationMrthod) {
		if(featureArrayList.size() > 0)
		{
			String[] featureAsArray = new String[featureArrayList.size()];
			for (int i = 0; i < featureAsArray.length; i++) {
				featureAsArray[i]= featureArrayList.get(i);
			}
			
			List<ArrayList<String>> allCombinationOfInputList = combinationMrthod.apply(featureAsArray);
			if(allCombinationOfInputList.size() > 0)
			{
				predicateString +=" [ ";

				for (ArrayList<String> solution : allCombinationOfInputList)
				{
					predicateString +=" ( ";
					for(String single :solution)
					{
						predicateString +="  "+single.toString() ;
						if(solution.indexOf(single)+1 != solution.size())
							predicateString +=" ^ ";
					}
					predicateString +=" ) ";
					if(allCombinationOfInputList.indexOf(solution)+1 != allCombinationOfInputList.size())

						predicateString +=" v ";
					predicateString +=" \n ";

				}

				predicateString +=" ] ";


			}
			predicateString +=" \n ";

		}
		return predicateString;
	}


	public static ArrayList<ArrayList<String> > combinations ( String[] inputArray ) {

		ArrayList<ArrayList<String> > combinationList = new ArrayList<ArrayList<String> > ();
		// Start i at 1, so that we do not include the empty set in the results
		for ( long i = 1; i < Math.pow(2, inputArray.length); i++ ) {
			ArrayList<String> portList = new ArrayList<String>();
			for ( int j = 0; j < inputArray.length; j++ ) {
				if ( (i & (long) Math.pow(2, j)) > 0 ) {
					// Include j in set
					portList.add(inputArray[j]);
				}
			}
			combinationList.add(portList);

		}
		return combinationList;
	}

	public static ArrayList<ArrayList<String> > optCombinations ( String[] inputArray ) {
		ArrayList<ArrayList<String> > combinationList = combinations(inputArray);
		ArrayList<String> trueValueAsList = new ArrayList<String>();
		trueValueAsList.add("TRUE");
		combinationList.add(trueValueAsList);
		return combinationList;
	}
	
	public static String combinationsAsString(String[] inputArray , String exta) {
		String result = "";
		int arraySize = inputArray.length;
		if (arraySize < 1) {
			return result;
		} else if (arraySize == 1) {
			return "-("+inputArray[0]+exta+")-";
		} else {
			
			result = "-("+inputArray[0];
			for (int i = 1; i < arraySize; i++) {
				result = result + " v " + inputArray[i];
			}
			result +=  exta+")-";
		}
		return result ;
	}

}



