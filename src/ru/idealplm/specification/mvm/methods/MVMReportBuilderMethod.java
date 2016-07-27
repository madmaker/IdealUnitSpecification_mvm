package ru.idealplm.specification.mvm.methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.SpecificationSettings;
import ru.idealplm.utils.specification.methods.ReportBuilderMethod;
import ru.idealplm.xml2pdf2.handlers.PDFBuilder;

public class MVMReportBuilderMethod implements ReportBuilderMethod{

	private Specification specification = Specification.getInstance();
	
	@Override
	public File makeReportFile() {
		System.out.println("...METHOD... ReportBuilderMethod");
		try {
			copy(MVMReportBuilderMethod.class.getResourceAsStream("/icons/iconMVM.jpg"), new File(specification.getXmlFile().getParentFile().getAbsolutePath()+"\\iconMVM.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*writeToConsole(specification.getXmlFile(), "XML");
		writeToConsole(Specification.settings.getConfigStream(), "CONFIG");
		writeToConsole(Specification.settings.getTemplateStream(), "TEMPLATE");*/
		return PDFBuilder.xml2pdf(specification.getXmlFile(),  Specification.settings.getTemplateStream(), Specification.settings.getConfigStream());
	}
	
	public static void copy(InputStream source, File dest) throws IOException {
        try {
            FileOutputStream os = new FileOutputStream(dest);
            try {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = source.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                os.close();
            }
        } catch (Exception ex){
        	ex.printStackTrace();
        } finally {
        	if(source!=null){
        		source.close();
        	}
        }
    }
	
	public void writeToConsole(Object what, String name){
		System.out.println("======= WRITING " + name + " ========");
		System.out.println("class="+what.getClass().getName());
		try{
			if(what.getClass().equals(File.class)){
				try (BufferedReader br = new BufferedReader(new FileReader((File)what))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				    	System.out.println(line);
				    }
				}
			} else if (what.getClass().equals(FileInputStream.class)){
				try (BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)what, "UTF-8"))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				    	System.out.println(line);
				    }
				}
			} else {
				System.out.println("=====not file nor stream");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
