package ru.pt.api.dto.process;

import java.util.HashMap;
import java.util.Map;

public class ProcessList {

    private String currentOperation;  //quote or save
    private String productVersionStatus; //prod or dev
    private String phDigest;
    private String ioDigest;
    private Map<String, Object> vars = new HashMap<>();

    public static String QUOTE = "quote";
    public static String SAVE = "save";
    
    public static String PROD = "prod";
    public static String DEV = "dev";

    public ProcessList(String currentOperation) {
        this.currentOperation = currentOperation;
        this.phDigest = "";
        this.ioDigest = "";
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

    public void setProductVersionStatus(String value){
        this.productVersionStatus = value;
    }

    public String getProductVersionStatus(){
        return this.productVersionStatus;
    }
}
