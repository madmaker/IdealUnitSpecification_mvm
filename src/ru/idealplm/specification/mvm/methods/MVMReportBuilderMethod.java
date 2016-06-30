package ru.idealplm.specification.mvm.methods;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.methods.ReportBuilderMethod;
import ru.idealplm.xml2pdf2.handlers.PDFBuilder;

public class MVMReportBuilderMethod implements ReportBuilderMethod{

	@Override
	public File makeReportFile(Specification specification) {
		return PDFBuilder.xml2pdf(specification.getXmlFile(), Specification.getDefaultSpecificationPDFTemplate(), Specification.getDefaultSpecificationPDFConfig());
	}

}
