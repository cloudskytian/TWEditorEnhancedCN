package TWEditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Database {
	private File file;
	private String name;
	private String fileType;
	private String fileVersion;
	private DBElement topLevelStruct;
	private byte[] structBuffer;
	private int structArraySize;
	private int structArrayCount;
	private byte[] fieldBuffer;
	private int fieldArraySize;
	private int fieldArrayCount;
	private byte[] labelBuffer;
	private int labelArraySize;
	private int labelArrayCount;
	private byte[] fieldDataBuffer;
	private int fieldDataSize;
	private int fieldDataLength;
	private byte[] fieldIndicesBuffer;
	private int fieldIndicesSize;
	private int fieldIndicesLength;
	private byte[] listIndicesBuffer;
	private int listIndicesSize;
	private int listIndicesLength;

	public Database() {
		this.name = new String();
	}

	public Database(String filePath) {
		this(new File(filePath));
	}

	public Database(File file) {
		this.file = file;
		this.name = file.getName();
	}

	public File getFile() {
		return this.file;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return this.fileType;
	}

	public void setType(String type) {
		if (type.length() != 4) {
			throw new IllegalArgumentException("文件类型不是4字符");
		}
		this.fileType = type;
	}

	public String getVersion() {
		return this.fileVersion;
	}

	public void setVersion(String version) {
		if ((!version.equals("V3.2")) && (!version.equals("V3.3"))) {
			throw new IllegalArgumentException("文件版本" + version + " 不支持");
		}
		this.fileVersion = version;
	}

	public DBElement getTopLevelStruct() {
		return this.topLevelStruct;
	}

	public void setTopLevelStruct(DBElement struct) {
		if (struct.getType() != 14) {
			throw new IllegalArgumentException("数据库元素结构错误");
		}
		this.topLevelStruct = struct;
	}

	public void load() throws DBException, IOException {
		if (this.file == null) {
			throw new IllegalStateException("没有可用的数据库文件");
		}
		FileInputStream in = new FileInputStream(this.file);
		try {
			load(in);
		} finally {
			in.close();
		}
	}

	public void load(InputStream in) throws DBException, IOException {
		try {
			byte[] headerBuffer = new byte[56];
			int count = in.read(headerBuffer);
			if (count != 56) {
				throw new DBException(this.name + ": GFF header太短");
			}
			this.fileType = new String(headerBuffer, 0, 4);
			this.fileVersion = new String(headerBuffer, 4, 4);
			if ((!this.fileVersion.equals("V3.2")) && (!this.fileVersion.equals("V3.3"))) {
				throw new DBException(this.name + ": GFF版本 " + this.fileVersion + "不支持");
			}
			this.structArrayCount = getInteger(headerBuffer, 12);
			this.structArraySize = this.structArrayCount;
			this.fieldArrayCount = getInteger(headerBuffer, 20);
			this.fieldArraySize = this.fieldArrayCount;
			this.labelArrayCount = getInteger(headerBuffer, 28);
			this.labelArraySize = this.labelArrayCount;
			this.fieldDataLength = getInteger(headerBuffer, 36);
			this.fieldDataSize = this.fieldDataLength;
			this.fieldIndicesLength = getInteger(headerBuffer, 44);
			this.fieldIndicesSize = this.fieldIndicesLength;
			this.listIndicesLength = getInteger(headerBuffer, 52);
			this.listIndicesSize = this.listIndicesLength;
			if (this.structArrayCount < 1) {
				throw new DBException(this.name + ": GFF文件不包含任何结构");
			}
			int size = 12 * this.structArraySize;
			this.structBuffer = new byte[size];
			count = in.read(this.structBuffer);
			if (count != size) {
				throw new DBException(this.name + ": 结构数组数据被截断");
			}
			if (this.fieldArrayCount > 0) {
				size = 12 * this.fieldArraySize;
				this.fieldBuffer = new byte[size];
				count = in.read(this.fieldBuffer);
				if (count != size) {
					throw new DBException(this.name + ": 字段数组数据被截断");
				}
			}
			if (this.labelArrayCount > 0) {
				size = 16 * this.labelArraySize;
				this.labelBuffer = new byte[size];
				count = in.read(this.labelBuffer);
				if (count != size) {
					throw new DBException(this.name + ": 标签数组数据被截断");
				}
			}
			if (this.fieldDataLength > 0) {
				this.fieldDataBuffer = new byte[this.fieldDataSize];
				count = in.read(this.fieldDataBuffer);
				if (count != this.fieldDataSize) {
					throw new DBException(this.name + ": 字段数据被截断");
				}
			}
			if (this.fieldIndicesLength > 0) {
				this.fieldIndicesBuffer = new byte[this.fieldIndicesSize];
				count = in.read(this.fieldIndicesBuffer);
				if (count != this.fieldIndicesSize) {
					throw new DBException(this.name + ": 字段索引被截断");
				}
			}
			if (this.listIndicesLength > 0) {
				this.listIndicesBuffer = new byte[this.listIndicesSize];
				count = in.read(this.listIndicesBuffer);
				if (count != this.listIndicesSize) {
					throw new DBException(this.name + ": 列表索引被截断");
				}
			}
			this.topLevelStruct = decodeStruct(new String(), 0);
		} finally {
			this.structBuffer = null;
			this.fieldBuffer = null;
			this.labelBuffer = null;
			this.fieldDataBuffer = null;
			this.fieldIndicesBuffer = null;
			this.listIndicesBuffer = null;
		}
	}

	private DBElement decodeField(int index) throws DBException {
		if (index >= this.fieldArrayCount) {
			throw new DBException(this.name + ": 字段索引" + index + "超过数组大小");
		}
		int offset = 12 * index;
		int fieldType = getInteger(this.fieldBuffer, offset);
		int labelIndex = getInteger(this.fieldBuffer, offset + 4);
		int dataOffset = getInteger(this.fieldBuffer, offset + 8);
		if (labelIndex >= this.labelArrayCount) {
			throw new DBException(this.name + ": 标签索引" + labelIndex + "超过数组大小");
		}
		int labelOffset = 16 * labelIndex;
		int labelLength;
		for (labelLength = 16; (labelLength > 0)
				&& (this.labelBuffer[(labelOffset + labelLength - 1)] == 0); labelLength--)
			;
		String label = new String(this.labelBuffer, labelOffset, labelLength);
		DBElement element;
		switch (fieldType) {
		case 15:
			element = decodeList(label, dataOffset);
			break;
		case 14:
			element = decodeStruct(label, dataOffset);
			break;
		case 0:
			element = new DBElement(fieldType, 0, label, new Integer(dataOffset & 0xFF));
			break;
		case 1:
			element = new DBElement(fieldType, 0, label, new Character((char) dataOffset));
			break;
		case 2:
			element = new DBElement(fieldType, 0, label, new Integer(dataOffset & 0xFFFF));
			break;
		case 3:
			dataOffset &= 65535;
			if (dataOffset > 32767)
				dataOffset |= -65536;
			element = new DBElement(fieldType, 0, label, new Integer(dataOffset));
			break;
		case 4:
			element = new DBElement(fieldType, 0, label, new Long(dataOffset & 0xFFFFFFFF));
			break;
		case 5:
			element = new DBElement(fieldType, 0, label, new Integer(dataOffset));
			break;
		case 6:
		case 7:
			if (dataOffset + 8 > this.fieldDataLength) {
				throw new DBException(this.name + ": 字段数据偏移 " + dataOffset + "超过字段数据");
			}
			long longValue = this.fieldDataBuffer[(dataOffset + 0)] & 0xFF
					| (this.fieldDataBuffer[(dataOffset + 1)] & 0xFF) << 8
					| (this.fieldDataBuffer[(dataOffset + 2)] & 0xFF) << 16
					| (this.fieldDataBuffer[(dataOffset + 3)] & 0xFF) << 24
					| (this.fieldDataBuffer[(dataOffset + 4)] & 0xFF) << 32
					| (this.fieldDataBuffer[(dataOffset + 5)] & 0xFF) << 40
					| (this.fieldDataBuffer[(dataOffset + 6)] & 0xFF) << 48
					| (this.fieldDataBuffer[(dataOffset + 7)] & 0xFF) << 56;
			if ((fieldType == 6) && (longValue < 0L)) {
				throw new DBException("DWORD64值对Java来说太大了");
			}
			element = new DBElement(fieldType, 0, label, new Long(longValue));
			break;
		case 8:
			element = new DBElement(fieldType, 0, label, new Float(Float.intBitsToFloat(dataOffset)));
			break;
		case 9:
			if (dataOffset + 8 > this.fieldDataLength) {
				throw new DBException(this.name + ": 字段数据偏移" + dataOffset + "超过字段数据");
			}
			long longBits = this.fieldDataBuffer[(dataOffset + 0)] & 0xFF
					| (this.fieldDataBuffer[(dataOffset + 1)] & 0xFF) << 8
					| (this.fieldDataBuffer[(dataOffset + 2)] & 0xFF) << 16
					| (this.fieldDataBuffer[(dataOffset + 3)] & 0xFF) << 24
					| (this.fieldDataBuffer[(dataOffset + 4)] & 0xFF) << 32
					| (this.fieldDataBuffer[(dataOffset + 5)] & 0xFF) << 40
					| (this.fieldDataBuffer[(dataOffset + 6)] & 0xFF) << 48
					| (this.fieldDataBuffer[(dataOffset + 7)] & 0xFF) << 56;
			element = new DBElement(fieldType, 0, label, new Double(Double.longBitsToDouble(longBits)));
			break;
		case 13:
			if (dataOffset + 4 > this.fieldDataLength) {
				throw new DBException("字段数据偏移" + dataOffset + "超过字段数据");
			}
			int byteLength = getInteger(this.fieldDataBuffer, dataOffset);
			dataOffset += 4;
			if (dataOffset + byteLength > this.fieldDataLength) {
				throw new DBException("虚数据长度" + byteLength + "超过字段数据");
			}
			byte[] byteData = new byte[byteLength];
			if (byteLength > 0) {
				System.arraycopy(this.fieldDataBuffer, dataOffset, byteData, 0, byteLength);
			}
			element = new DBElement(fieldType, 0, label, byteData);
			break;
		case 11:
			if (dataOffset + 1 > this.fieldDataLength) {
				throw new DBException(this.name + ": 字段数据偏移" + dataOffset + "超过字段数据");
			}
			int resourceLength = this.fieldDataBuffer[dataOffset] & 0xFF;
			dataOffset++;
			if (dataOffset + resourceLength > this.fieldDataLength)
				throw new DBException(this.name + ": 资源长度 " + resourceLength + "超过字段数据");
			String resourceString;
			if (resourceLength > 0)
				try {
					resourceString = new String(this.fieldDataBuffer, dataOffset, resourceLength, "UTF-8");
				} catch (UnsupportedEncodingException exc) {
					throw new DBException(this.name + ": UTF-8编码不支持", exc);
				}
			else {
				resourceString = new String();
			}
			element = new DBElement(fieldType, 0, label, resourceString);
			break;
		case 10:
			if (dataOffset + 4 > this.fieldDataLength) {
				throw new DBException(this.name + ": 字段数据偏移" + dataOffset + "超过字段数据");
			}
			int stringLength = getInteger(this.fieldDataBuffer, dataOffset);
			dataOffset += 4;
			if (dataOffset + stringLength > this.fieldDataLength)
				throw new DBException(this.name + ": 字符串长度" + stringLength + "超过字段数据");
			String string;
			if (stringLength > 0)
				try {
					string = new String(this.fieldDataBuffer, dataOffset, stringLength, "UTF-8");
				} catch (UnsupportedEncodingException exc) {
					throw new DBException(this.name + ": UTF-8编码不支持", exc);
				}
			else {
				string = new String();
			}
			element = new DBElement(fieldType, 0, label, string);
			break;
		case 12:
			if (dataOffset + 12 > this.fieldDataLength) {
				throw new DBException(this.name + ": 字段数据偏移" + dataOffset + "超过字段数据");
			}
			int localizedLength = getInteger(this.fieldDataBuffer, dataOffset);
			int stringReference = getInteger(this.fieldDataBuffer, dataOffset + 4);
			int substringCount = getInteger(this.fieldDataBuffer, dataOffset + 8);
			dataOffset += 12;
			localizedLength -= 8;
			LocalizedString localizedString = new LocalizedString(stringReference);
			for (int i = 0; i < substringCount; i++) {
				if (dataOffset + 8 > this.fieldDataLength) {
					throw new DBException(this.name + ": 本地化子字符串" + i + "超过字段数据");
				}
				if (localizedLength < 8) {
					throw new DBException(this.name + ": 本地化子字符串" + i + "超出本地化字符串");
				}
				int stringID = getInteger(this.fieldDataBuffer, dataOffset);
				int substringLength = getInteger(this.fieldDataBuffer, dataOffset + 4);
				dataOffset += 8;
				localizedLength -= 8;
				if (dataOffset + substringLength > this.fieldDataLength) {
					throw new DBException(this.name + ": 本地化子字符串" + i + "超过字段数据");
				}
				if (substringLength > localizedLength)
					throw new DBException(this.name + ": 本地化子字符串" + i + "超出本地化字符串");
				String substring;
				if (substringLength > 0)
					try {
						substring = new String(this.fieldDataBuffer, dataOffset, substringLength, "UTF-8");
					} catch (UnsupportedEncodingException exc) {
						throw new DBException(this.name + ": UTF-8编码不支持", exc);
					}
				else {
					substring = new String();
				}
				localizedString.addSubstring(new LocalizedSubstring(substring, stringID / 2, stringID & 0x1));
				dataOffset += substringLength;
				localizedLength -= substringLength;
			}
			element = new DBElement(fieldType, 0, label, localizedString);
			break;
		default:
			throw new DBException(this.name + ": 无法识别的字段类型" + fieldType);
		}
		return element;
	}

	private DBElement decodeStruct(String label, int index) throws DBException {
		if (index >= this.structArrayCount) {
			throw new DBException(this.name + ": 结构索引" + index + "超过数组大小");
		}
		int offset = 12 * index;
		int id = getInteger(this.structBuffer, offset);
		int fieldIndex = getInteger(this.structBuffer, offset + 4);
		int fieldCount = getInteger(this.structBuffer, offset + 8);
		DBList list = new DBList(fieldCount);
		if (fieldCount == 1) {
			DBElement field = decodeField(fieldIndex);
			list.addElement(field);
		} else if (fieldCount > 1) {
			offset = fieldIndex;
			for (int i = 0; i < fieldCount; i++) {
				if (offset + 4 > this.fieldIndicesLength) {
					throw new DBException("字段指数" + offset + "超过指数大小");
				}
				fieldIndex = getInteger(this.fieldIndicesBuffer, offset);
				offset += 4;
				DBElement field = decodeField(fieldIndex);
				list.addElement(field);
			}
		}
		return new DBElement(14, id, label, list);
	}

	private DBElement decodeList(String label, int offset) throws DBException {
		if (offset + 4 > this.listIndicesLength) {
			throw new DBException(this.name + ": 列表索引偏移量" + offset + "超过指数大小");
		}
		int structCount = getInteger(this.listIndicesBuffer, offset);
		DBList list = new DBList(structCount);
		int listOffset = offset + 4;
		for (int i = 0; i < structCount; i++) {
			if (listOffset + 4 > this.listIndicesLength) {
				throw new DBException(this.name + ": 列表索引偏移量" + listOffset + "超过指数大小");
			}
			int structIndex = getInteger(this.listIndicesBuffer, listOffset);
			listOffset += 4;
			list.addElement(decodeStruct(new String(), structIndex));
		}
		return new DBElement(15, 0, label, list);
	}

	public void save() throws DBException, IOException {
		File tmpFile = null;
		FileOutputStream out = null;
		if (this.file == null) {
			throw new IllegalStateException("没有可用的数据库文件");
		}
		try {
			tmpFile = new File(this.file.getPath() + ".new");
			out = new FileOutputStream(tmpFile);
			save(out);
			out.close();
			out = null;
			if ((this.file.exists()) && (!this.file.delete())) {
				throw new IOException("无法删除" + this.file.getName());
			}
			if (!tmpFile.renameTo(this.file)) {
				throw new IOException("无法重命名" + tmpFile.getName() + "为" + this.file.getName());
			}
		} finally {
			if (tmpFile != null) {
				if (out != null) {
					out.close();
				}
				if (tmpFile.exists())
					tmpFile.delete();
			}
		}
	}

	public void save(OutputStream out) throws DBException, IOException {
		try {
			this.structBuffer = new byte[48000];
			this.structArraySize = 4000;
			this.structArrayCount = 0;
			this.fieldBuffer = new byte[144000];
			this.fieldArraySize = 12000;
			this.fieldArrayCount = 0;
			this.labelBuffer = new byte[16000];
			this.labelArraySize = 1000;
			this.labelArrayCount = 0;
			this.fieldDataBuffer = new byte[20000];
			this.fieldDataSize = 20000;
			this.fieldDataLength = 0;
			this.fieldIndicesBuffer = new byte[36000];
			this.fieldIndicesSize = 36000;
			this.fieldIndicesLength = 0;
			this.listIndicesBuffer = new byte[8000];
			this.listIndicesSize = 8000;
			this.listIndicesLength = 0;
			if (this.topLevelStruct == null) {
				throw new DBException(this.name + ": 没有顶级结构");
			}
			if ((this.fileType == null) || (this.fileType.length() != 4)) {
				throw new DBException(this.name + ": 文件类型未设置");
			}
			if ((this.fileVersion == null) || (this.fileVersion.length() != 4)) {
				throw new DBException(this.name + ": 文件版本未设置");
			}
			encodeStruct(this.topLevelStruct);
			byte[] headerBuffer = new byte[56];
			byte[] buffer = this.fileType.getBytes();
			System.arraycopy(buffer, 0, headerBuffer, 0, 4);
			buffer = this.fileVersion.getBytes();
			System.arraycopy(buffer, 0, headerBuffer, 4, 4);
			int offset = 56;
			int structLength = 12 * this.structArrayCount;
			setInteger(offset, headerBuffer, 8);
			setInteger(this.structArrayCount, headerBuffer, 12);
			offset += structLength;
			int fieldLength = 12 * this.fieldArrayCount;
			setInteger(offset, headerBuffer, 16);
			setInteger(this.fieldArrayCount, headerBuffer, 20);
			offset += fieldLength;
			int labelLength = 16 * this.labelArrayCount;
			setInteger(offset, headerBuffer, 24);
			setInteger(this.labelArrayCount, headerBuffer, 28);
			offset += labelLength;
			setInteger(offset, headerBuffer, 32);
			setInteger(this.fieldDataLength, headerBuffer, 36);
			offset += this.fieldDataLength;
			setInteger(offset, headerBuffer, 40);
			setInteger(this.fieldIndicesLength, headerBuffer, 44);
			offset += this.fieldIndicesLength;
			setInteger(offset, headerBuffer, 48);
			setInteger(this.listIndicesLength, headerBuffer, 52);
			out.write(headerBuffer);
			out.write(this.structBuffer, 0, structLength);
			if (fieldLength != 0)
				out.write(this.fieldBuffer, 0, fieldLength);
			if (labelLength != 0)
				out.write(this.labelBuffer, 0, labelLength);
			if (this.fieldDataLength != 0)
				out.write(this.fieldDataBuffer, 0, this.fieldDataLength);
			if (this.fieldIndicesLength != 0)
				out.write(this.fieldIndicesBuffer, 0, this.fieldIndicesLength);
			if (this.listIndicesLength != 0) {
				out.write(this.listIndicesBuffer, 0, this.listIndicesLength);
			}
		} finally {
			this.structBuffer = null;
			this.fieldBuffer = null;
			this.labelBuffer = null;
			this.fieldDataBuffer = null;
			this.fieldIndicesBuffer = null;
			this.listIndicesBuffer = null;
		}
	}

	private int encodeField(DBElement element) throws DBException {
		int fieldType = element.getType();
		String fieldLabel = element.getLabel();
		if (fieldLabel.length() == 0) {
			throw new DBException("字段没有标签");
		}
		byte[] labelBytes = fieldLabel.getBytes();
		byte[] label = new byte[16];
		boolean match = false;
		System.arraycopy(labelBytes, 0, label, 0, Math.min(labelBytes.length, 16));
		int labelIndex;
		for (labelIndex = 0; labelIndex < this.labelArrayCount; labelIndex++) {
			int labelOffset = labelIndex * 16;
			match = true;
			for (int i = 0; i < 16; i++) {
				if (this.labelBuffer[(labelOffset + i)] != label[i]) {
					match = false;
					break;
				}
			}
			if (match) {
				break;
			}
		}
		if (!match) {
			if (this.labelArrayCount == this.labelArraySize) {
				this.labelArraySize += 1000;
				byte[] buffer = new byte[16 * this.labelArraySize];
				System.arraycopy(this.labelBuffer, 0, buffer, 0, this.labelArrayCount * 16);
				this.labelBuffer = buffer;
			}
			labelIndex = this.labelArrayCount++;
			int labelOffset = labelIndex * 16;
			System.arraycopy(label, 0, this.labelBuffer, labelOffset, 16);
		}
		Object fieldValue = element.getValue();
		int dataOffset;
		switch (fieldType) {
		case 15:
			dataOffset = encodeList(element);
			break;
		case 14:
			dataOffset = encodeStruct(element);
			break;
		case 0:
			dataOffset = ((Integer) fieldValue).intValue() & 0xFF;
			break;
		case 1:
			dataOffset = ((Character) fieldValue).charValue() & 0xFFFF;
			break;
		case 2:
		case 3:
			dataOffset = ((Integer) fieldValue).intValue() & 0xFFFF;
			break;
		case 4:
			dataOffset = ((Long) fieldValue).intValue();
			break;
		case 5:
			dataOffset = ((Integer) fieldValue).intValue();
			break;
		case 6:
		case 7:
			dataOffset = setFieldData(((Long) fieldValue).longValue());
			break;
		case 8:
			dataOffset = Float.floatToIntBits(((Float) fieldValue).floatValue());
			break;
		case 9:
			dataOffset = setFieldData(Double.doubleToLongBits(((Double) fieldValue).doubleValue()));
			break;
		case 13:
			byte[] voidData = (byte[]) fieldValue;
			int voidLength = voidData.length;
			byte[] voidBuffer = new byte[4 + voidLength];
			setInteger(voidLength, voidBuffer, 0);
			System.arraycopy(voidData, 0, voidBuffer, 4, voidLength);
			dataOffset = setFieldData(voidBuffer);
			break;
		case 11:
			String resourceString = (String) fieldValue;
			byte[] resourceData;
			try {
				resourceData = resourceString.getBytes("UTF-8");
			} catch (UnsupportedEncodingException exc) {
				throw new DBException(this.name + ": UTF-8编码不支持", exc);
			}
			int resourceLength = resourceData.length;
			if (resourceLength > 255) {
				throw new DBException("资源大小大于255");
			}
			byte[] resourceBuffer = new byte[1 + resourceLength];
			resourceBuffer[0] = ((byte) resourceLength);
			System.arraycopy(resourceData, 0, resourceBuffer, 1, resourceLength);
			dataOffset = setFieldData(resourceBuffer);
			break;
		case 10:
			String string = (String) fieldValue;
			byte[] stringBuffer;
			if (string.length() > 0) {
				byte[] stringData;
				try {
					stringData = string.getBytes("UTF-8");
				} catch (UnsupportedEncodingException exc) {
					throw new DBException(this.name + ": UTF-8编码不支持", exc);
				}
				int stringLength = stringData.length;
				stringBuffer = new byte[4 + stringLength];
				setInteger(stringLength, stringBuffer, 0);
				System.arraycopy(stringData, 0, stringBuffer, 4, stringLength);
			} else {
				stringBuffer = new byte[4];
				setInteger(0, stringBuffer, 0);
			}
			dataOffset = setFieldData(stringBuffer);
			break;
		case 12:
			LocalizedString localizedString = (LocalizedString) fieldValue;
			int substringCount = localizedString.getSubstringCount();
			int localizedLength = 8;
			List<byte[]> substringList = new ArrayList<byte[]>(substringCount);
			for (int i = 0; i < substringCount; i++) {
				LocalizedSubstring localizedSubstring = localizedString.getSubstring(i);
				String substring = localizedSubstring.getString();
				byte[] substringData;
				if (substring.length() > 0)
					try {
						substringData = substring.getBytes("UTF-8");
					} catch (UnsupportedEncodingException exc) {
						throw new DBException(this.name + ": UTF-8编码不支持", exc);
					}
				else {
					substringData = new byte[0];
				}
				substringList.add(substringData);
				localizedLength += 8 + substringData.length;
			}
			byte[] localizedBuffer = new byte[4 + localizedLength];
			setInteger(localizedLength, localizedBuffer, 0);
			setInteger(localizedString.getStringReference(), localizedBuffer, 4);
			setInteger(substringCount, localizedBuffer, 8);
			int substringOffset = 12;
			for (int i = 0; i < substringCount; i++) {
				LocalizedSubstring localizedSubstring = localizedString.getSubstring(i);
				byte[] substringData = (byte[]) substringList.get(i);
				int substringLength = substringData.length;
				setInteger(localizedSubstring.getLanguage() * 2 + localizedSubstring.getGender(), localizedBuffer,
						substringOffset);
				setInteger(substringLength, localizedBuffer, substringOffset + 4);
				if (substringLength > 0)
					System.arraycopy(substringData, 0, localizedBuffer, substringOffset + 8, substringLength);
				substringOffset += 8 + substringLength;
			}
			dataOffset = setFieldData(localizedBuffer);
			break;
		default:
			throw new DBException(this.name + ": 无法识别的字段类型" + fieldType);
		}
		if (this.fieldArrayCount == this.fieldArraySize) {
			this.fieldArraySize += 4000;
			byte[] buffer = new byte[12 * this.fieldArraySize];
			System.arraycopy(this.fieldBuffer, 0, buffer, 0, this.fieldArrayCount * 12);
			this.fieldBuffer = buffer;
		}
		int fieldIndex = this.fieldArrayCount++;
		int fieldOffset = fieldIndex * 12;
		setInteger(fieldType, this.fieldBuffer, fieldOffset);
		setInteger(labelIndex, this.fieldBuffer, fieldOffset + 4);
		setInteger(dataOffset, this.fieldBuffer, fieldOffset + 8);
		return fieldIndex;
	}

	private int encodeStruct(DBElement element) throws DBException {
		DBList list = (DBList) element.getValue();
		int fieldCount = list.getElementCount();
		int fieldOffset = 0;
		if (this.structArrayCount == this.structArraySize) {
			this.structArraySize += 2000;
			byte[] buffer = new byte[12 * this.structArraySize];
			System.arraycopy(this.structBuffer, 0, buffer, 0, this.structArrayCount * 12);
			this.structBuffer = buffer;
		}
		int structIndex = this.structArrayCount++;
		if (fieldCount == 1) {
			fieldOffset = encodeField(list.getElement(0));
		} else if (fieldCount > 1) {
			int indexLength = 4 * fieldCount;
			if (this.fieldIndicesLength + indexLength > this.fieldIndicesSize) {
				int increment = Math.max(indexLength, 8000);
				this.fieldIndicesSize += increment;
				byte[] buffer = new byte[this.fieldIndicesSize];
				System.arraycopy(this.fieldIndicesBuffer, 0, buffer, 0, this.fieldIndicesLength);
				this.fieldIndicesBuffer = buffer;
			}
			fieldOffset = this.fieldIndicesLength;
			this.fieldIndicesLength += indexLength;
			for (int i = 0; i < fieldCount; i++) {
				int fieldIndex = encodeField(list.getElement(i));
				setInteger(fieldIndex, this.fieldIndicesBuffer, fieldOffset + 4 * i);
			}
		}
		int structOffset = structIndex * 12;
		setInteger(element.getID(), this.structBuffer, structOffset);
		setInteger(fieldOffset, this.structBuffer, structOffset + 4);
		setInteger(fieldCount, this.structBuffer, structOffset + 8);
		return structIndex;
	}

	private int encodeList(DBElement element) throws DBException {
		DBList list = (DBList) element.getValue();
		int listCount = list.getElementCount();
		int listLength = (listCount + 1) * 4;
		if (this.listIndicesLength + listLength > this.listIndicesSize) {
			int increment = Math.max(listLength, 2000);
			this.listIndicesSize += increment;
			byte[] buffer = new byte[this.listIndicesSize];
			System.arraycopy(this.listIndicesBuffer, 0, buffer, 0, this.listIndicesLength);
			this.listIndicesBuffer = buffer;
		}
		int listOffset = this.listIndicesLength;
		this.listIndicesLength += listLength;
		setInteger(listCount, this.listIndicesBuffer, listOffset);
		for (int i = 0; i < listCount; i++) {
			int structIndex = encodeStruct(list.getElement(i));
			setInteger(structIndex, this.listIndicesBuffer, listOffset + 4 * (i + 1));
		}
		return listOffset;
	}

	private int setFieldData(byte[] data) {
		int dataLength = data.length;
		if (this.fieldDataLength + dataLength > this.fieldDataSize) {
			int increment = Math.max(dataLength, 8000);
			this.fieldDataSize += increment;
			byte[] buffer = new byte[this.fieldDataSize];
			System.arraycopy(this.fieldDataBuffer, 0, buffer, 0, this.fieldDataLength);
			this.fieldDataBuffer = buffer;
		}
		int dataOffset = this.fieldDataLength;
		this.fieldDataLength += dataLength;
		System.arraycopy(data, 0, this.fieldDataBuffer, dataOffset, dataLength);
		return dataOffset;
	}

	private int setFieldData(long data) {
		if (this.fieldDataLength + 8 > this.fieldDataSize) {
			this.fieldDataSize += 8000;
			byte[] buffer = new byte[this.fieldDataSize];
			System.arraycopy(this.fieldDataBuffer, 0, buffer, 0, this.fieldDataLength);
			this.fieldDataBuffer = buffer;
		}
		int dataOffset = this.fieldDataLength;
		this.fieldDataLength += 8;
		this.fieldDataBuffer[(dataOffset + 0)] = ((byte) (int) data);
		this.fieldDataBuffer[(dataOffset + 1)] = ((byte) (int) (data >> 8));
		this.fieldDataBuffer[(dataOffset + 2)] = ((byte) (int) (data >> 16));
		this.fieldDataBuffer[(dataOffset + 3)] = ((byte) (int) (data >> 24));
		this.fieldDataBuffer[(dataOffset + 4)] = ((byte) (int) (data >> 32));
		this.fieldDataBuffer[(dataOffset + 5)] = ((byte) (int) (data >> 40));
		this.fieldDataBuffer[(dataOffset + 6)] = ((byte) (int) (data >> 48));
		this.fieldDataBuffer[(dataOffset + 7)] = ((byte) (int) (data >> 56));
		return dataOffset;
	}

	private int getInteger(byte[] buffer, int offset) {
		return buffer[(offset + 0)] & 0xFF | (buffer[(offset + 1)] & 0xFF) << 8 | (buffer[(offset + 2)] & 0xFF) << 16
				| (buffer[(offset + 3)] & 0xFF) << 24;
	}

	private void setInteger(int number, byte[] buffer, int offset) {
		buffer[offset] = ((byte) number);
		buffer[(offset + 1)] = ((byte) (number >>> 8));
		buffer[(offset + 2)] = ((byte) (number >>> 16));
		buffer[(offset + 3)] = ((byte) (number >>> 24));
	}
}