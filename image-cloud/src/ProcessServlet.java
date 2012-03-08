import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.apache.commons.io.FileUtils;

@WebServlet("/ProcessServlet")
public class ProcessServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// SETUP fields
	// -----------------------------------------------------------------------------------------------------------------
	// Enter maximum individual image size [mb]
	private final float MAXIMAGE = 20;
	// Enter maximum memory capacity each parcel [mb]
	private final float CAP = 30;
	// Enter port number
	private final int PORT = 5555;
	// Enter number of threads
	private final int NTHREADS = 5;
	// Enter setting for printing to console
	private final boolean PRINT = false;
	// -----------------------------------------------------------------------------------------------------------------
	// Buffer size for zip compression
	final int BUFFER = 2048;
	// Enter delimiter in instructions.txt
	final String DELIMITER = " <~> ";

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ArrayList<Job> listing = new ArrayList<Job>();
		ArrayList<Parcel> parcels = new ArrayList<Parcel>();
		String directory;
		ArrayList<String> packages = new ArrayList<String>();
		String completeDir = null;
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String dl = null;
		try {
			// Create packages to ship out to processor clients
			directory = retrieveDirectory(request);
			listing = retrieveList(request);
			parcels = createParcels(listing);
			if (parcels.size() > 0) {
				packages = createPackages(parcels, directory);
				printToConsole("Packages created: " + packages.toString());
				completeDir = createCompleteFolder(directory);
				printToConsole("ProcessServlet - Complete directory created: "
						+ completeDir);
				ServerSocket serverSocket = new ServerSocket(PORT);
				// Submit packages for processing
				ExecutorService pool = Executors
						.newFixedThreadPool(this.NTHREADS);
				ArrayList<Callable<Object>> threads = new ArrayList<Callable<Object>>();
				for (int i = 0; i < packages.size(); i++) {
					// Create a thread for each package
					threads.add(Executors.callable(new ServerThread(packages
							.get(i), completeDir, serverSocket)));
				}
				pool.invokeAll(threads);
				pool.shutdown();
				serverSocket.close();
				for (int i = 0; i < packages.size(); i++) {
					// Processing complete, extract files, and update "status"
					// and "destination" fields of Jobs
					// Extract .zip folder containing processed images
					Zipper z = new Zipper();
					String name = packages.get(i);
					String[] split = name.split("\\\\");
					name = split[split.length - 1];
					String returnZip = completeDir + name;
					// Check if file exists
					File check = new File(returnZip);
					if (check.exists()) {
						try {
							String destinationDir = z.extract(returnZip,
									completeDir);
							updateParcel(parcels.get(i), destinationDir);
						} catch (Exception e) {
							// Error in .zip extraction
							updateParcel(parcels.get(i), null);
							e.printStackTrace();
						}
					} else {
						// Error in .zip processing
						updateParcel(parcels.get(i), null);
					}
				}
				printToConsole(completeDir);
				dl = zipCompleteFolder(completeDir);
			}
			HtmlPrinter.header(out);
			HtmlPrinter.processPage(out, listing, directory, dl);
			HtmlPrinter.footer(out);
			out.close();
		} catch (Exception e) {
			HtmlPrinter.error(out, 0, e);
			HtmlPrinter.footer(out);
			out.close();
			e.printStackTrace();
		}
	}

	private String zipCompleteFolder(String dir) throws Exception {
		File folders = new File(dir);
		ArrayList<String> folderNames = new ArrayList<String>(
				Arrays.asList(folders.list()));
		ArrayList<String> fileNames = new ArrayList<String>();
		for (int i = 0; i < folderNames.size(); i++) {
			String currentDir = dir + folderNames.get(i) + "\\";
			printToConsole(currentDir);
			File d = new File(currentDir);
			ArrayList<String> files = new ArrayList<String>(Arrays.asList(d
					.list()));
			for (int j = 0; j < files.size(); j++) {
				fileNames.add(currentDir + files.get(j));
			}
		}
		String comDir = dir + "complete.zip";
		Zipper z = new Zipper();
		z.compress(fileNames, comDir);
		return comDir;
	}

	// Retrieves directory name from Form data
	private String retrieveDirectory(HttpServletRequest request) {
		String directory = request.getParameter("directory");
		return directory;
	}

	// Creates a collection of Jobs for the image files in the directory
	private ArrayList<Job> retrieveList(HttpServletRequest request) {
		// Number of image files
		int count = Integer.parseInt(request.getParameter("count"));
		// Number of image processing options
		int opCount = Integer.parseInt(request.getParameter("opCount"));
		ArrayList<Job> listing = new ArrayList<Job>();

		// Extract name and command data
		for (int i = 0; i < count; i++) {
			// File name
			String address = request.getParameter("address"
					+ Integer.toString(i));
			// File size
			float size = new Float(request.getParameter("size"
					+ Integer.toString(i)));
			// Operations
			String[] opts = request.getParameterValues("operations"
					+ Integer.toString(i));
			int[] operations = new int[opCount];
			if (opts != null) {
				for (int j = 0; j < opts.length; j++) {
					operations[Integer.parseInt(opts[j])] = 1;
				}
			}
			// Status
			int status = Integer.parseInt(request.getParameter("status"
					+ Integer.toString(i)));
			// Parcel
			int parcel = Integer.parseInt(request.getParameter("parcel"
					+ Integer.toString(i)));
			// Destination
			String destination = request.getParameter("destination"
					+ Integer.toString(i));
			listing.add(new Job(address, size, operations, status, parcel,
					destination));
		}
		return listing;
	}

	// Update the fields of the Jobs contained by the Parcel
	private void updateParcel(Parcel p, String destinationDir) {
		for (int i = 0; i < p.size(); i++) {
			Job j = p.getJob(i);
			if (destinationDir != null) {
				String address = j.getAddress();
				String[] splits = address.split("\\\\");
				String name = splits[splits.length - 1];
				String destination = destinationDir + name;
				j.setDestination(destination);
				j.setStatus(2);
			} else {
				j.setStatus(4);
			}
		}
	}

	// Checks which Jobs need to be processed and groups them into parcels
	private ArrayList<Parcel> createParcels(ArrayList<Job> listing) {
		int count = listing.size();
		ArrayList<Parcel> parcels = new ArrayList<Parcel>();
		// Identify images that are too large
		for (int i = 0; i < count; i++) {
			Job entry = listing.get(i);
			float size = entry.getSize();
			if (size > MAXIMAGE) {
				entry.setStatus(3);
			}
		}
		// Identify images that don't need processing
		for (int i = 0; i < count; i++) {
			Job entry = listing.get(i);
			if (entry.getStatus() == 0) {
				int[] operations = entry.getOperations();
				int tally = 0;
				for (int j = 0; j < operations.length; j++) {
					tally = tally + operations[j];
				}
				if (tally == 0) {
					// Set status to 1 (Did nothing)
					entry.setStatus(1);
					entry.setDestination(entry.getAddress());
				}
			}
		}

		// Submit other images to parcels for processing
		Parcel p = new Parcel();
		float mem = 0;
		for (int i = 0; i < count; i++) {
			Job entry = listing.get(i);
			int status = entry.getStatus();
			float size = entry.getSize();
			if (status == 0 || status == 4) {
				// Check to see if there is room for more jobs in the current
				// parcel
				if ((mem + size) > CAP) {
					// Add parcel to the parcels arraylist
					parcels.add(p);
					// Reset temporary parcel and memory counter
					p = new Parcel();
					mem = 0;
				}
				entry.setStatus(1);
				entry.setParcel(parcels.size() - 1);
				p.addJob(entry);
				mem = mem + size;
			}
		}
		if (p.size() > 0) {
			parcels.add(p);
		}
		return parcels;
	}

	// Create .zip files for each parcel containing image files and an
	// instruction .txt file
	private ArrayList<String> createPackages(ArrayList<Parcel> parcels,
			String directory) throws Exception {
		ArrayList<String> packages = new ArrayList<String>();
		for (int i = 0; i < parcels.size(); i++) {
			Parcel p = parcels.get(i);
			// Write instructions.txt
			String txtDir = new String(directory + "instructions.txt");
			writeInstructionTxt(p, txtDir);
			// Create .zip file
			ArrayList<String> files = new ArrayList<String>();
			// Add parcel's jobs' image file names to files list
			for (int j = 0; j < p.size(); j++) {
				Job entry = p.getJob(j);
				files.add(entry.getAddress());
			}
			// Add instructions.txt to file list
			files.add(txtDir);
			// Create .zip file
			String zipDir = new String(directory + "package" + i + ".zip");
			Zipper z = new Zipper();
			z.compress(files, zipDir);
			packages.add(zipDir);
			FileUtils.forceDelete(new File(txtDir));
		}
		return packages;
	}

	// Writes and saves an instruction.txt file
	private void writeInstructionTxt(Parcel p, String txtDir)
			throws IOException {
		File txt = new File(txtDir);
		BufferedWriter output = new BufferedWriter(new FileWriter(txt));
		for (int j = 0; j < p.size(); j++) {
			Job entry = p.getJob(j);
			String address = entry.getAddress();
			String[] splits = address.split("\\\\");
			String name = splits[splits.length - 1];
			output.write(name);
			int[] operations = entry.getOperations();
			for (int k = 0; k < operations.length; k++) {
				output.write(DELIMITER);
				output.write(Integer.toString(operations[k]));
			}
			output.newLine();
		}
		output.close();
	}

	// Creates a new directory for storing processed images
	private String createCompleteFolder(String directory) {
		String dirName = directory + "complete\\";
		File complete = new File(dirName);
		complete.mkdir();
		return dirName;
	}

	// Print to System console
	private void printToConsole(String s) {
		if (this.PRINT == true) {
			System.out.println(s);
		}
	}
}