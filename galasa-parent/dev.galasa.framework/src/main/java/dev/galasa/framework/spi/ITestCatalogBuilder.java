/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import com.google.gson.JsonObject;

public interface ITestCatalogBuilder {

	void appendTestCatalog(JsonObject jsonRoot, JsonObject jsonTestClass, Class<?> testClass);
	
}
