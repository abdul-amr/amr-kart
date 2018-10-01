package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MergeStatus {
	
	private Map<String, Map<String, String>>  sourceMap;
	private Map<String, Map<String, String>>  destinationMap;
	private Map<String, Map<String, String>>  finalMap;
	private Map<String, Map<String, String>>  missingMap;
	
	public MergeStatus(){
		this.sourceMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.destinationMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.finalMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.missingMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}
	
	public static void main(String[] args) throws IOException {
		MergeStatus mergeStatus = new MergeStatus();
		mergeStatus.sourceMap = mergeStatus.loadSourceFile();
		mergeStatus.destinationMap = mergeStatus.loadDestinationFile();
		
		mergeStatus.mergeStatusFiles();
		
		mergeStatus.writeFile();
		
	}

	private void writeFile() throws IOException {
		// TODO Auto-generated method stub
		File fout = new File("./mergedStatus.txt");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for(String entry : this.finalMap.keySet()){
	    	String printS = entry+":";
	    	Map<String, String> modelMap = this.finalMap.get(entry);
	    	for(String variety:  modelMap.keySet()){
	    		String values = modelMap.get(variety);
	    		printS += variety+"-"+values+"\t";
	    	}
	    	bw.write(printS);
			bw.newLine();
	    }

		bw.newLine();
		bw.newLine();
		bw.newLine();
		bw.write("//Missing Phones or New Phone ");
		bw.newLine();
		bw.newLine();
		
		for(String entry : this.missingMap.keySet()){
	    	String printS = entry+":";
	    	Map<String, String> modelMap = this.missingMap.get(entry);
	    	for(String variety:  modelMap.keySet()){
	    		String values = modelMap.get(variety);
	    		printS += variety+"-"+values+"\t";
	    	}
	    	bw.write(printS);
			bw.newLine();
	    }
		
		bw.close();
	}

	private void mergeStatusFiles() {
		
		for(String key : this.destinationMap.keySet()){
			Map<String, String> sourceModels = this.sourceMap.get(key);
			if(sourceModels != null && sourceModels.size() > 0){
				Map<String, String> destinationModels = this.destinationMap.get(key);
				for(String modelKey : destinationModels.keySet()){
					String value = sourceModels.get(modelKey);
					if(value == null || "".equals(value)){// If the value is not there in source then take it from destination
						value = destinationModels.get(modelKey);
					}
					if(this.finalMap.get(key) != null){
						//phone exists
						Map<String, String> finalModelMap = this.finalMap.get(key);
						finalModelMap.put(modelKey, value);
						this.finalMap.put(key, finalModelMap);
					}else{
						//phone does not exists
						Map<String, String> finalModelMap = new TreeMap<>();
						finalModelMap.put(modelKey, value);
						this.finalMap.put(key, finalModelMap);
					}
				}
			}else {
				//missing phone
				this.missingMap.put(key, this.destinationMap.get(key));
			}
		}
	}

	private Map<String, Map<String, String>> loadSourceFile() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br1 = null;
        String sCurrentLine;
        br1 = new BufferedReader(new FileReader("./sourceFile.txt"));
        while ((sCurrentLine = br1.readLine()) != null) {
            if(sCurrentLine != null && !"".equals(sCurrentLine)){
            	String key = sCurrentLine.split(":")[0];
            	String value = sCurrentLine.split(":")[1];
            	String[] models = value.split("\t");
            	Map<String, String> modelMap = new TreeMap<>();
            	for(String eachModel : models){
            		modelMap.put(eachModel.split("-")[0], eachModel.split("-")[1]);
            	}
            	sourceMap.put(key, modelMap);
            }
        }
        
        return sourceMap;
	}

	private Map<String, Map<String, String>> loadDestinationFile() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br1 = null;
        String sCurrentLine;
        br1 = new BufferedReader(new FileReader("./destinationFile.txt"));
        while ((sCurrentLine = br1.readLine()) != null) {
            if(sCurrentLine != null && !"".equals(sCurrentLine)){
            	String key = sCurrentLine.split(":")[0];
            	String value = sCurrentLine.split(":")[1];
            	String[] models = value.split("\t");
            	Map<String, String> modelMap = new TreeMap<>();
            	for(String eachModel : models){
            		modelMap.put(eachModel.split("-")[0], eachModel.split("-")[1]);
            	}
            	destinationMap.put(key, modelMap);
            }
        }
        
        return destinationMap;
	}
}
