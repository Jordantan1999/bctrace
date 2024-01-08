package bctrace.runtime.io.shiftleft.bctrace.runtime;


public class BctraceRuntimeException extends RuntimeException {

  /**
	 * 
	 */
	private static final long serialVersionUID = -2964344017342446995L;
private RuntimeException rex;

  public BctraceRuntimeException(RuntimeException rex) {
    super(rex);
    this.rex = rex;
  }

  public RuntimeException getWrappedException() {
    return rex;
  }
}
