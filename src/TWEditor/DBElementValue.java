package TWEditor;

public abstract class DBElementValue implements Cloneable {
	public Object clone() {
		Object clonedObject;
		try {
			clonedObject = super.clone();
		} catch (CloneNotSupportedException exc) {
			throw new UnsupportedOperationException("无法克隆数据库元素值", exc);
		}
		return clonedObject;
	}
}