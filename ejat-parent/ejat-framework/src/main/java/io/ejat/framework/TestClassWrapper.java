package io.ejat.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.bcel.Repository;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Representation of the test class
 *
 */
public class TestClassWrapper {
	
	private Logger logger = LogManager.getLogger(TestClassWrapper.class);
	
	private Class<?> testClass;
	private Object testClassObject;
	
	private LinkedHashSet<Method> beforeClassMethods = new LinkedHashSet<Method>();
	private LinkedHashSet<Method> afterClassMethods = new LinkedHashSet<Method>();
	private LinkedHashSet<Method> beforeMethodMethods = new LinkedHashSet<Method>();
	private LinkedHashSet<Method> afterMethodMethods = new LinkedHashSet<Method>();
	private LinkedHashSet<Method> testMethods = new LinkedHashSet<Method>();
	private Field loggerField;
	
	private boolean testPassed = true;

	private static final String LOG_METHOD_BEFORE_CLASS = " type=BeforeClass";
	private static final String LOG_METHOD_BEFORE = " type=Before";
	private static final String LOG_METHOD_TEST = " type=Test";
	private static final String LOG_METHOD_AFTER = " type=After";
	private static final String LOG_METHOD_AFTER_CLASS = " type=AfterClass";
	
	private	static final String LOG_START_LINE = "\n" + StringUtils.repeat("-", 23) + " ";
	private	static final String LOG_ASTERS = StringUtils.repeat("*", 100);

	
	/**
	 * Constructor
	 */
	public TestClassWrapper(Class<?> testClass) {
		this.testClass = testClass;
	}
	
	
	/**
	 * Process the test class looking for fields that require injection and finding the test methods
	 * 
	 * @throws TestRunException
	 */
	//TODO: What if test class implements or extends?
	protected void parseTestClass() throws TestRunException {
		
		try {
			org.apache.bcel.classfile.JavaClass bcelJavaClass = Repository.lookupClass(testClass);
			
			// Process methods
			org.apache.bcel.classfile.Method[] bcelMethods = bcelJavaClass.getMethods();
			for (org.apache.bcel.classfile.Method bcelMethod : bcelMethods) {
				if (bcelMethod.getName().equals("<init>")) {
					if (!bcelMethod.isPublic()) {
						throw new TestRunException("Test class default constructor must be public");
					}
				} else {
					Method method = testClass.getMethod(bcelMethod.getName());
					Annotation[] annotations = method.getAnnotations();
					for (Annotation annotation : annotations) {
						if (annotation.annotationType() == io.ejat.BeforeClass.class) {
							beforeClassMethods.add(method);
						}
						if (annotation.annotationType() == io.ejat.AfterClass.class) {
							afterClassMethods.add(method);
						}
						if (annotation.annotationType() == io.ejat.Before.class) {
							beforeMethodMethods.add(method);
						}
						if (annotation.annotationType() == io.ejat.After.class) {
							afterMethodMethods.add(method);
						}
						if (annotation.annotationType() == io.ejat.Test.class) {
							testMethods.add(method);
						}
					}	
				}
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			throw new TestRunException("Unable to process test class for methods", e);
		}

		
		// Process fields by annotations
		try {
			Field[] fields = testClass.getDeclaredFields();
			for (Field field : fields) {			
				Annotation[] annotations = field.getAnnotations();
				for (Annotation annotation : annotations) {
					// Get Logger field
					//TODO: TestLogger annotation?
					if (annotation.annotationType() == io.ejat.TestLogger.class &&
						field. getType().getName().contentEquals(org.apache.logging.log4j.Logger.class.getName())) {
						loggerField = field;
					}
				}
			}
		}
		catch (SecurityException e) {
			throw new TestRunException("Unable to process test class for fields", e);
		} 
	}
	
	
	/**
	 *  Instantiate test class and set field values
	 *  
	 * @throws TestRunException
	 */
	protected void instantiateTestClass() throws TestRunException {
		try {
			logger.info("Starting" + 
						LOG_START_LINE + LOG_ASTERS + 
		                LOG_START_LINE + "*** Start of test class " + testClass.getName() +
		                LOG_START_LINE + LOG_ASTERS);
			testClassObject = testClass.newInstance();
			
			// Set logger field
			if (loggerField != null) {
				try {
					// Set logger
					if (loggerField.isAccessible()) {
						loggerField.set(testClassObject, logger);
					} else {
						loggerField.setAccessible(true);
						loggerField.set(testClassObject, logger);
						loggerField.setAccessible(false);
					}
		
				} catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
					testPassed = false;
					throw new TestRunException("Unable to set Logger field in test class", e);
				}
			}
		} catch (InstantiationException | IllegalAccessException | NullPointerException e) {
			testPassed = false;
			throw new TestRunException("Unable to instantiate test class", e);
		} finally {
			if (!testPassed) {
				logger.info("Ending" + 
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
	 * @throws Throwable 
	 */
	protected boolean runTestMethods() throws Throwable {
		
		// Run @BeforeClass methods
		Iterator<Method> beforeClassMethodIterator = beforeClassMethods.iterator();
        while(beforeClassMethodIterator.hasNext()) {
        	invokeMethod(beforeClassMethodIterator.next(), LOG_METHOD_BEFORE_CLASS);
        }
        
		// Run test methods
		Iterator<Method> testMethodIterator = testMethods.iterator();
        while(testMethodIterator.hasNext()){
    		// Run @Before methods
    		Iterator<Method> beforeMethodMethodIterator = beforeMethodMethods.iterator();
            while(beforeMethodMethodIterator.hasNext()) {
            	invokeMethod(beforeMethodMethodIterator.next(), LOG_METHOD_BEFORE);
            }
            
            // Run @Test method
            invokeMethod(testMethodIterator.next(), LOG_METHOD_TEST);
    		
            // Run @After methods
    		Iterator<Method> afterMethodMethodIterator = afterMethodMethods.iterator();
            while(afterMethodMethodIterator.hasNext()) {
                invokeMethod(afterMethodMethodIterator.next(), LOG_METHOD_AFTER);
            }
        }
        
		// Run @AfterClass methods
		Iterator<Method> afterClassMethodIterator = afterClassMethods.iterator();
        while(afterClassMethodIterator.hasNext()) {
            invokeMethod(afterClassMethodIterator.next(), LOG_METHOD_AFTER_CLASS);
        }
        
        // Test result
        if (testPassed) {
        	logger.info("Ending" + 
        				LOG_START_LINE + LOG_ASTERS + 
        				LOG_START_LINE + "*** PASSED - Test class " + testClass.getName() +
        				LOG_START_LINE + LOG_ASTERS);
        } else {
    		logger.info("Ending" + 
    					LOG_START_LINE + LOG_ASTERS + 
    	                LOG_START_LINE + "*** FAILED - Test class " + testClass.getName() +
    	                LOG_START_LINE + LOG_ASTERS);
        }
		
        return testPassed;
	}


	/** 
	 * Run the supplied method
	 * 
	 * @param method the method
	 * @param methodType the method type for logging
	 * @throws Throwable 
	 */
	private void invokeMethod(Method method, String methodType) throws Throwable {
		
		try {
        	logger.info("Starting" + 
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
        	logger.info("Ending" + 
						LOG_START_LINE + LOG_ASTERS +
                    	LOG_START_LINE + "*** OK - Test method " + testClass.getName() + "#" + method.getName() + methodType +
                    	LOG_START_LINE + LOG_ASTERS);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
			testPassed = false;
        	logger.error("Ending" + 
						 LOG_START_LINE + LOG_ASTERS +
                		 LOG_START_LINE + "*** FAILED - Test method " + testClass.getName() + "#" + method.getName() + methodType +
                		 LOG_START_LINE + LOG_ASTERS);
			logger.error("FAILED - Test method " + testClass.getName() + "#" + method.getName() + methodType, e.getCause());
        	throw new TestRunException("Unable to invoke test method", e);
		} catch (InvocationTargetException e) {
            testPassed = false;
        	logger.error("Ending" + 
						 LOG_START_LINE + LOG_ASTERS +
                		 LOG_START_LINE + "*** FAILED - Test method " + testClass.getName() + "#" + method.getName() + methodType +
                		 LOG_START_LINE + LOG_ASTERS);
			logger.error("FAILED - Test method " + testClass.getName() + "#" + method.getName() + methodType, e.getCause());
		}
	}

}
