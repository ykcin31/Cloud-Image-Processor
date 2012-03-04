import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.io.FileUtils;

import java.util.zip.*;

// Uploads, extracts, and lists images contained in the user-selected .zip file. 
// Draws interface for setting desired operations for each image.
// Nick Wong
@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	// Location to save .zip file and store image files
	private final String FILEPATH = "C:\\Users\\Nick\\workspace\\imagecloud\\image-cloud\\data\\";
	// Location to save data that is larger than MAXMEMSIZE.
	private final String TEMPPATH = "C:\\Users\\Nick\\workspace\\imagecloud\\image-cloud\\temp\\";
	// Maximum file size to be uploaded.
	private final int MAXFILESIZE = 100*1000000;
	// Maximum size that will be stored in memory
	private final int MAXMEMSIZE = 100*1000000;
	// Number of image processing operations
	private final int OPERATIONS = 2;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		HtmlPrinter.header(out);

		// Upload file
		// Verify the content type of request
		String contentType = request.getContentType();
		if ((contentType.indexOf("multipart/form-data") >= 0)) 
		{
			try
			{ 
				// Load file request
				String fileName = loadFile(request);
				// Extract the images in the .zip file
				Zipper z = new Zipper();
				String dirName = z.extract(fileName,FILEPATH);
				// Create collection of Jobs containing the image files contained in the loaded .zip file
				ArrayList<Job> listing = listImages(dirName,OPERATIONS);
				// Print table of image listing
				HtmlPrinter.uploadPage(out, listing, dirName);
				HtmlPrinter.footer(out);
				out.close();
			}
			catch(Exception ex)
			{
				HtmlPrinter.error(out, 0, ex);
				HtmlPrinter.footer(out);
				out.close();
				System.out.println(ex);
			}
		}
		else
		{
			HtmlPrinter.error(out, 0);
			HtmlPrinter.footer(out);
			out.close();
		}
	}

	// Save the requested file
	private String loadFile(HttpServletRequest request) throws Exception
	{
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

		FileItem fi = (FileItem)fileItems.get(0);
		if (!fi.isFormField ())	
		{
			// Get the uploaded file parameters
			fileName = fi.getName();
			// Write the file
			if(fileName.lastIndexOf("\\") >= 0 )
			{
				file = new File(FILEPATH + fileName.substring(fileName.lastIndexOf("\\")));
			}
			else
			{
				file = new File(FILEPATH + fileName.substring(fileName.lastIndexOf("\\")+1));
			}
			fi.write(file);
		}
		return file.toString();
	}

	// Extract the images and return a list describing the images
	private ArrayList<Job> listImages(String dirName, int opCount)
	{
		// Create collection of Jobs for image files
		File dir = new File(dirName);
		File[] files;
		files = dir.listFiles();
		Arrays.sort(files);
		ArrayList<Job> listing = new ArrayList<Job>();
		for (int j = 0; j < files.length; j++)
		{
			// Image file address
			String address =  files[j].toString();
			// Check for image file extension compatibility (.jpg, .png, .gif)
			int mid = address.lastIndexOf(".");
			String ext = address.substring(mid+1);
			if (ext.equalsIgnoreCase("jpg")||ext.equalsIgnoreCase("jpeg")||ext.equalsIgnoreCase("jpe")||ext.equalsIgnoreCase("gif")||ext.equalsIgnoreCase("png"))
			{
				// Image file size
				float size = ((float)files[j].length())/1000000; 
				// Operations toggle default values are 0
				int[] operations = new int[opCount];	
				int status = 0;
				int parcel = -1;
				String destination = null;
				listing.add(new Job(address, size, operations, status, parcel, destination));	
			}
		}
		return listing;
	}
}