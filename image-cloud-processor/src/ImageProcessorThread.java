import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageProcessorThread implements Runnable {

	private String imageName = null;
	private int[] operations = null;
	private String comName = null;

	public ImageProcessorThread(String imageName, int[] operations,
			String comName) {
		this.imageName = imageName;
		this.operations = operations;
		this.comName = comName;
	}

	@Override
	public void run() {
		// File name
		String[] splits = this.imageName.split("\\\\");
		String name = splits[splits.length - 1];
		// Read image
		BufferedImage img;
		try {
			img = ImageIO.read(new File(this.imageName));
			Raster imgRaster = img.getData();
			if (operations[0] == 1) {
				HistogramEqualizationThreaded f = new HistogramEqualizationThreaded(
						imgRaster);
				imgRaster = f.process();
			}
			// Save result
			img.setData(imgRaster);
			File out = new File(comName + name);
			int p = name.lastIndexOf(".");
			String ext = name.substring(p + 1);
			ImageIO.write(img, ext, out);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
