package ru.pt.api.dto.process;

import java.util.HashMap;
import java.util.Map;

public class ProcessList {

    private String currentOperation;  //quote or save
    private String dataScope; //prod or dev
    private String phDigest;
    private String ioDigest;
    private String insCompanyCode;
    private Map<String, Object> vars = new HashMap<>();

    public static String QUOTE = "quote";
    public static String SAVE = "save";
    
    public static String PROD = "PROD";
    public static String DEV = "DEV";

    public ProcessList(String currentOperation) {
        this.currentOperation = currentOperation;
        this.phDigest = "";
        this.ioDigest = "";
        this.insCompanyCode = "";
    }

    public String getCurrentOperation() {
        return currentOperation;
    }

    public void setCurrentOperation(String currentOperation) {
        this.currentOperation = currentOperation;
    }

    public String getPhDigest() {
        return phDigest;
    }

    public void setPhDigest(String phDigest) {
        this.phDigest = phDigest;
    }

    public String getIoDigest() {
        return ioDigest;
    }

    public void setIoDigest(String ioDigest) {
        this.ioDigest = ioDigest;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public void setVars (Map<String, Object> vars) {
        this.vars.putAll(vars);
    }

    public void setDataScope(String value){
        this.dataScope = value;
    }

    public String getDataScope(){
        return this.dataScope;
    }
    public String getInsCompanyCode(){
        return this.insCompanyCode;
    }

    public void setInsCompanyCode(String insCompanyCode){
        this.insCompanyCode = insCompanyCode;
    }
}
