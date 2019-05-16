package io.ejat.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.ejat.framework.spi.FrameworkException;
import io.ejat.framework.spi.Result;
import io.ejat.framework.spi.teststructure.TestMethod;

public class GenericMethodWrapper {
	
    public static final String LOG_METHOD_BEFORE_CLASS = " type=BeforeClass";
    public static final String LOG_METHOD_BEFORE = " type=Before";
    public static final String LOG_METHOD_TEST = " type=Test";
    public static final String LOG_METHOD_AFTER = " type=After";
    public static final String LOG_METHOD_AFTER_CLASS = " type=AfterClass";

    private Log logger = LogFactory.getLog(GenericMethodWrapper.class);

	public enum Type {
		BeforeClass,
		AfterClass,
		Before,
		After,
		Test
	}
	
	private Method   testMethod;
	private Class<?> testClass;
	private Type     type;
	private Result   result;
	
	private TestMethod testStructureMethod;

	public GenericMethodWrapper(Method testMethod, Class<?> testClass, Type type) {
		this.testMethod = testMethod;
		this.testClass  = testClass;
		this.type       = type;
	}
	
    /** 
     * Run the supplied method
     * 
     * @param method the method
     * @param methodType the method type for logging
     * @throws TestRunException
     */
	public void invoke(@NotNull TestRunManagers managers, Object testClassObject) throws TestRunException {
        try {
        	String methodType = ",type=" + type.toString();
        	Result ignored = managers.anyReasonTestMethodShouldBeIgnored(this.testMethod);
        	if (ignored != null) {
        		this.result = ignored;
        		return;
        	}
            managers.fillAnnotatedFields(testClassObject);
            managers.startOfTestMethod(this.testMethod);

            logger.info(TestClassWrapper.LOG_STARTING + 
            		TestClassWrapper.LOG_START_LINE + TestClassWrapper.LOG_ASTERS +
                    TestClassWrapper.LOG_START_LINE + "*** Start of test method " + testClass.getName() + "#" + testMethod.getName() + methodType +
                    TestClassWrapper.LOG_START_LINE + TestClassWrapper.LOG_ASTERS);

            try {
                this.testMethod.invoke(testClassObject);
                this.result = Result.passed();
            } catch(Throwable e) {
            	this.result = Result.failed(e);
            }
            
            Result overrideResult = managers.endOfTestMethod(this.result, this.result.getThrowable());
            if (overrideResult != null) {
            	this.result = overrideResult;
            }

            if (this.result.isPassed()) {
            	String resname = this.result.getName();
            	if (this.type != Type.Test) {
            		resname = "Ok";
            	}
                logger.info(TestClassWrapper.LOG_ENDING + 
                		TestClassWrapper.LOG_START_LINE + TestClassWrapper.LOG_ASTERS +
                        TestClassWrapper.LOG_START_LINE + "*** " + resname + " - Test method " + testClass.getName() + "#" + testMethod.getName() + methodType +
                        TestClassWrapper.LOG_START_LINE + TestClassWrapper.LOG_ASTERS);
            } else {
                logger.info(TestClassWrapper.LOG_ENDING + 
                		TestClassWrapper.LOG_START_LINE + TestClassWrapper.LOG_ASTERS +
                        TestClassWrapper.LOG_START_LINE + "*** " + this.result.getName() + " - Test method " + testClass.getName() + "#" + testMethod.getName() + methodType +
                        TestClassWrapper.LOG_START_LINE + TestClassWrapper.LOG_ASTERS);
            }
            
            this.testStructureMethod.setStatus(this.result.getName());
            if (this.result.getThrowable() != null) {
            	Throwable t = this.getResult().getThrowable();
            	try {
            		StringWriter sw = new StringWriter();
            		PrintWriter pw = new PrintWriter(sw);
            		t.printStackTrace(pw);
            		this.testStructureMethod.setException(sw.toString());
            	} catch(Exception e) {
            		this.testStructureMethod.setException("Unable to report exception because of " + e.getMessage());
            	}
            }
        }
        catch(FrameworkException e) {
            throw new TestRunException("There was a problem with the framework, please check stacktrace", e);
        }
		return;
	}
	
	
	public boolean fullStop() {
		return false;
	}

	public Result getResult() {
		return this.result;
	}

	public TestMethod getStructure() {
		this.testStructureMethod = new TestMethod();
		this.testStructureMethod.setMethodName(testMethod.getName());
		this.testStructureMethod.setType(this.type.toString());
		
		return this.testStructureMethod;
	}

}
