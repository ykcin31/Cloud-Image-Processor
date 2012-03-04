import java.util.*;

public class Parcel 
{
	private ArrayList<Job> listing;
	
	public Parcel()
	{
		this.listing = new ArrayList<Job>();
	}
	
	public Job getJob(int i)
	{
		return listing.get(i);
	}
	
	public void addJob(Job entry)
	{
		listing.add(entry);
	}
	
	public void clearAll()
	{
		listing.clear();
	}
	
	public int size()
	{
		return listing.size();
	}
}
