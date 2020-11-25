/*
 * Licensed Materials - Property of IBM
 *
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.gendocs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class ManagerDoclet {

    private static final String TAG_MANAGER      = "@galasa.manager";
    private static final String TAG_ANNOTATION   = "@galasa.annotation";
    private static final String TAG_CPS_PROPERTY = "@galasa.cps.property";

    private static final String TAG_DESCRIPTION   = "@galasa.description";
    private static final String TAG_NAME          = "@galasa.name";
    private static final String TAG_EXTRA         = "@galasa.extra";
    private static final String TAG_LIMITATIONS   = "@galasa.limitations";
    private static final String TAG_REQUIRED      = "@galasa.required";
    private static final String TAG_DEFAULT       = "@galasa.default";
    private static final String TAG_VALID_VALUES  = "@galasa.valid_values";
    private static final String TAG_EXAMPLES      = "@galasa.examples";
    private static final String TAG_RELEASE_STATE = "@galasa.release.state";

    private static final String PROPERTY_NAME          = "name";
    private static final String PROPERTY_TITLE         = "title";
    private static final String PROPERTY_DESCRIPTION   = "description";
    private static final String PROPERTY_REQUIRED      = "required";
    private static final String PROPERTY_DEFAULT       = "default";
    private static final String PROPERTY_VALID_VALUES  = "validValues";
    private static final String PROPERTY_EXAMPLES      = "examples";
    private static final String PROPERTY_EXTRA         = "extra";
    private static final String PROPERTY_LIMITATIONS   = "limitations";
    private static final String PROPERTY_ATTRIBUTES    = "attributes";
    private static final String PROPERTY_RELEASE_STATE = "state";

    private static final String REGEX_MANAGER_NAME    = "[\\s/\\\\]";

    private static Gson gson = new Gson();

    private ManagerDoclet() {
        throw new IllegalStateException("Static class");
      }

    public static boolean start(RootDoc root) throws Exception {
        return processRoot(root, FileSystems.getDefault().getPath(".").toAbsolutePath());
    }

    public static boolean processRoot(RootDoc root, Path cwd) throws Exception {

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        ve.init();

        for(PackageDoc packageDoc : root.specifiedPackages()) {
            processPackageDoc(ve, packageDoc, cwd);
        }

        for(ClassDoc classDoc : root.classes()) {
            processClassDoc(ve, classDoc, cwd);
        }
        return true;
    }

    private static void processPackageDoc(VelocityEngine ve, PackageDoc packageDoc, Path cwd) throws Exception {
        System.out.println("CatalogDoclet - Processing package " + packageDoc.name());
        processTags(ve, packageDoc, packageDoc.name(), packageDoc.name(), cwd);
    }

    private static void processClassDoc(VelocityEngine ve, ClassDoc classDoc, Path cwd) throws Exception {
        System.out.println("CatalogDoclet - Processing Class " + classDoc.qualifiedName());
        processTags(ve, classDoc, classDoc.qualifiedName(), getPackageName(classDoc.qualifiedName()), cwd);
    }

    public static String getPackageName(String qualifiedName) {

        int pos = qualifiedName.lastIndexOf('.');
        if (pos < 0) {
            return "";
        }

        return qualifiedName.substring(0, pos);
    }

    public static void processTags(VelocityEngine ve, Doc doc, String name, String packageName, Path cwd) throws Exception {
        Tag[] tags = doc.tags();
        if (tags == null) {
            return;
        }

        JsonObject cpsProperty = new JsonObject();
        String propertyName = "";

        for(Tag tag : tags) {
            switch(tag.name()) {
                case TAG_CPS_PROPERTY:
                propertyName = recordCpsProperty(ve, doc, name, packageName, cwd, cpsProperty);
                    break;
                case TAG_MANAGER:
                    recordManager(ve, doc, name, packageName, cwd);
                    return;
                case TAG_ANNOTATION:
                    recordAnnotation(ve, doc, name, packageName, cwd);
                    return;
                default:
                    break;
            }
        }

        if(cpsProperty.size() != 0) {
            Path snippetFile = cwd.resolve("vscode_cps_snippets.json").toAbsolutePath();
            if (!Files.exists(snippetFile.getParent())) {
                Files.createDirectories(snippetFile.getParent());
            }

            JsonObject fullCps = new JsonObject();
            if(snippetFile.toFile().exists()) {
                fullCps = gson.fromJson(new String(Files.readAllBytes(snippetFile)), JsonObject.class);
            }
            if (propertyName != null) {
                fullCps.add(propertyName, cpsProperty);
            } 
            
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((Files.newOutputStream(snippetFile))));
            writer.write(gson.toJson(fullCps));
            writer.close();
        }
    }

    public static String recordCpsProperty(VelocityEngine ve, Doc doc, String qualifiedName, String packageName, Path cwd, JsonObject property) throws IOException {
        System.out.println("    Found CPS Property " + qualifiedName);

        String manager = getTagString(doc, TAG_CPS_PROPERTY, packageName);

        String propertyTitle = getFirstSentenceString(doc, packageName);
        String propertyName = getTagString(doc, TAG_NAME, packageName);
        String propertyDescription = getTagString(doc, TAG_DESCRIPTION, packageName);
        String propertyRequired = getTagString(doc, TAG_REQUIRED, packageName);
        String propertyDefault = getTagString(doc, TAG_DEFAULT, packageName);
        String propertyValidValues = getTagString(doc, TAG_VALID_VALUES, packageName);
        String propertyExamples = getTagString(doc, TAG_EXAMPLES, packageName);
        String propertyExtra = getTagString(doc, TAG_EXTRA, packageName);

        VelocityContext context = new VelocityContext();
        context.put(PROPERTY_TITLE, propertyTitle);
        context.put(PROPERTY_NAME, propertyName);
        context.put(PROPERTY_DESCRIPTION, propertyDescription);
        context.put(PROPERTY_REQUIRED, propertyRequired);
        context.put(PROPERTY_DEFAULT, propertyDefault);
        context.put(PROPERTY_VALID_VALUES, propertyValidValues);
        context.put(PROPERTY_EXAMPLES, propertyExamples);
        context.put(PROPERTY_EXTRA, propertyExtra);

        Template propertiesTemplate = ve.getTemplate("/property.template");

        String managerPrefix = "";
        if (manager != null) {
            manager = manager.trim().toLowerCase().replaceAll(REGEX_MANAGER_NAME, "_");
            if (!manager.isEmpty()) {
                managerPrefix = manager + FileSystems.getDefault().getSeparator();
            }
        }

        Path propertyFile = cwd.resolve(managerPrefix + "cps_" + qualifiedName + ".md").toAbsolutePath();
        if (!Files.exists(propertyFile.getParent())) {
            Files.createDirectories(propertyFile.getParent());
        }


        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((Files.newOutputStream(propertyFile))));
        propertiesTemplate.merge(context, writer);
        writer.close();

        property.addProperty("name", propertyTitle);
        property.addProperty("description", propertyDescription);
        if (propertyName != null && propertyName.indexOf(".") > -1) {
            property.addProperty("prefix", propertyName.substring(0, propertyName.indexOf(".")));
        } else {
            property.addProperty("prefix", propertyName);
        }
        JsonArray body = new JsonArray();
        body.add(propertyName);
        property.add("body", body);

        return propertyName;
    }

    public static void recordAnnotation(VelocityEngine ve, Doc doc, String qualifiedName, String packageName, Path cwd) throws IOException {
        System.out.println("    Found Annotation " + qualifiedName);

        ClassDoc classDoc = null;
        if (doc instanceof ClassDoc) {
            classDoc = (ClassDoc) doc;
        }
        String manager = getTagString(doc, TAG_ANNOTATION, packageName);

        String propertyTitle = getFirstSentenceString(doc, packageName);
        String propertyName = "@" + doc.name();
        String propertyDescription = getTagString(doc, TAG_DESCRIPTION, packageName);
        String propertyExamples = getTagString(doc, TAG_EXAMPLES, packageName);
        String propertyExtra = getTagString(doc, TAG_EXTRA, packageName);

        ArrayList<Attribute> attrs = new ArrayList<>();
        if (classDoc != null && classDoc.fields() != null) {
            for(MethodDoc fieldDoc : classDoc.methods()) {
                Attribute attr = new Attribute(fieldDoc.name(), getString(fieldDoc.getRawCommentText(), packageName));
                attrs.add(attr);
            }
        }


        VelocityContext context = new VelocityContext();
        context.put(PROPERTY_TITLE, propertyTitle);
        context.put(PROPERTY_NAME, propertyName);
        context.put(PROPERTY_DESCRIPTION, propertyDescription);
        context.put(PROPERTY_ATTRIBUTES, attrs);
        context.put(PROPERTY_EXAMPLES, propertyExamples);
        context.put(PROPERTY_EXTRA, propertyExtra);

        Template propertiesTemplate = ve.getTemplate("/annotation.template");

        String managerPrefix = "";
        if (manager != null) {
            manager = manager.trim().toLowerCase().replaceAll(REGEX_MANAGER_NAME, "_");
            if (!manager.isEmpty()) {
                managerPrefix = manager + FileSystems.getDefault().getSeparator();
            }
        }

        Path propertyFile = cwd.resolve(managerPrefix + "annotation_" + qualifiedName + ".md").toAbsolutePath();
        if (!Files.exists(propertyFile.getParent())) {
            Files.createDirectories(propertyFile.getParent());
        }


        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((Files.newOutputStream(propertyFile))));
        propertiesTemplate.merge(context, writer);
        writer.close();
    }

    public static void recordManager(VelocityEngine ve, Doc doc, String qualifiedName, String packageName, Path cwd) throws ManagerDocsException, IOException {
        System.out.println("    Found Manager " + qualifiedName);

        String manager = getTagString(doc, TAG_MANAGER, packageName);
        if (manager == null) {
            throw new ManagerDocsException("Manager javadoc for " + qualifiedName + " does not have a @galasa.manager id");
        }

        String propertyTitle = getFirstSentenceString(doc, packageName);
        String propertyName = getTagString(doc, TAG_NAME, packageName);
        String propertyDescription = getTagString(doc, TAG_DESCRIPTION, packageName);
        String propertyExtra = getTagString(doc, TAG_EXTRA, packageName);
        String propertyLimitations = getTagString(doc, TAG_LIMITATIONS, packageName);
        String propertyReleaseState = getTagString(doc, TAG_RELEASE_STATE, packageName);

        VelocityContext context = new VelocityContext();
        context.put(PROPERTY_TITLE, propertyTitle);
        context.put(PROPERTY_NAME, propertyName);
        context.put(PROPERTY_DESCRIPTION, propertyDescription);
        context.put(PROPERTY_LIMITATIONS, propertyLimitations);
        context.put(PROPERTY_EXTRA, propertyExtra);
        context.put(PROPERTY_RELEASE_STATE, propertyReleaseState);


        Template propertiesTemplate = ve.getTemplate("/manager.template");

        String managerId = "";
        managerId = manager.trim().toLowerCase().replaceAll(REGEX_MANAGER_NAME, "_");
        if (managerId.isEmpty()) {
           throw new ManagerDocsException("Manager javadoc for " + qualifiedName + " does not have a name for @galasa.manager");
        }
        String managerPrefix = managerId + FileSystems.getDefault().getSeparator();

        Path propertyFile = cwd.resolve(managerPrefix + "manager.md").toAbsolutePath();
        if (!Files.exists(propertyFile.getParent())) {
            Files.createDirectories(propertyFile.getParent());
        }

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((Files.newOutputStream(propertyFile))));
        propertiesTemplate.merge(context, writer);
        writer.close();

        Path idFile = cwd.resolve(managerPrefix + "id.txt");
        Files.write(idFile, manager.getBytes());
    }

    public static String getFirstSentenceString(Doc dococ, String packageName) {
        Tag[] tags = dococ.firstSentenceTags();

        return getFirstTagString(tags, packageName);
    }

    public static String getTagString(Doc doc, String tagName, String packageName) {
        Tag[] tags = doc.tags(tagName);
        return getFirstTagString(tags, packageName);
    }

    public static String getFirstTagString(Tag[] tags, String packageName) {

        if (tags == null || tags.length == 0) {
            return null;
        }

        for(Tag tag :tags) {
            String text = getTagString(tag, packageName);
            if (text != null) {
                return text;
            }
        }

        return null;
    }

    public static String getTagString(Tag tag, String packageName) {

        if (tag == null) {
            return null;
        }

        String text = tag.text();
        if (text == null) {
            return null;
        }

        text = text.trim();
        if (text.isEmpty()) {
            return null;
        }

        return getString(text, packageName);
    }


    public static String getString(String text, String packageName) {

        text = text.replaceAll("\n", "");
        text = text.replaceAll("\\Q{@literal @}\\E", "@");

        //*** Replace all the links
        Pattern linkPattern = Pattern.compile("(\\Q{@link\\E\\s+([\\w|\\.]+)\\s?\\Q}\\E)");

        Matcher matcher = linkPattern.matcher(text);
        while(matcher.find()) {
            String className = matcher.group(2);
            String fullname = null;
            if (!className.contains(".")) {
                fullname = packageName + "." + className;
            } else {
                fullname = className;
            }

            String javadocPrefix = "https://javadoc-snapshot.galasa.dev";
            String fullnamePath = fullname.replaceAll("\\.", "/");

            text = matcher.replaceFirst("<a href=\"" + javadocPrefix + "/" + fullnamePath + ".html\" target=\"_blank\">" + className + "</a>");
//            text = matcher.replaceFirst("[" + className + "](" + javadocPrefix + "/" + fullnamePath + ".html)");
            matcher = linkPattern.matcher(text);
        }

        return text;
    }


    public static class Attribute {
        private String name;
        private String text;

        public Attribute(String name, String text) {
            this.name = name;
            this.text = text;
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }
    }

}
