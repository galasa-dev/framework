package dev.galasa.framework.api.ras.internal.mocks;

public enum MockErrorMessage {
    ERROR_TEST_000(000,"This Error Message contains no parameters!"),
    ERROR_TEST_001(001,"This Error Message contains one paramater: {0}"),
    ERROR_TEST_005(005,"This Error Message contains five paramaters: {0} , {1} , {2} , {3} , {4}")
    ;

    private String template ;
    private int templateNumber;

    private MockErrorMessage(int templateNumber , String template) {
        this.template = template ;
        this.templateNumber = templateNumber ;
    }

    public String toString() {
        return this.template ;
    }

    public int getTemplateNumber() {
        return this.templateNumber;
    }
}

