import java.awt.image.BufferedImage;
import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.imageio.*;


@WebServlet("/ViewImage")
public class ViewImage extends HttpServlet 
{
	
	private static final long serialVersionUID = 1L;

	// Image handler for viewing images from server
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		// Get image name
		String fileName = request.getParameter("file");
		// Check image type
		int mid = fileName.lastIndexOf(".");
		//String name = fileName.substring(0,mid);
		String ext = fileName.substring(mid+1,fileName.length());
		if(ext.equalsIgnoreCase("jpg")||ext.equalsIgnoreCase("jpeg")||ext.equalsIgnoreCase("jpe") )
		{
			BufferedImage img = null;
			try
			{
				img = ImageIO.read(new File(fileName));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", baos);
				baos.flush();
				byte[] imageByte = baos.toByteArray();
				baos.close();
				response.setContentType("image/jpeg");
				ServletOutputStream out = response.getOutputStream();
				out.write(imageByte);
			}
			catch (IOException e)
			{
			}
		}
		else if(ext.equalsIgnoreCase("gif"))
		{
			BufferedImage img = null;
			try
			{
				img = ImageIO.read(new File(fileName));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "gif", baos);
				baos.flush();
				byte[] imageByte = baos.toByteArray();
				baos.close();
				response.setContentType("image/gif");
				ServletOutputStream out = response.getOutputStream();
				out.write(imageByte);
			}
			catch (IOException e)
			{
			}
		}
		else if(ext.equalsIgnoreCase("png"))
		{
			BufferedImage img = null;
			try
			{
				img = ImageIO.read(new File(fileName));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "png", baos);
				baos.flush();
				byte[] imageByte = baos.toByteArray();
				baos.close();
				response.setContentType("image/x-png");
				ServletOutputStream out = response.getOutputStream();
				out.write(imageByte);
			}
			catch (IOException e)
			{
			}
		}
		else
		{
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("Can not show image type.");
		}
		
	}
}