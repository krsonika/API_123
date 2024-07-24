package Business_Logics;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import org.springframework.stereotype.Component;

@Component("LIC16548_PJT1009_PE_NLPd48a8720-4afa-4f35-a2f9-6bb738a153e9")
public class FetchSectionDataFromPdf implements Nlp {
	@InputParams({@InputParam(name = "PdfFilePath", type = "java.lang.String"), @InputParam(name = "PageNumber", type = "java.lang.Integer"), @InputParam(name = "StartX", type = "java.lang.Integer"), @InputParam(name = "StartY", type = "java.lang.Integer"), @InputParam(name = "Width", type = "java.lang.Integer"), @InputParam(name = "Height", type = "java.lang.Integer")})
	@ReturnType(name = "Data", type = "java.lang.String")

	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}
	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String pdfFilePath = (String) attributes.get("PdfFilePath");
		Integer pageNumber = (Integer) attributes.get("PageNumber");
		Integer startX = (Integer) attributes.get("StartX");
		Integer startY = (Integer) attributes.get("StartY");
		Integer width = (Integer) attributes.get("Width");
		Integer heigth = (Integer) attributes.get("Height");
		
		String data = "";
		try {
			data = extractDataFromPdf(pdfFilePath, pageNumber, startX, startY, width, heigth);
			nlpResponseModel.setMessage("Data from Pdf is "+data);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to fetch Data from Pdf "+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("Data", data);
		return nlpResponseModel;
	}

	public static String extractDataFromPdf(String filePath, int pageNumber, Integer startX, Integer startY, Integer width, Integer height) throws Exception {
		// Load the PDF document
		PDDocument document = PDDocument.load(new File(filePath));

		// Define the rectangle area
		Rectangle rect = new Rectangle((int) startX, (int) startY, (int) width, (int) height);

		// Extract text from the specified area on the page
		PDFTextStripperByArea stripper = new PDFTextStripperByArea();
		stripper.setSortByPosition(true);
		stripper.addRegion("customRegion", rect);
		stripper.extractRegions(document.getPage(pageNumber-1));

		// Get the extracted text
		String extractedText = stripper.getTextForRegion("customRegion");

		// Close the PDF document
		document.close();
		return extractedText;
	}
	
} 