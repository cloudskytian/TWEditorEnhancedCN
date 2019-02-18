package TWEditor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceInputStream extends InputStream {
	private FileInputStream in;
	private int residualLength;

	public ResourceInputStream(ResourceEntry entry) throws IOException {
		this.residualLength = entry.getLength();
		this.in = new FileInputStream(entry.getFile());
		this.in.skip(entry.getOffset());
	}

	public void close() throws IOException {
		if (this.in != null) {
			this.in.close();
			this.in = null;
		}
		this.residualLength = 0;
	}

	public int available() {
		return this.residualLength;
	}

	public int read() throws IOException {
		if (this.in == null)
			throw new IOException("输入流已关闭");
		int b;
		if (this.residualLength > 0) {
			b = this.in.read();
			if (b != -1)
				this.residualLength -= 1;
		} else {
			b = -1;
		}
		return b;
	}

	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	public int read(byte[] buffer, int bufferOffset, int bufferLength) throws IOException {
		if (this.in == null)
			throw new IOException("输入流已关闭");
		int count;
		if (this.residualLength > 0) {
			count = this.in.read(buffer, bufferOffset, Math.min(bufferLength, this.residualLength));
			if (count != -1)
				this.residualLength -= count;
		} else {
			count = -1;
		}
		return count;
	}

	public long skip(long count) throws IOException {
		if (this.in == null) {
			throw new IOException("输入流已关闭");
		}
		long skipped = this.in.skip(Math.min(count, this.residualLength));
		this.residualLength = ((int) (this.residualLength - skipped));
		return skipped;
	}

	protected void finalize() {
		try {
			close();
			super.finalize();
		} catch (Throwable exc) {
			Main.logException("最终确定输入流时出现异常", exc);
		}
	}
}