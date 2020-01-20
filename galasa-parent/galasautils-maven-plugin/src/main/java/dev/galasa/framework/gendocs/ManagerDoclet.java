package dev.galasa.framework.gendocs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class ManagerDoclet {
    
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
               
       return;
    }

    public static String getPackageName(String qualifiedName) {
        
        int pos = qualifiedName.lastIndexOf(".");
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

        for(Tag tag : tags) {
            switch(tag.name()) {
                case "@galasa.cps.property":
                    recordCpsProperty(ve, doc, name, packageName, cwd);
                    return;
                case "@galasa.manager":
                    recordManager(ve, doc, name, packageName, cwd);
                    return;
                case "@galasa.annotation":
                    recordAnnotation(ve, doc, name, packageName, cwd);
                    return;
            }
        }
    }

    public static void recordCpsProperty(VelocityEngine ve, Doc doc, String qualifiedName, String packageName, Path cwd) throws IOException {
        System.out.println("    Found CPS Property " + qualifiedName);

        String manager = getTagString(doc, "@galasa.cps.property", packageName);
        
        String propertyTitle = getFirstSentenceString(doc, packageName);
        String propertyName = getTagString(doc, "@galasa.name", packageName);
        String propertyDescription = getTagString(doc, "@galasa.description", packageName);
        String propertyRequired = getTagString(doc, "@galasa.required", packageName);
        String propertyDefault = getTagString(doc, "@galasa.default", packageName);
        String propertyValidValues = getTagString(doc, "@galasa.valid_values", packageName);
        String propertyExamples = getTagString(doc, "@galasa.examples", packageName);
        String propertyExtra = getTagString(doc, "@galasa.extra", packageName);

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
           
        String managerPrefix = "";
        if (manager != null) {
            manager = manager.trim().toLowerCase().replaceAll("[\\s/\\\\]", "_");
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
    }

    public static void recordAnnotation(VelocityEngine ve, Doc doc, String qualifiedName, String packageName, Path cwd) throws IOException {
        System.out.println("    Found Annotation " + qualifiedName);
        
        ClassDoc classDoc = null;
        if (doc instanceof ClassDoc) {
            classDoc = (ClassDoc) doc;
        }
        String manager = getTagString(doc, "@galasa.annotation", packageName);
       
        String propertyTitle = getFirstSentenceString(doc, packageName);
        String propertyName = "@" + doc.name();
        String propertyDescription = getTagString(doc, "@galasa.description", packageName);
        String propertyExamples = getTagString(doc, "@galasa.examples", packageName);
        String propertyExtra = getTagString(doc, "@galasa.extra", packageName);
        
        ArrayList<Attribute> attrs = new ArrayList<>();
        if (classDoc != null && classDoc.fields() != null) {
            for(MethodDoc fieldDoc : classDoc.methods()) {
                Attribute attr = new Attribute(fieldDoc.name(), getString(fieldDoc.getRawCommentText(), packageName));
                attrs.add(attr);
            }
        }
        

        VelocityContext context = new VelocityContext();
        context.put("title", propertyTitle);
        context.put("name", propertyName);
        context.put("description", propertyDescription);
        context.put("attributes", attrs);
        context.put("examples", propertyExamples);
        context.put("extra", propertyExtra);

        Template propertiesTemplate = ve.getTemplate("/annotation.template");
           
        String managerPrefix = "";
        if (manager != null) {
            manager = manager.trim().toLowerCase().replaceAll("[\\s/\\\\]", "_");
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

    public static void recordManager(VelocityEngine ve, Doc doc, String qualifiedName, String packageName, Path cwd) throws Exception {
        System.out.println("    Found Manager " + qualifiedName);

        String manager = getTagString(doc, "@galasa.manager", packageName);
        
        String propertyTitle = getFirstSentenceString(doc, packageName);
        String propertyName = getTagString(doc, "@galasa.name", packageName);
        String propertyDescription = getTagString(doc, "@galasa.description", packageName);
        String propertyExtra = getTagString(doc, "@galasa.extra", packageName);        
        String propertyLimitations = getTagString(doc, "@galasa.limitations", packageName);

        VelocityContext context = new VelocityContext();
        context.put("title", propertyTitle);
        context.put("name", propertyName);
        context.put("description", propertyDescription);
        context.put("limitations", propertyLimitations);
        context.put("extra", propertyExtra);

        Template propertiesTemplate = ve.getTemplate("/manager.template");
           
        String managerId = "";
        managerId = manager.trim().toLowerCase().replaceAll("[\\s/\\\\]", "_");
        if (managerId.isEmpty()) {
           throw new Exception("Manager javadoc for " + qualifiedName + " does not have a name for @galasa.manager");
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
            
            text = matcher.replaceFirst("[" + className + "](" + javadocPrefix + "/" + fullnamePath + ".html)");
            matcher = linkPattern.matcher(text);
        }

        return text;
    }
    
    
    public static class Attribute {
        public String name;
        public String text;
        
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
