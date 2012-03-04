import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.commons.io.FileUtils;

@WebServlet("/ProcessServlet")
public class ProcessServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	// Maximum individual image size [mb]
	final float MAXIMAGE = 3;
	// CAP on total memory of each parcel [mb]
	final float CAP = 4;
	// Buffer size for zip compression
	final int BUFFER = 2048;
	// Delimiter in instructions.txt
	final String DELIMITER = " <~> ";

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		ArrayList<Job> listing = new ArrayList<Job>();
		ArrayList<Parcel> parcels = new ArrayList<Parcel>();
		String directory;
		ArrayList<String> packages = new ArrayList<String>();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		HtmlPrinter.header(out);
		//---------------------------------------------------------------------------------------------------
		// Retrieve request data 
		try
		{
			directory = retrieveDirectory(request);
			listing = retrieveList(request);
			parcels = createParcels(listing);
			packages = createPackages(parcels, directory);
			// Submit packages for processing
			Distributor d = new Distributor();
			d.submit(packages);
			HtmlPrinter.processPage(out, listing, directory);
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

	private String retrieveDirectory(HttpServletRequest request) 
	{
		String directory = request.getParameter("directory");
		return directory;
	}

	private ArrayList<Job> retrieveList(HttpServletRequest request)
	{
		// Number of image files
		int count =  Integer.parseInt(request.getParameter("count"));
		// Number of image processing options
		int opCount =  Integer.parseInt(request.getParameter("opCount"));
		ArrayList<Job> listing = new ArrayList<Job>();

		// Extract name and command data
		for (int i = 0; i < count; i++)
		{
			// File name
			String address =  request.getParameter("address" + Integer.toString(i));
			// File size
			float size = new Float(request.getParameter("size" + Integer.toString(i)));
			// Operations
			String[] opts =  request.getParameterValues("operations" + Integer.toString(i));
			int[] operations = new int[opCount];
			if(opts!=null)
			{
				for(int j=0; j<opts.length; j++)
				{
					operations[Integer.parseInt(opts[j])] = 1;
				}
			}
			// Status
			int status =  Integer.parseInt(request.getParameter("status" + Integer.toString(i)));
			// Parcel
			int parcel =  Integer.parseInt(request.getParameter("parcel" + Integer.toString(i)));
			// Destination 
			String destination = request.getParameter("destination" + Integer.toString(i));
			listing.add(new Job(address, size, operations, status, parcel, destination));
		}
		return listing;
	}

	private ArrayList<Parcel> createParcels(ArrayList<Job> listing)
	{
		int count = listing.size();
		ArrayList<Parcel> parcels = new ArrayList<Parcel>();
		Parcel doNothing = new Parcel();
		// Identify images that don't need processing
		for(int i = 0; i < count; i++)
		{
			Job entry = listing.get(i);
			int[] operations = entry.getOperations();
			int tally = 0;
			for(int j=0; j < operations.length; j++)
			{
				tally = tally + operations[j];
			}
			if(tally==0)
			{
				// Set status to 2 (Complete)
				entry.setStatus(2);
				entry.setDestination(entry.getAddress());
				// Add to "Do Nothing" parcel
				doNothing.addJob(entry);
				entry.setParcel(0);
			}
		}
		parcels.add(doNothing);

		// Submit other images to parcels for processing
		Parcel p = new Parcel();
		float mem = 0;
		for(int i = 0; i < count; i++)
		{
			Job entry = listing.get(i);
			int status = entry.getStatus();
			float size = entry.getSize();
			if(status == 0)
			{	
				if(size > MAXIMAGE)
				{
					entry.setStatus(3);
				}
				else
				{
					// Check to see if there is room for more jobs in the current parcel
					if((mem + size) > CAP)
					{	
						// Add parcel to the parcels arraylist
						parcels.add(p);
						// Reset temporary parcel and memory counter
						p = new Parcel();
						mem = 0;
					}
					entry.setStatus(1);
					entry.setParcel(parcels.size()-1);
					p.addJob(entry);
					mem = mem + size; 
				}
			}
		}
		if(p.size()>0)
		{
			parcels.add(p);
		}
		return parcels;
	}

	// Create .zip files for each parcel containing image files and an instruction .txt file
	private ArrayList<String> createPackages(ArrayList<Parcel> parcels, String directory) throws Exception
	{
		ArrayList<String> packages = new ArrayList<String>();
		for(int i = 1; i < parcels.size(); i++)
		{
			Parcel p = parcels.get(i);
			// Write instructions.txt
			String txtDir = new String(directory+"instructions.txt");
			writeInstructionTxt(p, txtDir);
			// Create .zip file
			ArrayList<String> files = new ArrayList<String>();
			// Add parcel's jobs' image file names to files list
			for(int j = 0; j < p.size(); j++)
			{
				Job entry = p.getJob(j);
				files.add(entry.getAddress());
			}
			// Add instructions.txt to file list
			files.add(txtDir);
			// Create .zip file
			String zipDir = new String(directory+"package"+i+".zip");
			Zipper z = new Zipper();
			z.compress(files, zipDir);
			packages.add(zipDir);
			FileUtils.forceDelete(new File(txtDir));
		}
		return packages;
	}
	
	// Writes and saves an instruction.txt file
	private void writeInstructionTxt(Parcel p, String txtDir) throws IOException
	{
		File txt = new File(txtDir);
		BufferedWriter output = new BufferedWriter(new FileWriter(txt));
		for(int j = 0; j < p.size(); j++)
		{
			Job entry = p.getJob(j);
			String address = entry.getAddress();
			String[] splits = address.split("\\\\");
			String name = splits[splits.length-1];
			output.write(name);
			int[] operations = entry.getOperations();
			for(int k = 0; k < operations.length; k++)
			{
				output.write(DELIMITER);
				output.write(Integer.toString(operations[k]));
			}
			output.newLine();
		}
		output.close();
	}
}