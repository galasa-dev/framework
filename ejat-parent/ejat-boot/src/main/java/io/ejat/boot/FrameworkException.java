package io.ejat.boot;

/**
 * @see java.lang.Exception
 */
public class FrameworkException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @see java.lang.Exception#Exception()
	 */
	public FrameworkException() {
		super();
	}
	
	/**
	 * @see java.lang.Exception#Exception(String)
	 */
	public FrameworkException(String message) {
		super(message);
	}

	/**
	 * @see java.lang.Exception#Exception(Throwable)
	 */
	public FrameworkException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @see java.lang.Exception#Exception(String, Throwable)
	 */
	public FrameworkException(String message, Throwable cause) {
		super(message, cause);
	}

}
