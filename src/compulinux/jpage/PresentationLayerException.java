package compulinux.jpage;

public class PresentationLayerException extends RuntimeException {
	static final long serialVersionUID = 100;

	public PresentationLayerException() {
		super();
	}

	public PresentationLayerException(Throwable t) {
		super(t);
	}

	public PresentationLayerException(String msg) {
		super(msg);
	}
}
