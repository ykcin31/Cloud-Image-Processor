import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HistogramEqualizationThreaded 
{
	private Raster imgRaster;
	public HistogramEqualizationThreaded(Raster imgRaster)
	{
		this.imgRaster = imgRaster;
	}
/*	// For testing
	public static void main(String[] args) throws IOException
	{
		// Read image
		String name = "test.jpg";
		String imageName = "C:\\Users\\Nick\\Desktop\\" + name;
		String comName = "C:\\Users\\Nick\\Desktop\\Complete\\";
		BufferedImage img = ImageIO.read(new File(imageName));
		Raster imgRaster = img.getData();
		// Perform operation
		long begin = System.nanoTime();
		HistogramEqualization f = new HistogramEqualization(imgRaster);
		f.process();
		// Save result
		img.setData(imgRaster);
		File out = new File(comName + name);
		int p = name.lastIndexOf(".");
		String ext = name.substring(p+1);
		ImageIO.write(img, ext, out);
		long end = System.nanoTime();
		// Complete		
		System.out.println("Done: " + (end-begin));
	}*/

	public Raster process()
	{
		// Get image dimensions
		int imgBands = imgRaster.getNumBands();
		// Stores result
		WritableRaster result = (WritableRaster) imgRaster;
		// Perform histogram equalization
		ExecutorService pool = Executors.newFixedThreadPool(imgBands);
		List<Callable<Object>> threads = new ArrayList<Callable<Object>>();
		for(int i = 0; i < imgBands; i++)
		{
			threads.add(Executors.callable(new HistogramEqualizationThread(imgRaster, result, i)));
			//System.out.println("Execute:" + i);
		}
		try
		{
			pool.invokeAll(threads);
		}	
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return (Raster) result;
	}
}