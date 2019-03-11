package io.ejat.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;

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
	
	private boolean testPassed = true;
	
	// Test class members
	private LinkedHashSet<Method> beforeClassMethods = new LinkedHashSet<>();
	private LinkedHashSet<Method> afterClassMethods = new LinkedHashSet<>();
	private LinkedHashSet<Method> beforeMethodMethods = new LinkedHashSet<>();
	private LinkedHashSet<Method> afterMethodMethods = new LinkedHashSet<>();
	private LinkedHashSet<Method> testMethods = new LinkedHashSet<>();
	private Field loggerField;

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
	protected void parseTestClass() throws TestRunException {
		
		try {
			org.apache.bcel.classfile.JavaClass bcelJavaClass = org.apache.bcel.Repository.lookupClass(testClass);
			parseMethods(bcelJavaClass);		
			parseFields();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			throw new TestRunException("Unable to process test class for methods", e);
		}
	}
	
	
	/**
	 * Instantiate test class and set field values
	 *  
	 * @throws TestRunException
	 */
	protected void instantiateTestClass() throws TestRunException {
		try {
			logger.info(LOG_STARTING + 
						LOG_START_LINE + LOG_ASTERS + 
		                LOG_START_LINE + "*** Start of test class " + testClass.getName() +
		                LOG_START_LINE + LOG_ASTERS);
			testClassObject = testClass.newInstance();
			
			// Set logger field
			if (loggerField != null) {
				setField(loggerField, logger);
			}
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
	protected boolean runTestMethods() throws TestRunException {
		
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
	private void parseMethods(org.apache.bcel.classfile.JavaClass bcelJavaClass) throws NoSuchMethodException, TestRunException {
		org.apache.bcel.classfile.Method[] bcelMethods = bcelJavaClass.getMethods();
		for (org.apache.bcel.classfile.Method bcelMethod : bcelMethods) {
			if (!bcelMethod.isPublic()) {
				throw new TestRunException("Test methods must be public " + bcelJavaClass.getClassName() + "#" + bcelMethod.getName());
			} else {
				if (!bcelMethod.getName().equals("<init>")) {
					Method method = testClass.getMethod(bcelMethod.getName());
					Annotation[] annotations = method.getAnnotations();
					for (Annotation annotation : annotations) {
						storeMethod(method, annotation.annotationType());
					}
				}	
			}
		}
	}


	/**
	 * Parse test class for fields by Annotation 
	 * @param bcelJavaClass
	 */
	private void parseFields() {
		Field[] fields = testClass.getDeclaredFields();
		for (Field field : fields) {			
			Annotation[] annotations = field.getAnnotations();
			for (Annotation annotation : annotations) {
				storeField(field, annotation.annotationType());
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
			beforeClassMethods.add(method);
		}
		if (annotationType == io.ejat.AfterClass.class) {
			afterClassMethods.add(method);
		}
		if (annotationType == io.ejat.Before.class) {
			beforeMethodMethods.add(method);
		}
		if (annotationType == io.ejat.After.class) {
			afterMethodMethods.add(method);
		}
		if (annotationType == io.ejat.Test.class) {
			testMethods.add(method);
		}		
	}


	/**
	 * Store fields that require injection
	 * 
	 * @param field
	 * @param annotationType
	 */
	private void storeField(Field field, Class<? extends Annotation> annotationType) {
		if (annotationType == io.ejat.TestLogger.class &&
			field.getType().getName().contentEquals(org.apache.logging.log4j.Logger.class.getName())) {
			loggerField = field;
		}
	}


	/**
	 * Set a field to a value
	 * @param field
	 * @param value
	 * @throws TestRunException
	 */
	private void setField(Field field, Object value) throws TestRunException {
		try {
			// Set Field
			if (field.isAccessible()) {
				field.set(testClassObject, value);
			} else {
				field.setAccessible(true);
				field.set(testClassObject, value);
				field.setAccessible(false);
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
			testPassed = false;
			throw new TestRunException("Unable to set field " + field.getName() + " in test class", e);
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
