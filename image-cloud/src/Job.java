// Class for holding job properties.
// May not use.

public class Job {
	private String address;
	private float size;
	private int[] operations;
	private int status;
	private int parcel;
	private String destination;

	public Job(String address, float size, int[] operations, int status,
			int parcel, String destination) {
		this.address = address;
		this.size = size;
		this.operations = operations;
		this.status = status;
		this.parcel = parcel;
		this.destination = destination;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public float getSize() {
		return this.size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public int[] getOperations() {
		return this.operations;
	}

	public void setOperations(int[] operations) {
		this.operations = operations;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getParcel() {
		return this.parcel;
	}

	public void setParcel(int parcel) {
		this.parcel = parcel;
	}

	public String getDestination() {
		return this.destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public void print() {
		System.out.println(address);
		System.out.println(String.format("%3.2f", size));
		for (int i = 0; i < operations.length; i++) {
			System.out.print(operations[i] + ", ");
		}
		System.out.println(Integer.toString(status));
		System.out.println(Integer.toString(parcel));
		System.out.println(destination);
	}
}
