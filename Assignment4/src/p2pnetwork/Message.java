package p2pnetwork;

public class Message {
	
	private ShallowNode sourceNode;
	
	private int hopCount;
	
	private String text;
	
	private String recName;
	
	public Message(ShallowNode sourceNode, int hopCount, String text, String recName) {
		this.sourceNode = sourceNode;
		this.hopCount = hopCount;
		this.text = text;
		this.recName = recName;
	}
	
	public void decHopCount() {
		hopCount--;
	}

	public ShallowNode getSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(ShallowNode sourceNode) {
		this.sourceNode = sourceNode;
	}

	public int getHopCount() {
		return hopCount;
	}

	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getRecName() {
		return recName;
	}

	public void setRecName(String recName) {
		this.recName = recName;
	}
}