import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;

public class Distributor 
{
	private int status = 0;

	public Distributor()
	{
		this.status=0;
	}

	public void submit(ArrayList<String> packages) throws InterruptedException, IOException
	{
		final String FILEPATH = "C:\\Users\\Nick\\workspace\\imagecloud\\image-cloud\\data\\clients\\";
		// Maximum number of simultaneous threads
		final int THREADS = 5;
		// Create executor pool
		ExecutorService pool = Executors.newFixedThreadPool(THREADS);
		List<Callable<Object>> threads = new ArrayList<Callable<Object>>();
		
		
		for(int i = 0; i < packages.size(); i++)
		{
			ProcessorThread p = new ProcessorThread(packages.get(i),FILEPATH);
			threads.add(Executors.callable(p));
		}
		pool.invokeAll(threads);
		status = 1;
	}

	public int getStatus()
	{
		return status;
	}
}