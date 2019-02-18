package TWEditor;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class KeyInputStream extends InputStream {
	private RandomAccessFile in;
	private long dataOffset;
	private int residualLength;

	public KeyInputStream(KeyEntry keyEntry) throws DBException, IOException {
		File file = new File(keyEntry.getArchivePath());
		this.in = new RandomAccessFile(file, "r");
		byte[] header = new byte[20];
		int count = this.in.read(header);
		if (count != header.length) {
			throw new DBException("BIF header太小");
		}
		String type = new String(header, 0, 4);
		if (!type.equals("BIFF")) {
			throw new DBException("BIF签名不正确");
		}
		String version = new String(header, 4, 4);
		if (!version.equals("V1.1")) {
			throw new DBException("BIF版本" + version + "不支持");
		}
		int resourceCount = getInteger(header, 8);
		long resourceOffset = getInteger(header, 16);
		byte[] buffer = new byte[20];
		this.in.seek(resourceOffset);
		int keyID = keyEntry.getResourceID();
		for (int i = 0; i < resourceCount; i++) {
			count = this.in.read(buffer);
			if (count != buffer.length) {
				throw new DBException("资源表被截断");
			}
			int resourceID = getInteger(buffer, 0);
			if (resourceID == keyID) {
				int resourceType = getShort(buffer, 16);
				if (resourceType != keyEntry.getResourceType()) {
					throw new DBException("KEY/BIF资源类型不匹配");
				}
				this.dataOffset = getInteger(buffer, 8);
				this.residualLength = getInteger(buffer, 12);
				break;
			}
		}
		if (this.dataOffset == 0L)
			throw new DBException("KEY资源'" + keyEntry.getFileName() + "'未在BIF中发现");
	}

	public void close() throws IOException {
		if (this.in != null) {
			this.in.close();
		}
		this.in = null;
		this.residualLength = 0;
	}

	public int available() {
		return this.residualLength;
	}

	public int read() throws IOException {
		if (this.in == null)
			throw new IOException("输入流未打开");
		int result;
		if (this.residualLength == 0) {
			result = -1;
		} else {
			this.in.seek(this.dataOffset);
			result = this.in.readByte() & 0xFF;
			this.dataOffset += 1L;
			this.residualLength -= 1;
		}
		return result;
	}

	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	public int read(byte[] buffer, int bufferOffset, int bufferLength) throws IOException {
		if (this.in == null) {
			throw new IOException("输入流未打开");
		}
		int count = 0;
		if (this.residualLength == 0) {
			count = -1;
		} else {
			this.in.seek(this.dataOffset);
			int length = Math.min(this.residualLength, bufferLength);
			count = this.in.read(buffer, bufferOffset, length);
			if (count < 0) {
				throw new EOFException("流处理意外结束");
			}
			this.dataOffset += count;
			this.residualLength -= count;
		}
		return count;
	}

	protected void finalize() {
		try {
			close();
			super.finalize();
		} catch (Throwable exc) {
			Main.logException("最终确定输入流时出现异常", exc);
		}
	}

	private int getShort(byte[] buffer, int offset) {
		int value = buffer[(offset + 0)] & 0xFF | (buffer[(offset + 1)] & 0xFF) << 8;
		if (value >= 32768) {
			value |= -65536;
		}
		return value;
	}

	private int getInteger(byte[] buffer, int offset) {
		return buffer[(offset + 0)] & 0xFF | (buffer[(offset + 1)] & 0xFF) << 8 | (buffer[(offset + 2)] & 0xFF) << 16
				| (buffer[(offset + 3)] & 0xFF) << 24;
	}
}