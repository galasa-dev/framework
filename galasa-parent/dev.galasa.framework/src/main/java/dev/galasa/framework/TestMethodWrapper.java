/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.teststructure.TestMethod;

public class TestMethodWrapper {
	
	private final List<GenericMethodWrapper> befores = new ArrayList<>();
	private GenericMethodWrapper testMethod;
	private final List<GenericMethodWrapper> afters = new ArrayList<>();
	
	private Result result;	
	private boolean fullStop = false;
	
	protected TestMethodWrapper(Method testMethod, Class<?> testClass, ArrayList<GenericMethodWrapper> beforeMethods, ArrayList<GenericMethodWrapper> afterMethods) {
		
		this.testMethod = new GenericMethodWrapper(testMethod, testClass, GenericMethodWrapper.Type.Test);
		
		for(GenericMethodWrapper before : beforeMethods) {
			//TODO,  check the before can be run, before adding to list
			this.befores.add(before);
		}
		
		for(GenericMethodWrapper after : afterMethods) {
			//TODO,  check the after can be run, before adding to list
			this.afters.add(after);
		}
		
		return;
	}

	public void invoke(@NotNull TestRunManagers managers, Object testClassObject) throws TestRunException {
		// run all the @Befores before the test method
		for(GenericMethodWrapper before : this.befores) {
			before.invoke(managers, testClassObject);
			if (before.getResult().isFullStop()) {
				this.fullStop = true;
				this.result = Result.failed("Before method failed");
				return;
			}
		}

		testMethod.invoke(managers, testClassObject);
		this.fullStop = this.testMethod.fullStop();
		this.result = this.testMethod.getResult();
	
		// run all the @Afters after the test method
		for(GenericMethodWrapper after : this.afters) {
			after.invoke(managers, testClassObject);
			if (after.fullStop()) {
				this.fullStop = true;
				if (this.result == null) {
					this.result = Result.failed("After method failed");
				}
			}
		}
	}

	public boolean fullStop() {
		return this.fullStop;
	}

	public Result getResult() {
		return this.result;
	}

	public TestMethod getStructure() {
		TestMethod methodStructure = testMethod.getStructure();
		ArrayList<TestMethod> structureBefores = new ArrayList<>();
		ArrayList<TestMethod> structureAfters = new ArrayList<>();
		
		methodStructure.setBefores(structureBefores);
		methodStructure.setAfters(structureAfters);
		
		for(GenericMethodWrapper before : this.befores) {
			structureBefores.add(before.getStructure());
		}
		
		for(GenericMethodWrapper after : this.afters) {
			structureAfters.add(after.getStructure());
		}
		
		return methodStructure;
	}
	
}
