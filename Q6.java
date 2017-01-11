import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


//uniqueValues row=attributes 
//attributes row= one entity

public class Q6 {
	static ArrayList<ArrayList<String>> attributes;
	static ArrayList<ArrayList<String>> uniqueValues;
	static int noOfAttributes, noOfData;
	static ArrayList<ArrayList<Integer>> countAttributes;
	static double [] InfoGain;
	static double [] GainRatio;
	
	public static void readFile(String fileName){

		String line;
		int rowIndex=0;
		try{
			BufferedReader br =new BufferedReader(new FileReader(fileName));
			
			if((line=br.readLine())!=null){
				if(rowIndex>=attributes.size()){
					attributes.add(new ArrayList<String>());
				}
				for(String lineParts:line.split("\\s+")){
					attributes.get(rowIndex).add(lineParts);
				}
				noOfAttributes=attributes.get(rowIndex).size()-1;
				rowIndex++;
			}
			do{
			if((line=br.readLine())==null)	break;

				//System.out.println(line);
				if(rowIndex>=attributes.size()){
					attributes.add(new ArrayList<String>());
				}
				for(String lineParts:line.split("\\s+")){
					attributes.get(rowIndex).add(lineParts);
				}
				rowIndex++;
			}while(true);
			noOfData=attributes.size()-1;
			//System.out.println( noOfAttributes + " "+ noOfData);
			br.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{}

	}

	public static void findUnique(){
		int rowIndex=1, colIndex=0,row=0,tempRowCount=0, cntCol=0;
		
		uniqueValues.add(new ArrayList<String>());
		for(colIndex=1,row=0;colIndex<=noOfAttributes;colIndex++,row++){	//first value found is always unique
			uniqueValues.add(new ArrayList<String>());
			uniqueValues.get(row).add(attributes.get(1).get(colIndex));
			
			countAttributes.add(new ArrayList<Integer>());
			countAttributes.get(row).add(1);
		}
		for(colIndex=1;colIndex<=noOfAttributes;colIndex++){
			for(rowIndex=2;rowIndex<=noOfData;rowIndex++){
				tempRowCount=colIndex-1;
				if(!(uniqueValues.get(tempRowCount).contains(attributes.get(rowIndex).get(colIndex)))){
					uniqueValues.get(tempRowCount).add(attributes.get(rowIndex).get(colIndex));		//not found add in unique
					countAttributes.get(tempRowCount).add(1);
				}
				else{		//found increase count
					cntCol=uniqueValues.get(tempRowCount).indexOf(attributes.get(rowIndex).get(colIndex));
					countAttributes.get(tempRowCount).set(cntCol,countAttributes.get(tempRowCount).get(cntCol)+1);
				}
			}
		}
	}
	
	public static double calcEntropy(int colIndex, String Value){	//index acco. attribute list
		double entropy;
		int index, dataSet=0;
		double posData=0, negData=0;
			
		for(index=1;index <= noOfData;index++){
			if(colIndex==noOfAttributes)	dataSet=noOfData;
			else if(attributes.get(index).get(colIndex).equals(Value))	dataSet++;
			
			//count positive
			if(attributes.get(index).get(noOfAttributes).equals("Yes") && attributes.get(index).get(colIndex).equals(Value)){
				posData++;
			}
			else{
				//count negative
				if(colIndex==noOfAttributes)	negData++;
				else if(attributes.get(index).get(noOfAttributes).equals("No") && attributes.get(index).get(colIndex).equals(Value))
					negData++;
			}
		}
		//System.out.println(Value+ " "+ posData +" " + negData+ " "+ dataSet);
		posData= posData/dataSet;
		negData= negData/dataSet;
		
		if(posData==1 || negData==1){
			entropy=0;
		}
		else	//claculate entropy
			entropy= 0 - posData*(Math.log(posData)/ Math.log(2))- negData*(Math.log(negData)/ Math.log(2));
		
		//System.out.println("Entropy:" +entropy);
		return entropy;
	}

	public static double calcSplitFeature(int rowIndex){	//index acco. countAttribute/unique list
		double entropy=0,partEn=0;
		int dataSize=countAttributes.get(rowIndex).size();
		double dataCount=0;
			
		for(int i=0;i<dataSize;i++){
			//count data
			dataCount= (countAttributes.get(rowIndex).get(i));
			dataCount/= noOfData;
			
			if(dataCount==1){
				partEn=0;
			}
			else{
				partEn = dataCount*(Math.log(dataCount)/ Math.log(2));
			}
			entropy= entropy - partEn;	//splitting feature
		}
		//System.out.println("Split Entropy:" +entropy);

		return entropy;
	}

	public static double calcConditionalEntropy(int RowIndex){	//index acco. to unique list
		double condEntropy=0,count=0;
		int i=0;
		
		for(String v: uniqueValues.get(RowIndex)){
			count=0;
			for(i=0;i<=noOfData;i++){
				if(attributes.get(i).get(RowIndex+1).equals(v))
					count++;
			}
			count= count/noOfData;
			condEntropy = (condEntropy) - ((count)*(calcEntropy(RowIndex+1, v)));

			//System.out.println("count:"+count);
			//System.out.println("cond entropy:"+condEntropy+"\n");
		}
		return condEntropy;
	}

	public static int calcGainRatio(){
		int [] count= new int[noOfAttributes-1];
		int temp=0;
		double maxRatio=0;

		for(int i=0;i<noOfAttributes-1;i++){
			//information gain
			InfoGain[i]=calcEntropy(noOfAttributes, "Yes") + calcConditionalEntropy(i);
			//System.out.println("InfoGain["+i+ "]: "+ InfoGain[i]);
			
			count[i]=uniqueValues.get(i).size();
			//System.out.println("uni Count:" + count[i]);
			
			//gain ratio
			GainRatio[i]=InfoGain[i]/calcSplitFeature(i);
			if(maxRatio < GainRatio[i]){
				maxRatio = GainRatio[i];
				temp=i;
			}
			
			//System.out.println("GainRatio["+i+ "]: "+ GainRatio[i]+ "\nMax: " +maxRatio + "\n");
			
		}
		
		return temp;
	}
	public static void main(String args []){
		attributes= new ArrayList<ArrayList<String>>();
		uniqueValues= new ArrayList<ArrayList<String>>();
		countAttributes= new ArrayList<ArrayList<Integer>>();

		String inFile="input.txt";
		readFile(inFile);

		InfoGain = new double[noOfAttributes-1];		//first and last colomn are skipped
		GainRatio = new double [noOfAttributes-1];
		findUnique();
		
		int bestFit= calcGainRatio()+1;		//as first colomn is skipped
		System.out.println("Best feature to be root: "+attributes.get(0).get(bestFit));
	}
}
