package io.ejat.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Representation of the test class
 *
 */
public class TestClassWrapper {
	
	private Log logger = LogFactory.getLog(TestClassWrapper.class);
	
	private Class<?> testClass;
	private Object testClassObject;
	
	private boolean testPassed = true;
	
	// Test class members
	private LinkedHashMap<String, Method> beforeClassMethods = new LinkedHashMap<>();
	private LinkedHashMap<String, Method> afterClassMethods = new LinkedHashMap<>();
	private LinkedHashMap<String, Method> beforeMethodMethods = new LinkedHashMap<>();
	private LinkedHashMap<String, Method> afterMethodMethods = new LinkedHashMap<>();
	private LinkedHashMap<String, Method> testMethods = new LinkedHashMap<>();

	// Logger statics
	private static final String LOG_STARTING = "Starting";
	private static final String LOG_ENDING = "Ending";	
	private	static final String LOG_START_LINE = "\n" + StringUtils.repeat("-", 23) + " ";
	private	static final String LOG_ASTERS = StringUtils.repeat("*", 100);
	private static final String LOG_METHOD_BEFORE_CLASS = " type=BeforeClass";
	private static final String LOG_METHOD_BEFORE = " type=Before";
	private static final String LOG_METHOD_TEST = " type=Test";
	private static final String LOG_METHOD_AFTER = " type=After";
	private static final String LOG_METHOD_AFTER_CLASS = " type=AfterClass";

	
	/**
	 * Constructor
	 */
	public TestClassWrapper(Class<?> testClass) {
		this.testClass = testClass;
	}
	
	
	/**
	 * Process the test class looking for test methods and fields that need to be injected
	 * 
	 * @throws TestRunException
	 */
	public void parseTestClass() throws TestRunException {
		
		try	{
			// Create a list of test classes and it's super classes
			LinkedList<Class<?>> classListList = new LinkedList<>();
			classListList.add(testClass);
			Class<?> superClass = testClass.getSuperclass();
			while (!superClass.isAssignableFrom(Object.class)) {
				classListList.add(superClass);
				superClass = superClass.getSuperclass();
			}
			
			Iterator<Class<?>> lit = classListList.descendingIterator();
			while(lit.hasNext()){
				parseMethods(lit.next());
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new TestRunException("Unable to process test class for methods", e);
		}
	}
	
	
	/**
	 * Instantiate test class and set field values
	 *  
	 * @throws TestRunException
	 */
	public void instantiateTestClass() throws TestRunException {
		try {
			logger.info(LOG_STARTING + 
						LOG_START_LINE + LOG_ASTERS + 
		                LOG_START_LINE + "*** Start of test class " + testClass.getName() +
		                LOG_START_LINE + LOG_ASTERS);
			testClassObject = testClass.newInstance();
		} catch (InstantiationException | IllegalAccessException | NullPointerException e) {
			testPassed = false;
			throw new TestRunException("Unable to instantiate test class", e);
		} finally {
			if (!testPassed) {
				logger.info(LOG_ENDING + 
							LOG_START_LINE + LOG_ASTERS + 
							LOG_START_LINE + "*** FAILED - Test class " + testClass.getName() +
							LOG_START_LINE + LOG_ASTERS);
			}
		}
	}


	/**
	 * Run the test methods in declared order together with @BeforeClass, @Before, @After and @AfterClass
	 * 
	 * @return test passed
	 * @throws TestRunException 
	 */
	public boolean runTestMethods() throws TestRunException {
		
		// Run @BeforeClass methods
		for (Map.Entry<String, Method> beforeClassMethodEntry : beforeClassMethods.entrySet()) {
			invokeMethod(beforeClassMethodEntry.getValue(), LOG_METHOD_BEFORE_CLASS);
		}
	    
		// Run test methods
		for (Map.Entry<String, Method> testMethodEntry : testMethods.entrySet()) {
			// Run @Before methods
			for (Map.Entry<String, Method> beforeMethodMethodEntry : beforeMethodMethods.entrySet()) {
				invokeMethod(beforeMethodMethodEntry.getValue(), LOG_METHOD_BEFORE);
			}
	        
	        // Run @Test method			
			invokeMethod(testMethodEntry.getValue(), LOG_METHOD_TEST);
			

			for (Map.Entry<String, Method> afterMethodMethodEntry : afterMethodMethods.entrySet()) {
				invokeMethod(afterMethodMethodEntry.getValue(), LOG_METHOD_AFTER);
			}
		}
	    
		// Run @AfterClass methods
		for (Map.Entry<String, Method> afterClassMethodEntry : afterClassMethods.entrySet()) {
			invokeMethod(afterClassMethodEntry.getValue(), LOG_METHOD_AFTER_CLASS);
		}
	    
	    // Test result
	    if (testPassed) {
	    	logger.info(LOG_ENDING + 
	    				LOG_START_LINE + LOG_ASTERS + 
	    				LOG_START_LINE + "*** PASSED - Test class " + testClass.getName() +
	    				LOG_START_LINE + LOG_ASTERS);
	    } else {
			logger.info(LOG_ENDING + 
						LOG_START_LINE + LOG_ASTERS + 
		                LOG_START_LINE + "*** FAILED - Test class " + testClass.getName() +
		                LOG_START_LINE + LOG_ASTERS);
	    }
		
	    return testPassed;
	}


	/**
	 * Parse test class for test methods
	 * 
	 * @param bcelJavaClass
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws TestRunException
	 */
	private void parseMethods(Class<?> testClassXXX) throws NoSuchMethodException, TestRunException {
		org.apache.bcel.classfile.JavaClass bcelJavaClass;
		try {
			bcelJavaClass = org.apache.bcel.Repository.lookupClass(testClassXXX);
		} catch (ClassNotFoundException e) {
			throw new TestRunException(e);
		}
		org.apache.bcel.classfile.Method[] bcelMethods = bcelJavaClass.getMethods();
		for (org.apache.bcel.classfile.Method bcelMethod : bcelMethods) {
			if (bcelMethod.isPublic() && !bcelMethod.getName().equals("<init>")) {
				Method method = testClassXXX.getMethod(bcelMethod.getName());
				Annotation[] annotations = method.getAnnotations();
				for (Annotation annotation : annotations) {
					storeMethod(method, annotation.annotationType());
				}	
			}
		}
	}


	/** 
	 * Store the test methods
	 * 
	 * @param method
	 * @param annotationType
	 */
	private void storeMethod(Method method, Class<? extends Annotation> annotationType) {
		if (annotationType == io.ejat.BeforeClass.class) {
			beforeClassMethods.put(method.getName(), method);
		}
		if (annotationType == io.ejat.AfterClass.class) {
			afterClassMethods.put(method.getName(), method);
		}
		if (annotationType == io.ejat.Before.class) {
			beforeMethodMethods.put(method.getName(), method);
		}
		if (annotationType == io.ejat.After.class) {
			afterMethodMethods.put(method.getName(), method);
		}
		if (annotationType == io.ejat.Test.class) {
			testMethods.put(method.getName(), method);
		}		
	}


	/** 
	 * Run the supplied method
	 * 
	 * @param method the method
	 * @param methodType the method type for logging
	 * @throws TestRunException
	 */
	private void invokeMethod(Method method, String methodType) throws TestRunException {
		
		try {
        	logger.info(LOG_STARTING + 
						LOG_START_LINE + LOG_ASTERS +
	                    LOG_START_LINE + "*** Start of test method " + testClass.getName() + "#" + method.getName() + methodType +
        	            LOG_START_LINE + LOG_ASTERS);
        	if (method.isAccessible()) {
        		method.invoke(testClassObject);
        	} else {
        		method.setAccessible(true);
        		method.invoke(testClassObject);
        		method.setAccessible(true);
        	}
        	logger.info(LOG_ENDING + 
						LOG_START_LINE + LOG_ASTERS +
                    	LOG_START_LINE + "*** OK - Test method " + testClass.getName() + "#" + method.getName() + methodType +
                    	LOG_START_LINE + LOG_ASTERS);
		} catch (IllegalAccessException e) {
			testPassed = false;
        	logger.error(LOG_ENDING + 
						 LOG_START_LINE + LOG_ASTERS +
                		 LOG_START_LINE + "*** FAILED - Test method " + testClass.getName() + "#" + method.getName() + methodType +
                		 LOG_START_LINE + LOG_ASTERS);
			logger.error("FAILED - Test method " + testClass.getName() + "#" + method.getName() + methodType, e.getCause());
        	throw new TestRunException("Unable to invoke test method", e);
		} catch (InvocationTargetException e) {
            testPassed = false;
        	logger.error(LOG_ENDING + 
						 LOG_START_LINE + LOG_ASTERS +
                		 LOG_START_LINE + "*** FAILED - Test method " + testClass.getName() + "#" + method.getName() + methodType +
                		 LOG_START_LINE + LOG_ASTERS);
			logger.error("FAILED - Test method " + testClass.getName() + "#" + method.getName() + methodType, e.getCause());
		}
	}

}
