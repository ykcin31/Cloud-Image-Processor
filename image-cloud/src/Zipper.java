import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.*;
import org.apache.commons.io.FileUtils;


// The Zipper class contains methods for extracting and compressing .zip files.
public class Zipper 
{
	final int BUFFER = 2048;

	public Zipper()
	{
	}

	// Extracts contents of .zip file called "fileName" to a folder in "filePath"
	public String extract(String fileName, String filepath) throws IOException
	{
		// Check for .zip file
		int mid = fileName.lastIndexOf(".");
		String ext = fileName.substring(mid+1,fileName.length());
		String dirName = null;
		if(ext.equalsIgnoreCase("zip"))
		{
			// Create corresponding directory for extraction
			String[] splitSlash = fileName.split("\\\\");
			String end = splitSlash[(splitSlash.length)-1];
			int dot = end.lastIndexOf(".");
			String name = end.substring(0,dot);
			dirName = filepath + name + "\\";
//			System.out.println(dirName);

			File directory = new File(dirName);
			if(directory.exists())
			{
				FileUtils.deleteDirectory(directory);
			}
			if((new File(dirName)).mkdirs())
			{
//				System.out.println("Created folder: " + dirName);
				FileInputStream fis = new FileInputStream(fileName);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
				ZipEntry entry;
				while((entry = zis.getNextEntry()) != null)
				{
					int count;
					byte data[] = new byte[BUFFER];
					// write the files to the disk
					FileOutputStream fos = new FileOutputStream(dirName + entry.getName());
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1) 
					{
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
				}
				zis.close();
				// Delete .zip file
				FileUtils.forceDelete(new File(fileName)); 
			}
		}
		return dirName;
	}
	
	// Compresses files listed in "files" into a .zip file called "name"
	public void compress(ArrayList<String> files, String zipName) throws Exception
	{
		byte data[] = new byte[BUFFER];
		// File buffer streams
		BufferedInputStream origin = null;
		String zipDir = zipName;
		FileOutputStream zipDest = new FileOutputStream(zipDir);
		ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(zipDest));  
		// Set compression method
		zipOut.setMethod(ZipOutputStream.DEFLATED);
		// Iterate compressing contained files	
		for (int i = 0; i < files.size(); i++) 
		{
			String address = files.get(i);
			FileInputStream fj = new FileInputStream(address);
			origin = new BufferedInputStream(fj, BUFFER);
			String[] splits = address.split("\\\\");
			String name = splits[splits.length-1];
			ZipEntry entry = new ZipEntry(name);
			zipOut.putNextEntry(entry);
			int count;
			while((count = origin.read(data, 0, BUFFER)) != -1) 
			{
				zipOut.write(data, 0, count);
			}
			origin.close();
		}
		zipOut.close();	
	}
}