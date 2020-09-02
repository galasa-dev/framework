package dev.galasa.framework.spi.ras;


public class RasTestClass {
    
    public String testClass;
    public String bundleName;

    public RasTestClass(String testClass, String bundleName) {
        this.testClass = testClass;
        this.bundleName = bundleName;
    }

    public String getTestClass() {
        return this.testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    
}