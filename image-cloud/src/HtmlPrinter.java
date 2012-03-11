import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// This class is used for printing blocks of HTML code for the web-based user interface
public class HtmlPrinter {
	public HtmlPrinter() {

	}

	// Prints the code at the head of the HTML file, includes HTML document
	// header, css, and page title
	public void header(PrintWriter out) throws IOException {
		// Print page heading
		out.print("<!DOCTYPE html>");
		out.print("<html>");
		out.print("<head>");
		out.print("<meta charset=\"utf-8\">");
		// CSS
		out.print("<style type = \"text/css\">");
		out.print("body{font-family:georgia,sans-serif;text-align:center; background-color:whitesmoke}");
		out.print("table{width:80%; margin-left:10%; margin-right:10%; align:center; border-collapse:collapse; border-bottom: 5px double black;}");
		out.print("td{width:200px; height:300px; text-align:center; vertical-align:middle; border-bottom: 2px solid black;}");
		out.print("th{width:200px; height:25px; text-align:center; vertical-align:middle; border-bottom: 5px double black;}");
		out.print("</style>");

		out.print("<title>Cloud Image Processor</title>");
		out.print("</head>");
		out.print("<body>");
		out.print("<h1>Cloud Image Processor</h1>");
	}

	public void homePage(PrintWriter out) throws IOException {
		out.print("<form action=\"UploadServlet\" method=\"post\" enctype=\"multipart/form-data\">");
		out.print("<table><tr><th></th><th></th><th>File size (MB)</th><th>Image Equalization</th><th>Edge Detection</th><th>Result</th></tr>");
		out.print("<tr><td></td><td></td><td></td><td></td><td></td><td></td></tr>");
		out.print("</table>");
		out.print("<br/>");
		out.print("<input type=\"file\" name=\"file\" size=\"50\"/><br/><br/>");
		out.print("<input type=\"submit\" value=\"Upload Archive\"/>");
		out.print("</form>");

	}

	// Print body corresponding to UploadServlet
	public void uploadPage(PrintWriter out, ArrayList<Job> listing,
			String directory) throws IOException {
		// Print table listing for images on "Upload" page
		// out.print("<h2>Select image processing operations:</h2>");
		// Create form and table entries
		out.print("<form action=\"ProcessServlet\" method=\"post\">");
		out.print("<input type=\"hidden\" name=\"directory\" value=\""
				+ directory + "\" />");
		out.print("<input type=\"hidden\" name=\"count\" value=\""
				+ listing.size() + "\" />");
		out.print("<input type=\"hidden\" name=\"opCount\" value=\""
				+ listing.get(0).getOperations().length + "\" />");
		// Print table header
		out.print("<table>");
		out.print("<tr>");
		// out.print("<th>Filename</th>");
		out.print("<th></th>");
		out.print("<th>File size (MB)</th>");
		out.print("<th>Image Equalization</th>");
		out.print("<th>Edge Detection</th>");
		out.print("<th>Result</th>");
		out.print("</tr>");
		for (int i = 0; i < listing.size(); i++) {
			out.print("<tr>");
			// tableName(out, listing.get(i));
			tableImg(out, listing.get(i));
			tableSize(out, listing.get(i));
			tableOptions(out, listing.get(i), i, 0);
			tableStatus(out, listing.get(i));
			out.print("</tr>");
		}

		// Close table
		out.print("</table>");
		for (int i = 0; i < listing.size(); i++) {
			formData(out, listing.get(i), i);
		}

		out.print("<br/>");
		out.print("<input type=\"submit\" value=\"Submit\" />");
		out.print("</form>");
	}

	// Print body corresponding to UploadServlet
	public void processPage(PrintWriter out, ArrayList<Job> listing,
			String directory, String dl) throws IOException {
		// Print table listing for images on "Upload" page
		// out.print("<h2>Processing complete:</h2>");
		// Create form and table entries
		out.print("<form action=\"ProcessServlet\" method=\"post\">");
		out.print("<input type=\"hidden\" name=\"directory\" value=\""
				+ directory + "\" />");
		out.print("<input type=\"hidden\" name=\"count\" value=\""
				+ listing.size() + "\" />");
		out.print("<input type=\"hidden\" name=\"opCount\" value=\""
				+ listing.get(0).getOperations().length + "\" />");
		// Print table header
		out.print("<table>");
		out.print("<tr>");
		// out.print("<th>Filename</th>");
		out.print("<th></th>");
		out.print("<th>File size (MB)</th>");
		out.print("<th>Histogram Equalization</th>");
		out.print("<th>Edge Detection</th>");
		out.print("<th>Result</th>");
		out.print("</tr>");
		for (int i = 0; i < listing.size(); i++) {
			out.print("<tr>");
			// tableName(out, listing.get(i));
			tableImg(out, listing.get(i));
			tableSize(out, listing.get(i));
			tableOptions(out, listing.get(i), i, 1);
			tableStatus(out, listing.get(i));
			formData(out, listing.get(i), i);
			out.print("</tr>");
		}

		// Close table
		out.print("</table>");
		out.print("<br/>");

		out.print("<h2>What would you like to do next?</h2>");
		boolean retry = false;
		for (int i = 0; i < listing.size(); i++) {
			if (listing.get(i).getStatus() == 4) {
				retry = true;
				break;
			}
		}
		if (retry == true) {
			out.print("<input type = \"radio\" name=\"option\" value = \"Retry\" />");
			out.print("Reprocess failed jobs<br/>");
		}
		boolean save = false;
		for (int i = 0; i < listing.size(); i++) {
			if (listing.get(i).getStatus() == 2) {
				save = true;
				break;
			}
		}
		if (save == true) {
			out.print("<a href=\"" + dl
					+ "\">Download</a> processed images<br/>");
		}
		String add = "http://localhost:8080/image-cloud/Upload.html";
		out.print("<a href=\"" + add
				+ "\">Upload</a> another archive<br/><br/>");
		out.print("<input type=\"submit\" value=\"Submit\" />");
		out.print("</form>");
	}

	// Print hidden form data
	private void formData(PrintWriter out, Job entry, int i) {
		out.print("<input type=\"hidden\" name=\"address" + i + "\" value=\""
				+ entry.getAddress() + "\"/>");
		out.print("<input type=\"hidden\" name=\"size" + i + "\" value=\""
				+ String.format("%3.2f", entry.getSize()) + "\"/>");
		out.print("<input type=\"hidden\" name=\"status" + i + "\" value=\""
				+ Integer.toString(entry.getStatus()) + "\"/>");
		out.print("<input type=\"hidden\" name=\"parcel" + i + "\" value=\""
				+ Integer.toString(entry.getParcel()) + "\"/>");
		out.print("<input type=\"hidden\" name=\"destination" + i
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
		out.print("<td>");
		out.print("<p>" + name + "</p>");
		out.print("</td>");
	}

	// Print thumb nail of entry
	public void tableImg(PrintWriter out, Job entry) throws IOException {
		String address = entry.getAddress();
		String name = abbreviate(address);
		out.print("<td>");
		/*
		 * out.print("<img src = " +
		 * "\"http://localhost:8080/image-cloud/ViewImage?file=" + address +
		 * "\"" + " alt = \"" + name + "\"" + " title = \"" + name + "\"" +
		 * " width = \"200\" />");
		 */
		out.print("<img src = " + "\"" + address + "\"" + " alt = \"" + name
				+ "\"" + " width = \"200\" />");
		out.print("</td>");
	}

	// Print file size of entry
	public void tableSize(PrintWriter out, Job entry) throws IOException {
		String size = String.format("%3.2f", entry.getSize());

		out.print("<td>");
		out.print("<p>" + size + "</p>");
		out.print("</td>");
	}

	// Print check boxes for all options
	public void tableOptions(PrintWriter out, Job entry, int row, int mode)
			throws IOException {
		int[] operations = entry.getOperations();
		int n = operations.length;
		if (mode == 0) {
			for (int i = 0; i < n; i++) {
				String script = " onmousedown=\"this.__chk = this.checked\" onclick=\"if (this.__chk) this.checked = false\"";

				out.print("<td>");
				out.print("<input type = \"radio\" name=\"operations" + row
						+ "\"" + " value = \"" + Integer.toString(i) + "\""
						+ script + "/>");

				out.print("</td>");
			}
		}
		if (mode == 1) {
			for (int i = 0; i < n; i++) {
				out.print("<td>");
				if (operations[i] == 0) {
					out.print("<input type = \"radio\" disabled = \"disabled\"/>");
				}
				if (operations[i] == 1) {
					out.print("<input type = \"radio\" disabled = \"disabled\" checked = \"yes\"/>");
					out.print("<input type=\"hidden\" name=\"operations" + row
							+ "\" value=\"" + Integer.toString(i) + "\"/>");
				}
				out.print("</td>");
			}
		}

	}

	// Print status
	public void tableStatus(PrintWriter out, Job entry) throws IOException {
		int status = entry.getStatus();

		if (status == 0) {
			out.print("<td>");
			out.print("<p>Uploaded</p>");
			out.print("</td>");
		}
		if (status == 1) {
			String address = entry.getDestination();
			String name = abbreviate(address);
			out.print("<td>");
			/*
			 * out.print("<img src = " +
			 * "\"http://localhost:8080/image-cloud/ViewImage?file=" + address +
			 * "\"" + " alt = \"" + name + "\"" + " width = \"200\" />");
			 */
			out.print("<img src = " + "\"" + address + "\"" + " alt = \""
					+ name + "\"" + " width = \"200\" />");
			out.print("</td>");
		}
		if (status == 2) {
			String address = entry.getDestination();
			String name = abbreviate(address);
			out.print("<td>");
			/*
			 * out.print("<img src = " +
			 * "\"http://localhost:8080/image-cloud/ViewImage?file=" + address +
			 * "\"" + " alt = \"" + name + "\"" + " width = \"200\" />");
			 */
			out.print("<img src = " + "\"" + address + "\"" + " alt = \""
					+ name + "\"" + " width = \"200\" />");
			out.print("</td>");
		}
		if (status == 3) {
			out.print("<td>");
			out.print("<p>Image exceeds allowable file size</p>");
			out.print("</td>");
		}
		if (status == 4) {
			out.print("<td>");
			out.print("<p>Processing error</p>");
			out.print("</td>");
		}
	}

	// Print error message
	public void error(PrintWriter out, int i) throws IOException {
		if (i == 0) {
			out.print("<h3>Error retrieving request. Try Uploading again.</h3>");
		}
	}

	// Print error message with exception message
	public void error(PrintWriter out, int i, Exception e) throws IOException {
		if (i == 0) {
			out.print("<h3>Error retrieving request. Try Uploading again.</h3>");
		}
		out.print("<p>Exception caught:");
		out.print("<br/>");
		e.printStackTrace();
		out.print("</p>");
	}

	// Print closing lines of HTML document
	public void footer(PrintWriter out) throws IOException {
		out.print("</body>");
		out.print("</html>");
	}

}