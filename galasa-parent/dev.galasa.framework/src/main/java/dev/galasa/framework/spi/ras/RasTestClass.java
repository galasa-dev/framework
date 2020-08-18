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


    // public TestClass testClass(String testClass) {
    //     this.testClass = testClass;
    //     return this;
    // }

    // public TestClass bundleName(String bundleName) {
    //     this.bundleName = bundleName;
    //     return this;
    // }

    // @Override
    // public boolean equals(Object o) {
    //     if (o == this)
    //         return true;
    //     if (!(o instanceof TestClass)) {
    //         return false;
    //     }
    //     TestClass testClass = (TestClass) o;
    //     return Objects.equals(testClass, testClass.testClass) && Objects.equals(bundleName, testClass.bundleName);
    // }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(testClass, bundleName);
    // }

    @Override
    public String toString() {
        return "{" +
            " testClass:'" + getTestClass() + "'" +
            ", bundleName:'" + getBundleName() + "'" +
            "}";
    }

    
}