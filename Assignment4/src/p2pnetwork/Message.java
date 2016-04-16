package p2pnetwork;

public class Message {
	
	private ShallowNode sourceNode;
	
	private int hopCount;
	
	private String text;
	
	private String targetName;
	
	private ShallowNode targetNode;
	
	public Message(ShallowNode sourceNode, int hopCount, String text, String targetName) {
		this.sourceNode = sourceNode;
		this.hopCount = hopCount;
		this.text = text;
		this.targetName = targetName;
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

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public ShallowNode getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(ShallowNode targetNode) {
		this.targetNode = targetNode;
	}
}