import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// This class is used for printing blocks of HTML code for the web-based user interface
public class HtmlPrinter {
	public HtmlPrinter() {

	}

	// Prints the code at the head of the HTML file, includes HTML document
	// header and page title
	public void header(PrintWriter out) throws IOException {
		// Print page heading
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("<meta charset=\"utf-8\">");
		// CSS
		out.println("<style type = \"text/css\">");
		out.println("body{font-family:georgia,sans-serif;text-align:center; background-color:whitesmoke}");
		out.println("table{width:80%; margin-left:10%; margin-right:10%; align:center; border-collapse:collapse; border-bottom: 5px double black;}");
		out.println("td{width:200px; height:300px; text-align:center; vertical-align:middle; border-bottom: 2px solid black;}");
		out.println("th{width:200px; height:25px; text-align:center; vertical-align:middle; border-bottom: 5px double black;}");
		out.println("</style>");

		out.println("<title>Cloud Image Processor</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h1>Cloud Image Processor</h1>");
	}

	// Print body corresponding to UploadServlet
	public void uploadPage(PrintWriter out, ArrayList<Job> listing,
			String directory) throws IOException {
		// Print table listing for images on "Upload" page
		// out.println("<h2>Select image processing operations:</h2>");
		// Create form and table entries
		out.println("<form action=\"ProcessServlet\" method=\"post\">");
		out.println("<input type=\"hidden\" name=\"directory\" value=\""
				+ directory + "\" />");
		out.println("<input type=\"hidden\" name=\"count\" value=\""
				+ listing.size() + "\" />");
		out.println("<input type=\"hidden\" name=\"opCount\" value=\""
				+ listing.get(0).getOperations().length + "\" />");
		// Print table header
		out.println("<table>");
		out.println("<tr>");
		// out.println("<th>Filename</th>");
		out.println("<th></th>");
		out.println("<th>File size (MB)</th>");
		out.println("<th>Image Equalization</th>");
		out.println("<th>Edge Detection</th>");
		out.println("<th>Result</th>");
		out.println("</tr>");
		for (int i = 0; i < listing.size(); i++) {
			out.println("<tr>");
			// tableName(out, listing.get(i));
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
	public void processPage(PrintWriter out, ArrayList<Job> listing,
			String directory, String dl) throws IOException {
		// Print table listing for images on "Upload" page
		// out.println("<h2>Processing complete:</h2>");
		// Create form and table entries
		out.println("<form action=\"ProcessServlet\" method=\"post\">");
		out.println("<input type=\"hidden\" name=\"directory\" value=\""
				+ directory + "\" />");
		out.println("<input type=\"hidden\" name=\"count\" value=\""
				+ listing.size() + "\" />");
		out.println("<input type=\"hidden\" name=\"opCount\" value=\""
				+ listing.get(0).getOperations().length + "\" />");
		// Print table header
		out.println("<table>");
		out.println("<tr>");
		// out.println("<th>Filename</th>");
		out.println("<th></th>");
		out.println("<th>File size (MB)</th>");
		out.println("<th>Histogram Equalization</th>");
		out.println("<th>Edge Detection</th>");
		out.println("<th>Result</th>");
		out.println("</tr>");
		for (int i = 0; i < listing.size(); i++) {
			out.println("<tr>");
			// tableName(out, listing.get(i));
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

		out.println("<h2>What would you like to do next?</h2>");
		boolean retry = false;
		for (int i = 0; i < listing.size(); i++) {
			if (listing.get(i).getStatus() == 4) {
				retry = true;
				break;
			}
		}
		if (retry == true) {
			out.println("<input type = \"radio\" name=\"option\" value = \"Retry\" />");
			out.println("Reprocess failed jobs<br/>");
		}
		boolean save = false;
		for (int i = 0; i < listing.size(); i++) {
			if (listing.get(i).getStatus() == 2) {
				save = true;
				break;
			}
		}
		if (save == true) {
			out.println("<a href=\"" + dl
					+ "\">Download</a> processed images<br/>");
		}
		String add = "http://localhost:8080/image-cloud/Upload.html";
		out.println("<a href=\"" + add
				+ "\">Upload</a> another archive<br/><br/>");
		out.println("<input type=\"submit\" value=\"Submit\" />");
		out.println("</form>");
	}

	// Print hidden form data
	private void formData(PrintWriter out, Job entry, int i) {
		out.println("<input type=\"hidden\" name=\"address" + i + "\" value=\""
				+ entry.getAddress() + "\"/>");
		out.println("<input type=\"hidden\" name=\"size" + i + "\" value=\""
				+ String.format("%3.2f", entry.getSize()) + "\"/>");
		out.println("<input type=\"hidden\" name=\"status" + i + "\" value=\""
				+ Integer.toString(entry.getStatus()) + "\"/>");
		out.println("<input type=\"hidden\" name=\"parcel" + i + "\" value=\""
				+ Integer.toString(entry.getParcel()) + "\"/>");
		out.println("<input type=\"hidden\" name=\"destination" + i
				+ "\" value=\"" + entry.getDestination() + "\"/>");
	}

	// Returns abbreviated name from full address of file
	private String abbreviate(String address) {
		String[] splits = address.split("\\\\");
		String name = splits[splits.length - 1];
		return name;
	}

	// Print abbreviated name of entry
	public void tableName(PrintWriter out, Job entry) throws IOException {
		String name = abbreviate(entry.getAddress());
		out.println("<td>");
		out.println("<p>" + name + "</p>");
		out.println("</td>");
	}

	// Print thumb nail of entry
	public void tableImg(PrintWriter out, Job entry) throws IOException {
		String address = entry.getAddress();
		String name = abbreviate(address);
		out.println("<td>");
		/*out.println("<img src = "
				+ "\"http://localhost:8080/image-cloud/ViewImage?file="
				+ address + "\"" + " alt = \"" + name + "\"" + " title = \""
				+ name + "\"" + " width = \"200\" />");
		*/	
		out.println("<img src = " + "\"" + address + "\"" + " alt = \"" + name + "\"" + " width = \"200\" />");
		out.println("</td>");
	}

	// Print file size of entry
	public void tableSize(PrintWriter out, Job entry) throws IOException {
		String size = String.format("%3.2f", entry.getSize());

		out.println("<td>");
		out.println("<p>" + size + "</p>");
		out.println("</td>");
	}

	// Print check boxes for all options
	public void tableOptions(PrintWriter out, Job entry, int row, int mode)
			throws IOException {
		int[] operations = entry.getOperations();
		int n = operations.length;
		if (mode == 0) {
			for (int i = 0; i < n; i++) {
				String script = " onmousedown=\"this.__chk = this.checked\" onclick=\"if (this.__chk) this.checked = false\"";

				out.println("<td>");
				out.println("<input type = \"radio\" name=\"operations" + row
						+ "\"" + " value = \"" + Integer.toString(i) + "\""
						+ script + "\"/>");

				out.println("</td>");
			}
		}
		if (mode == 1) {
			for (int i = 0; i < n; i++) {
				out.println("<td>");
				if (operations[i] == 0) {
					out.println("<input type = \"radio\" disabled = \"disabled\"/>");
				}
				if (operations[i] == 1) {
					out.println("<input type = \"radio\" disabled = \"disabled\" checked = \"yes\"/>");
					out.println("<input type=\"hidden\" name=\"operations"
							+ row + "\" value=\"" + Integer.toString(i)
							+ "\"/>");
				}
				out.println("</td>");
			}
		}

	}

	// Print status
	public void tableStatus(PrintWriter out, Job entry) throws IOException {
		int status = entry.getStatus();

		if (status == 0) {
			out.println("<td>");
			out.println("<p>Uploaded</p>");
			out.println("</td>");
		}
		if (status == 1) {
			String address = entry.getDestination();
			String name = abbreviate(address);
			out.println("<td>");
			/*
			 * out.println("<img src = " +
			 * "\"http://localhost:8080/image-cloud/ViewImage?file=" + address +
			 * "\"" + " alt = \"" + name + "\"" + " width = \"200\" />");
			 */
			out.println("<img src = " + "\"" + address + "\"" + " alt = \"" + name + "\"" + " width = \"200\" />");
			out.println("</td>");
		}
		if (status == 2) {
			String address = entry.getDestination();
			String name = abbreviate(address);
			out.println("<td>");
			/*
			 * out.println("<img src = " +
			 * "\"http://localhost:8080/image-cloud/ViewImage?file=" + address +
			 * "\"" + " alt = \"" + name + "\"" + " width = \"200\" />");
			 */
			out.println("<img src = " + "\"" + address + "\"" + " alt = \"" + name + "\"" + " width = \"200\" />");
			out.println("</td>");
		}
		if (status == 3) {
			out.println("<td>");
			out.println("<p>Image exceeds allowable file size</p>");
			out.println("</td>");
		}
		if (status == 4) {
			out.println("<td>");
			out.println("<p>Processing error</p>");
			out.println("</td>");
		}
	}

	// Print error message
	public void error(PrintWriter out, int i) throws IOException {
		if (i == 0) {
			out.println("<h3>Error retrieving request. Try Uploading again.</h3>");
		}
	}

	// Print error message with exception message
	public void error(PrintWriter out, int i, Exception e) throws IOException {
		if (i == 0) {
			out.println("<h3>Error retrieving request. Try Uploading again.</h3>");
		}
		out.println("<p>Exception caught:");
		out.println("<br/>");
		e.printStackTrace();
		out.println("</p>");
	}

	// Print closing lines of HTML document
	public void footer(PrintWriter out) throws IOException {
		out.println("</body>");
		out.println("</html>");
	}

}