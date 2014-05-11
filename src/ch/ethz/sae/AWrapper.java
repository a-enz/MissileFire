package ch.ethz.sae;
import apron.*;

public class AWrapper {

	Abstract1 elem;
	Manager man;
	Object statement;

	//changed constructor: now initializes man too
	public AWrapper(Abstract1 e, Manager m) {
		elem = e;
		man = m;
	}

	public Abstract1 get() {
		return elem;
	}

	public void set(Abstract1 e) {
		elem = e;
	}

	public Object getStatement() {
		return statement;
	}

	public void setStatement(Object statement) {
		this.statement = statement;
	}

	public void copy(AWrapper src) {
		this.elem = src.get();
		this.statement = src.statement;
	}

	public boolean equals(Object o) {
		Abstract1 t = ((AWrapper) o).get();
		try {
			if (elem.isEqual(man, t) != elem.isIncluded(man, t))
				;
			
			return elem.isIncluded(man, t);
		} catch (ApronException e) {
			System.err.println("isEqual failed");
			System.exit(-1);
		}
		return false;
	}

	public String toString() {
		try {
			if (elem.isTop(man))
				return "<Top>";

			return elem.toString();
		} catch (ApronException e) {
			System.err.println("toString failed");
			System.exit(-1);
		}
		return null;
	}
}
