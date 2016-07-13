package ru.idealplm.specification.mvm.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.methods.ReportBuilderMethod;
import ru.idealplm.xml2pdf2.handlers.PDFBuilder;

public class MVMReportBuilderMethod implements ReportBuilderMethod{

	@Override
	public File makeReportFile(Specification specification) {
		System.out.println("...METHOD... ReportBuilderMethod");
		try {
			copy(MVMReportBuilderMethod.class.getResourceAsStream("/icons/iconMVM.jpg"), new File(specification.getXmlFile().getParentFile().getAbsolutePath()+"\\iconMVM.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return PDFBuilder.xml2pdf(specification.getXmlFile(), Specification.getDefaultSpecificationPDFTemplate(), Specification.getDefaultSpecificationPDFConfig());
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

}
