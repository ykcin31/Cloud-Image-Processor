import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class HistogramEqualization 
{
	private Raster imgRaster;

	// For testing
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
	}

	public HistogramEqualization(Raster imgRaster)
	{
		this.imgRaster = imgRaster;
	}

	public Raster process()
	{
		int min = 255;
		int max = 0;

		// Get image dimensions
		//Raster imgRaster = this.img.getData();
		int imgWidth = imgRaster.getWidth();
		int imgHeight = imgRaster.getHeight();
		int imgBands = imgRaster.getNumBands();
		// System.out.println("Image size: " + imgWidth + " x " + imgHeight + " x " + imgBands);

		// Stores result
		WritableRaster result = (WritableRaster) imgRaster;

		int[] hist = new int[256];
		int[] cdf = new int[256];
		int[] cdfNew = new int[256];

		
		int value = 0;
		for(int b = 0; b < imgBands; b++)
		{
			Arrays.fill(hist, 0);
			Arrays.fill(cdf, 0);
			Arrays.fill(cdfNew, 0);

			
			// Generate histogram
			for(int i = 0; i < imgWidth; i++)
			{
				for(int j = 0; j < imgHeight; j++)
				{
					value = imgRaster.getSample(i,j,b);
					hist[value]++; 
				}
			}
			// Find min (first value with non-zero count)
			for (int n = 0; n < hist.length; n++)
			{
				if (hist[n] > 0)
				{
					min = n;
					break;
				}
			}
			// Find max (last value with non-zero count)
			for (int m = hist.length-1; m > -1; m--)
			{
				if (hist[m] > 0)
				{
					max = m;
					break;
				}
			}
			// Generate CDF
			for(int c = 0; c < 256; c++)
			{
				if(c == 0)
				{
					cdf[c] = hist[c];
				}
				else
				{
					cdf[c] = cdf[c-1] + hist[c];
				}
			}
			// Compute CDF transformation
			for(int c = 0; c < 256; c++)
			{
				cdfNew[c] = (int)((float)(cdf[c] - cdf[min]) / (cdf[max] - cdf[min]) * (256 - 1));
			}
			// Apply CDF transformation
			for(int i = 0; i < imgWidth; i++)
			{
				for(int j = 0; j < imgHeight; j++)
				{
					value = imgRaster.getSample(i, j, b);
					result.setSample(i, j, b, cdfNew[value]);
		        }
			}
		}
		return (Raster) result;
	}
}