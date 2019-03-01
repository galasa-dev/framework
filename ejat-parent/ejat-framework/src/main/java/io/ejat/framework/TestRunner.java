package io.ejat.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service={TestRunner.class})
public class TestRunner {


	private Logger logger = LogManager.getLogger(TestRunner.class);
	private BundleContext bundleContext;

	@Reference
	RepositoryAdmin repositoryAdmin;
	
	private LinkedHashSet<String> beforeClassMethod = new LinkedHashSet<String>();
	private LinkedHashSet<String> afterClassMethod = new LinkedHashSet<String>();
	private LinkedHashSet<String> beforeMethodMethod = new LinkedHashSet<String>();
	private LinkedHashSet<String> afterMethodMethod = new LinkedHashSet<String>();
	private LinkedHashSet<String> testMethod = new LinkedHashSet<String>();

	private String testBundleName;
	private String testClassName;
	private Class<?> testClass;
	
	public boolean runTest(String testBundleName, String testClassName) throws Exception {		
		this.testClassName = testClassName;
		this.testBundleName = testBundleName;
		return processTestClass();
	}
	
	private boolean processTestClass() throws Exception {
		
		boolean testPassed = false;
		
		logger.info("Run test: " + this.testBundleName + "/" + testClassName);
		Bundle[] bundles = bundleContext.getBundles();
		boolean bundleFound = false;
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().equals(testBundleName)) {
				bundleFound = true;
				logger.trace("Found Bundle: " + testBundleName);
				try {
					testClass = bundle.loadClass(testClassName);
				} catch (ClassNotFoundException e) {
					throw new Exception("Unable to load test class " + testClassName);
				}
				logger.trace("Found test class: " + testClass.getName());
				JavaClass clazz = Repository.lookupClass(testClass);
				org.apache.bcel.classfile.Method[] bcelMethods = clazz.getMethods();
				for (org.apache.bcel.classfile.Method bcelMethod : bcelMethods) {
					if (!bcelMethod.getName().equals("<init>")) {
						Method method = testClass.getMethod(bcelMethod.getName());
						Annotation[] annotations = method.getAnnotations();
						for (Annotation annotation : annotations) {
							if (annotation.annotationType() == io.ejat.BeforeClass.class) {
								beforeClassMethod.add(method.getName());
							}
							if (annotation.annotationType() == io.ejat.AfterClass.class) {
								afterClassMethod.add(method.getName());
							}
							if (annotation.annotationType() == io.ejat.Before.class) {
								beforeMethodMethod.add(method.getName());
							}
							if (annotation.annotationType() == io.ejat.After.class) {
								afterMethodMethod.add(method.getName());
							}
							if (annotation.annotationType() == io.ejat.Test.class) {
								testMethod.add(method.getName());
							}
						}	
					}
				}
				testPassed = runTestMethods();
				break;
			}
		}
		if (!bundleFound) {
			throw new Exception("Unable to find test bundle " + testBundleName);
		}
		
		return testPassed;
	}
	

	private boolean runTestMethods() {
		
		boolean testPassed = true;
		
		Object testClassObject = null;
		try {
			logger.info("\n" + 
		                "*******************************************************************************************************\n" +
		                "** Instantiating class:  " + testClassName + "\n" +
		                "*******************************************************************************************************");
			testClassObject = testClass.newInstance();
		} catch (IllegalAccessException | InstantiationException e1) {
            testPassed = false;
			e1.printStackTrace();
		}
		// Run before class methods
		Iterator<String> beforeClassMethodIterator = beforeClassMethod.iterator();
        while(beforeClassMethodIterator.hasNext()) {
            try {
            	Method method = testClass.getDeclaredMethod(beforeClassMethodIterator.next());
            	logger.info("\n" + 
		                    "*******************************************************************************************************\n" +
            	            "** Running method: " + method.getName() + "\n" +
            	            "*******************************************************************************************************");
				method.invoke(testClassObject);
			} catch (Exception e) {
	            testPassed = false;
				e.printStackTrace();
			}
        }
        
		// Run test methods
		Iterator<String> testMethodIterator = testMethod.iterator();
        while(testMethodIterator.hasNext()){
    		// Run before method methods
    		Iterator<String> beforeMethodMethodIterator = beforeMethodMethod.iterator();
            while(beforeMethodMethodIterator.hasNext()) {
                try {
            		Method method = testClass.getDeclaredMethod(beforeMethodMethodIterator.next());
            		logger.info("\n" + 
    		                    "*******************************************************************************************************\n" + 
            		            "** Running method: " + method.getName() + "\n" +
            		            "*******************************************************************************************************");
    				method.invoke(testClassObject);
    			} catch (Exception e) {
    	            testPassed = false;
    				e.printStackTrace();
    			}
            }
            
            // Run test method
            try {
            	Method method = testClass.getDeclaredMethod(testMethodIterator.next());
            	logger.info("\n" + 
		                    "*******************************************************************************************************\n" + 
            	            "** Running method: " + method.getName() + "\n" +
            	            "*******************************************************************************************************");
				method.invoke(testClassObject);
			} catch (Exception e) {
	            testPassed = false;
				e.printStackTrace();
			}
    		
            // Run after method methods
    		Iterator<String> afterMethodMethodIterator = afterMethodMethod.iterator();
            while(afterMethodMethodIterator.hasNext()) {
                try {
            		Method testMethod = testClass.getDeclaredMethod(afterMethodMethodIterator.next());
            		logger.info("\n" + 
    		                    "*******************************************************************************************************\n" + 
            		            "** Running method: " + testMethod.getName() + "\n" +
            		            "*******************************************************************************************************");
                	testMethod.invoke(testClassObject);
    			} catch (Exception e) {
    	            testPassed = false;
    				e.printStackTrace();
    			}
            }
        }
        
		// Run after class methods
		Iterator<String> afterClassMethodIterator = afterClassMethod.iterator();
        while(afterClassMethodIterator.hasNext()) {
            try {
        		Method method = testClass.getDeclaredMethod(afterClassMethodIterator.next());
        		logger.info("\n" + 
	                        "*******************************************************************************************************\n" +
        		            "** Running method: " + method.getName() + "\n" +
        		            "*******************************************************************************************************");
        		
				method.invoke(testClassObject);
			} catch (Exception e) {
	            testPassed = false;
				e.printStackTrace();
			}
        }
        
        return testPassed;
	}

	@Activate
	public void activate(BundleContext context) {
		this.bundleContext = context;
	}
	
}
