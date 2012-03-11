import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class HistogramEqualizationThread implements Runnable
{
	private Raster imgRaster;
	private WritableRaster result;
	private int band;

	public HistogramEqualizationThread(Raster imgRaster, WritableRaster result, int band)
	{
		this.imgRaster = imgRaster;
		this.result = result;
		this.band = band;
	}

	public void run()
	{		
		int imgWidth = this.imgRaster.getWidth();
		int imgHeight = this.imgRaster.getHeight();
		int[] hist = new int[256];
		int[] cdf = new int[256];
		int[] cdfNew = new int[256];
		Arrays.fill(hist, 0);
		Arrays.fill(cdf, 0);
		Arrays.fill(cdfNew, 0);
		int value = 0;
		int max = 255;
		int min = 0;
		// Generate histogram
		for(int i = 0; i < imgWidth; i++)
		{
			for(int j = 0; j < imgHeight; j++)
			{
				value = imgRaster.getSample(i,j,this.band);
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
				value = imgRaster.getSample(i, j, this.band);
				result.setSample(i, j, this.band, cdfNew[value]);
			}
		}
	}
}
