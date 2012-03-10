import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.io.FileUtils;

// Uploads, extracts, and lists images contained in the user-selected .zip file. 
// Draws interface for setting desired operations for each image.
// Nick Wong

@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
	// SETUP fields
	// -----------------------------------------------------------------------------------------------------------------
	// Enter directory to save .zip file and store image files
	private final String FILEPATH = "C:\\Users\\Nick\\Desktop\\image-cloud\\data\\";
	// Enter directory to save data that is larger than MAXMEMSIZE.
	private final String TEMPPATH = "C:\\Users\\Nick\\Desktop\\image-cloud\\temp\\";
	// Enter maximum file size [bytes] to be uploaded.
	private final int MAXFILESIZE = 100 * 1000000;
	// Enter maximum file size [bytes] that will be stored in memory
	private final int MAXMEMSIZE = 100 * 1000000;
	// Enter number of image processing operations
	private final int OPERATIONS = 2;
	// Enter setting for printing to console
	private final boolean PRINT = false;
	// -----------------------------------------------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		FileUtils.deleteDirectory(new File(FILEPATH));
		FileUtils.deleteDirectory(new File(TEMPPATH));

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		HtmlPrinter page = new HtmlPrinter();
		// Upload file
		// Verify the content type of request
		String contentType = request.getContentType();
		if ((contentType.indexOf("multipart/form-data") >= 0)) {
			try {
				// Create directories if necessary
				File dataDir = new File(FILEPATH);
				if (!dataDir.exists()) {
					dataDir.mkdirs();
					printToConsole("UploadServlet - Created folder: "
							+ dataDir.toString());
				}
				File tempDir = new File(TEMPPATH);
				if (!tempDir.exists()) {
					tempDir.mkdirs();
					printToConsole("UploadServlet - Created folder: "
							+ tempDir.toString());
				}
				// Load file request
				String fileName = loadFile(request);
				// Extract the images in the .zip file
				Zipper z = new Zipper();
				String dirName = z.extract(fileName, FILEPATH);
				printToConsole("UploadServlet - Created folder: " + dirName);

				// Create collection of Jobs containing the image files
				// contained in the loaded .zip file
				ArrayList<Job> listing = listImages(dirName, OPERATIONS);
				printToConsole("UploadServlet - Image files uploaded and extracted");

				// Print HTML
				page.header(out);
				page.uploadPage(out, listing, dirName);
				page.footer(out);
				out.close();
			} catch (Exception ex) {
				page.error(out, 0, ex);
				page.footer(out);
				out.close();
				ex.printStackTrace();
			}
		} else {
			page.error(out, 0);
			page.footer(out);
			out.close();
		}
	}

	// Save the requested file
	private String loadFile(HttpServletRequest request) throws Exception {
		File file = null;
		String fileName = null;

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(MAXMEMSIZE);
		factory.setRepository(new File(TEMPPATH));

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(MAXFILESIZE);
		// Parse the request to get file items.
		List fileItems = upload.parseRequest(request);

		FileItem fi = (FileItem) fileItems.get(0);
		if (!fi.isFormField()) {
			// Get the uploaded file parameters
			fileName = fi.getName();
			// Write the file
			if (fileName.lastIndexOf("\\") >= 0) {
				file = new File(FILEPATH
						+ fileName.substring(fileName.lastIndexOf("\\")));
			} else {
				file = new File(FILEPATH
						+ fileName.substring(fileName.lastIndexOf("\\") + 1));
			}
			fi.write(file);
		}
		return file.toString();
	}

	// Extract the images and return a list describing the images
	private ArrayList<Job> listImages(String dirName, int opCount) {
		// Create collection of Jobs for image files
		File dir = new File(dirName);
		File[] files;
		files = dir.listFiles();
		Arrays.sort(files);
		ArrayList<Job> listing = new ArrayList<Job>();
		for (int j = 0; j < files.length; j++) {
			// Image file address
			String address = files[j].toString();
			// Check for image file extension compatibility (.jpg, .png, .gif)
			int mid = address.lastIndexOf(".");
			String ext = address.substring(mid + 1);
			if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")
					|| ext.equalsIgnoreCase("jpe")
					|| ext.equalsIgnoreCase("gif")
					|| ext.equalsIgnoreCase("png")) {
				// Image file size
				float size = ((float) files[j].length()) / 1000000;
				// Operations toggle default values are 0
				int[] operations = new int[opCount];
				int status = 0;
				int parcel = -1;
				String destination = address;
				listing.add(new Job(address, size, operations, status, parcel,
						destination));
			}
		}
		return listing;
	}

	// Print to System console
	private void printToConsole(String s) {
		if (this.PRINT == true) {
			System.out.println(s);
		}
	}
}