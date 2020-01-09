package dev.galasa.framework.gendocs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class ManagerDoclet {
    public static boolean start(RootDoc root) throws IOException {

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        ve.init();

        for(ClassDoc classDoc : root.classes()) {
            processClassDoc(ve, classDoc);
        }
        return true;
    }

    private static void processClassDoc(VelocityEngine ve, ClassDoc classDoc) throws IOException {

        System.out.println("CatalogDoclet - Processing " + classDoc.qualifiedName());

        Tag[] tags = classDoc.tags();
        if (tags == null) {
            return;
        }

        for(Tag tag : tags) {
            switch(tag.name()) {
                case "@galasa.cps.property":
                    recordCpsProperty(ve, classDoc);
                    return;
            }
        }


        return;
    }

    private static void recordCpsProperty(VelocityEngine ve, ClassDoc classDoc) throws IOException {
        String propertyTitle = getFirstSentenceString(classDoc);
        String propertyName = getTagString(classDoc, "@galasa.name");
        String propertyDescription = getTagString(classDoc, "@galasa.description");
        String propertyRequired = getTagString(classDoc, "@galasa.required");
        String propertyDefault = getTagString(classDoc, "@galasa.default");
        String propertyValidValues = getTagString(classDoc, "@galasa.valid_values");
        String propertyExamples = getTagString(classDoc, "@galasa.examples");
        String propertyExtra = getTagString(classDoc, "@galasa.extra");

        VelocityContext context = new VelocityContext();
        context.put("title", propertyTitle);
        context.put("name", propertyName);
        context.put("description", propertyDescription);
        context.put("required", propertyRequired);
        context.put("default", propertyDefault);
        context.put("validValues", propertyValidValues);
        context.put("examples", propertyExamples);
        context.put("extra", propertyExtra);

        Template propertiesTemplate = ve.getTemplate("/property.template");

        Path propertyFile = Paths.get("cps_" + classDoc.qualifiedName() + ".md");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((Files.newOutputStream(propertyFile))));
        propertiesTemplate.merge(context, writer);
        writer.close();
    }

    private static String getFirstSentenceString(ClassDoc classDoc) {
        Tag[] tags = classDoc.firstSentenceTags();

        if (tags == null || tags.length == 0) {
            return null;
        }

        for(Tag tag :tags) {
            String text = getTagString(tag);
            if (text != null) {
                return text;
            }
        }

        return null;
    }

    private static String getTagString(ClassDoc classDoc, String tagName) {
        Tag[] tags = classDoc.tags(tagName);

        if (tags == null || tags.length == 0) {
            return null;
        }

        for(Tag tag :tags) {
            String text = getTagString(tag);
            if (text != null) {
                return text;
            }
        }

        return null;
    }

    private static String getTagString(Tag tag) {

        String text = tag.text();
        if (text != null && !text.isEmpty()) {
            text = text.replaceAll("\n", "");

            return text;
        }

        return null;
    }




}
