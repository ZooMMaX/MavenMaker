package ru.zoommax.mm.MavenMaker;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Main {
    static JarEntry e = null;
    static File ff = null;
        public static void main(String[] args) throws IOException {
            unzipJar("./", args[0]);
        }

    public static void unzipJar(String destinationDir, String jarPath) throws IOException {
        File file = new File(jarPath);
        JarFile jar = new JarFile(file);

        //now create all files
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = (JarEntry) enums.nextElement();

            String fileName = destinationDir + File.separator +entry.getName();
            String nf = jar.getName().substring(jar.getName().lastIndexOf(File.separator)+1, jar.getName().lastIndexOf("."));
            File f = new File(nf+".pom");


            if (fileName.contains("pom.xml")) {
                System.out.println("Yes");
                e = entry;
                ff = f;
                break;
            }
        }
        exists(jar, e, ff);
    }

    public static void exists(JarFile jar, JarEntry entry, File f) throws IOException {
        InputStream is = jar.getInputStream(entry);
        FileOutputStream fos = new FileOutputStream(f);
        while (is.available() > 0) {
            fos.write(is.read());
        }

        fos.close();
        is.close();
        String path = xmlparse(f);
        File dirs = new File(path);
        if (!dirs.exists()){
            dirs.mkdirs();
        }
        String pj = path+jar.getName().substring(jar.getName().lastIndexOf(File.separator)+1);
        String pf = path+f.getName();
        Files.copy(Paths.get(jar.getName()), Paths.get(pj), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(f.toPath(), Paths.get(pf), StandardCopyOption.REPLACE_EXISTING);
        HashMap<String, String> tmp = xmldata(f);
        FileWriter writer = new FileWriter(path+"maven-metadata.xml", false);
        writer.write("<metadata modelVersion=\"1.1.0\">\n" +
                "<groupId>"+tmp.get("groupId")+"</groupId>\n" +
                "<artifactId>"+tmp.get("artifactId")+"</artifactId>\n" +
                "<version>"+tmp.get("version")+"</version>\n" +
                "<versioning>\n" +
                "<snapshot>\n" +
                "<buildNumber>1</buildNumber>\n" +
                "</snapshot>\n" +
                "<lastUpdated>"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +"</lastUpdated>\n" +
                "</versioning>\n" +
                "</metadata>");
        writer.flush();
        writer.close();
        FileWriter writer2 = new FileWriter(Paths.get(path).getParent()+"/maven-metadata.xml", false);
        writer2.write("<metadata modelVersion=\"1.1.0\">\n" +
                "<groupId>"+tmp.get("groupId")+"</groupId>\n" +
                "<artifactId>"+tmp.get("artifactId")+"</artifactId>\n" +
                "<version>"+tmp.get("version")+"</version>\n" +
                "<versioning>\n" +
                "<latest>"+tmp.get("version")+"</latest>\n" +
                "<versions>\n" +
                "<version>"+tmp.get("version")+"</version>\n" +
                "</versions>\n" +
                "<lastUpdated"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +"</lastUpdated>\n" +
                "</versioning>\n" +
                "</metadata>");
        writer2.flush();
        writer2.close();
    }

    public static HashMap<String, String> xmldata(File file){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            String groupId = doc.getElementsByTagName("groupId").item(0).getTextContent();
            String artifactId = doc.getElementsByTagName("artifactId").item(0).getTextContent();
            String version = doc.getElementsByTagName("version").item(0).getTextContent();
            HashMap<String, String> tmp = new HashMap<>();
            tmp.put("groupId", groupId);
            tmp.put("artifactId", artifactId);
            tmp.put("version", version);
            return tmp;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String xmlparse(File file){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            String groupId = doc.getElementsByTagName("groupId").item(0).getTextContent().replace(".", "/");
            String artifactId = doc.getElementsByTagName("artifactId").item(0).getTextContent();
            String version = doc.getElementsByTagName("version").item(0).getTextContent();
            return groupId+"/"+artifactId+"/"+version+"/";

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }

}
