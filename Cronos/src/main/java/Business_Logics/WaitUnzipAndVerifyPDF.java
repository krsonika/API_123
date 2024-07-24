package Business_Logics;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class WaitUnzipAndVerifyPDF implements Nlp {
	@InputParams({@InputParam(name = "Max Wait Time(in seconds)", type = "java.lang.Integer"), @InputParam(name = "Expected Data", type = "java.lang.String")})

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
		Integer waitTime = (Integer) attributes.get("Max Wait Time(in seconds)");
		String expectedData = (String) attributes.get("Expected Data");

		// Your program element business logic goes here ...
		try {
			Integer maxWaitTime = waitTime;
			boolean flag = false;
			int count = 0;
			File latestFile = null;
			while (true) {
				latestFile = getLatestFile();
				if (latestFile != null) {
					String FileName = latestFile.getName();
					if (!FileName.endsWith(".crdownload") && !FileName.endsWith(".tmp")) {
						System.out.println("File downloaded successfully.");
						flag = true;
						break;
					}
				} else {
					Thread.sleep(1000);
					if (count >= maxWaitTime) {
						System.out.println("No files donloaded within the last " + maxWaitTime + " seconds.");
						break;
					}else {
						count++;
					}
				}
			}

			String absolutePathOfPdf = "";
			if (flag) {
				unzip(latestFile.getAbsolutePath(), latestFile.getParent());
				absolutePathOfPdf = new File(latestFile.getAbsolutePath().split("\\.")[0]).listFiles()[0].getAbsolutePath();
				System.out.println(absolutePathOfPdf);
				String actualPdfContent = fetchDataFromPDF(absolutePathOfPdf);
				String expectedContent = expectedData;

				List<String> notPresentInPDF = new LinkedList<String>();
				for (int i = 0; i < expectedContent.split(",").length; i++) {
					if (!actualPdfContent.contains(expectedContent.split(",")[i].trim())) {
						notPresentInPDF.add(expectedContent.split(",")[i].trim());
					}
				}

				System.out.println(notPresentInPDF.size());
				if (notPresentInPDF.size()==0) {
					nlpResponseModel.setMessage("Expected data present in PDF File");
					nlpResponseModel.setStatus(CommonConstants.pass);
					System.out.println("Expected data present in PDF File");
				} else {
					nlpResponseModel.setMessage("ALL/Some data is not in Pdf File those are "+notPresentInPDF);
					nlpResponseModel.setStatus(CommonConstants.fail);
					System.out.println("ALL/Some data is not in Pdf File those are "+notPresentInPDF);
				}
			}else {
				System.out.println("fail msg and status");
				nlpResponseModel.setMessage("No files donloaded within the last " + maxWaitTime + " seconds");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
			System.out.println(flag);

		}catch (Exception e) {
			nlpResponseModel.setMessage("Failed to unzip and verify PDF");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static File getLatestFile() {
		File directory = new File(System.getProperty("user.home")+ File.separator + "Downloads");
		File[] files = directory.listFiles();

		// Find the latest file updated within the last 1 second
		File latestFile = null;
		long currentTime = System.currentTimeMillis();
		long lastModifiedThreshold = currentTime - 10000; // 1 second ago
		for (File file : files) {
			if (file.isFile() && file.lastModified() > lastModifiedThreshold) {
				latestFile = file;
				break; // Found the latest file updated within the last 1 second
			}
		}
		return latestFile;
	}

	public static void unzip(String zipFilePath, String destDir) throws IOException {
		try (FileInputStream fis = new FileInputStream(zipFilePath);
				ZipInputStream zis = new ZipInputStream(fis)) {

			ZipEntry zipEntry = zis.getNextEntry();
			// iterate over all entries in the zip file
			while (zipEntry != null) {
				String filePath = destDir + File.separator + zipEntry.getName();
				if (!zipEntry.isDirectory()) {
					// Ensure that files are extracted inside below directory
					String parentDirPath = zipFilePath.split("\\.")[0];
					File parentDir = new File(parentDirPath);
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					filePath = parentDirPath + File.separator + zipEntry.getName().substring(zipEntry.getName().indexOf('/') + 1);
					extractFile(zis, filePath);
				} else {
					// if the entry is a directory, create it
					File dirEntry = new File(filePath);
					dirEntry.mkdirs();
				}
				zis.closeEntry();
				zipEntry = zis.getNextEntry();
			}
		}
	}

	private static void extractFile(ZipInputStream zis, String filePath) throws IOException {
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = zis.read(buffer)) > 0) {
				bos.write(buffer, 0, len);
			}
		}
	}
	
	public static String fetchDataFromPDF(String pdfFilePath) throws IOException {
		try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
			PDFTextStripper pdfStripper = new PDFTextStripper();
			String text = pdfStripper.getText(document);
			return text;
		}
	}
	
	public static void main(String[] args) {
		NlpRequestModel nlp = new NlpRequestModel();
		nlp.getAttributes().put("Max Wait Time(in seconds)", 60);
		nlp.getAttributes().put("Expected Data", "channa,swaraj,jitu");
		
		try {
			NlpResponseModel res = new WaitUnzipAndVerifyPDF().execute(nlp);
		} catch (NlpException e) {
			e.printStackTrace();
		}
	}
} 