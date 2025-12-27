package ru.pt.api.dto.process;

public class ProcessList {

    private String currentOperation;  //quote or save
    private String phDigest;
    private String ioDigest;

    public static String QUOTE = "quote";
    public static String SAVE = "save";
    
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
}
