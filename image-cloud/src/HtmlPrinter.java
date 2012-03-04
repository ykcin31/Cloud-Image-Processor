import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// This class is used for printing blocks of HTML code for the web-based user interface
public class HtmlPrinter 
{
	// Prints the code at the head of the HTML file, includes HTML document header and page title
	public static void header(PrintWriter out) throws IOException
	{
		// Print page heading
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("<meta charset=\"utf-8\">");
		out.println("<title>Cloud Image Processor</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h1>Cloud Image Processor</h1>");
	}

	// Print body corresponding to UploadServlet
	public static void uploadPage(PrintWriter out, ArrayList<Job> listing, String directory) throws IOException
	{
		// Print table listing for images on "Upload" page
		out.println("<h2>Select image processing operations:</h2>");
		// Create form and table entries
		out.println("<form action=\"ProcessServlet\" method=\"post\">");
		out.println("<input type=\"hidden\" name=\"directory\" value=\"" + directory + "\" />");
		out.println("<input type=\"hidden\" name=\"count\" value=\"" + listing.size() + "\" />");
		out.println("<input type=\"hidden\" name=\"opCount\" value=\"" + listing.get(0).getOperations().length + "\" />");
		// Print table header
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Filename</th>");
		out.println("<th>Thumbnail</th>");
		out.println("<th>Filesize (MB)</th>");
		out.println("<th>Image Equalization</th>");
		out.println("<th>Edge-detection</th>");
		out.println("<th>Status</th>");
		out.println("</tr>");
		for (int i = 0; i < listing.size(); i++) 
		{
			out.println("<tr>");
			tableName(out, listing.get(i));
			tableImg(out, listing.get(i));
			tableSize(out, listing.get(i));
			tableOptions(out, listing.get(i), i, 0);
			tableStatus(out, listing.get(i));
			formData(out, listing.get(i), i);
			out.println("</tr>");
		}

		// Close table
		out.println("</table>");
		out.println("<br/>");
		out.println("<input type=\"submit\" value=\"Submit\" />");
		out.println("</form>");
	}

	// Print body corresponding to UploadServlet
	public static void processPage(PrintWriter out, ArrayList<Job> listing, String directory) throws IOException
	{
		// Print table listing for images on "Upload" page
		out.println("<h2>Processing:</h2>");
		// Create form and table entries
		out.println("<form action=\"ProcessServlet\" method=\"post\">");
		out.println("<input type=\"hidden\" name=\"directory\" value=\"" + directory + "\" />");
		out.println("<input type=\"hidden\" name=\"count\" value=\"" + listing.size() + "\" />");
		out.println("<input type=\"hidden\" name=\"opCount\" value=\"" + listing.get(0).getOperations().length + "\" />");
		// Print table header
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Filename</th>");
		out.println("<th>Thumbnail</th>");
		out.println("<th>Filesize (MB)</th>");
		out.println("<th>Image Equalization</th>");
		out.println("<th>Edge-detection</th>");
		out.println("<th>Status</th>");
		out.println("</tr>");
		for (int i = 0; i < listing.size(); i++) 
		{
			out.println("<tr>");
			tableName(out, listing.get(i));
			tableImg(out, listing.get(i));
			tableSize(out, listing.get(i));
			tableOptions(out, listing.get(i), i, 1);
			tableStatus(out, listing.get(i));
			formData(out, listing.get(i), i);
			out.println("</tr>");
		}

		// Close table
		out.println("</table>");
		out.println("<br/>");
		//out.println("<input type=\"submit\" value=\"Process\" />");
		out.println("</form>");
	}

	// Print hidden form data
	private static void formData(PrintWriter out, Job entry, int i)
	{
		out.println("<input type=\"hidden\" name=\"address" + i + "\" value=\"" + entry.getAddress() + "\"/>");		
		out.println("<input type=\"hidden\" name=\"size" + i + "\" value=\"" + String.format("%3.2f",entry.getSize()) + "\"/>");		
		out.println("<input type=\"hidden\" name=\"status" + i + "\" value=\"" + Integer.toString(entry.getStatus()) + "\"/>");		
		out.println("<input type=\"hidden\" name=\"parcel" + i + "\" value=\"" + Integer.toString(entry.getParcel()) + "\"/>");		
		out.println("<input type=\"hidden\" name=\"destination" + i + "\" value=\"" + entry.getDestination() + "\"/>");		
	}

	// Returns abbreviated name from full address of file
	private static String abbreviate (String address)
	{
		String[] splits = address.split("\\\\");
		String name = splits[splits.length-1];
		return name;
	}

	// Print abbreviated name of entry	
	public static void tableName (PrintWriter out, Job entry) throws IOException
	{
		String name = abbreviate(entry.getAddress());		
		out.println("<td>");
		out.println("<p>" + name + "</p>");
		out.println("</td>");
	}

	// Print thumb nail of entry	
	public static void tableImg (PrintWriter out, Job entry) throws IOException
	{
		String address = entry.getAddress();
		String name = abbreviate(address);

		out.println("<td>");
		out.println("<img src = "+ "\"http://localhost:8080/image-cloud/ViewImage?file=" + address +"\"" 
				+ " alt = \"" + name + "\""
				+ " title = \"" + name + "\""
				+ " width = \"75\" />");
		out.println("</td>");
	}

	// Print file size of entry
	public static void tableSize (PrintWriter out, Job entry) throws IOException
	{
		String size = String.format("%3.2f",entry.getSize());

		out.println("<td>");
		out.println("<p>" + size + "</p>");
		out.println("</td>");
	}

	// Print check boxes for all options			
	public static void tableOptions (PrintWriter out, Job entry, int row, int mode) throws IOException
	{
		int[] operations = entry.getOperations();
		int n = operations.length;
		if (mode == 0)
		{
			for(int i = 0; i < n; i++)
			{
				out.println("<td>");
				out.println("<input type = \"checkbox\" name=\"operations" + row + "\""
						+ " value = \"" + Integer.toString(i) + "\" />");
				out.println("</td>");
			}
		}
		if(mode == 1)
		{
			for(int i = 0; i < n; i++)
			{
				out.println("<td>");
				if(operations[i] == 0)
				{
					out.println("<input type = \"checkbox\" disabled = \"disabled\"/>");
				}
				if(operations[i] == 1)
				{
					out.println("<input type = \"checkbox\" disabled = \"disabled\" checked = \"yes\"/>");
					out.println("<input type=\"hidden\" name=\"operations" + row + "\" value=\"" + Integer.toString(i) + "\"/>");		
				}
				out.println("</td>");
			}
		}

	}

	// Print status			
	public static void tableStatus (PrintWriter out, Job entry) throws IOException
	{
		int status = entry.getStatus();

		if(status == 0)
		{
			out.println("<td>");
			out.println("<p>Waiting</p>");
			out.println("</td>");
		}
		if(status == 1)
		{
			out.println("<td>");
			out.println("<p>Processing...</p>");
			out.println("</td>");
		}
		if(status == 2)
		{
			String address = entry.getDestination();
			String name = abbreviate(address);

			out.println("<td>");
			out.println("<img src = "+ "\"http://localhost:8080/image-cloud/ViewImage?file=" + address +"\"" 
					+ " alt = \"" + name + "\"" 
					+ " width = \"75\" />");
			out.println("</td>");

		}
		if(status == 3)
		{
			out.println("<td>");
			out.println("<p>Image exceeds allowable file size</p>");
			out.println("</td>");
		}
	}

	// Print error message
	public static void error(PrintWriter out, int i) throws IOException
	{
		if (i == 0)
		{
			out.println("<h3>Error retrieving request. Try Uploading again.</h3>");
		}
	}

	// Print error message with exception message
	public static void error(PrintWriter out, int i, Exception e) throws IOException
	{
		if (i == 0)
		{
			out.println("<h3>Error retrieving request. Try Uploading again.</h3>");
		}
		out.println("<p>Exception caught:");
		out.println("<br/>");		
		e.printStackTrace();
		out.println("</p>");
	}

	// Print closing lines of HTML document
	public static void footer(PrintWriter out) throws IOException
	{
		out.println("</body>");
		out.println("</html>");
	}	

}