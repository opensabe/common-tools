/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Base64 {
	private static final byte[] encodingTable = { (byte) 'A', (byte) 'B',
			(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G',
			(byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
			(byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q',
			(byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V',
			(byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a',
			(byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f',
			(byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k',
			(byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p',
			(byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u',
			(byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z',
			(byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
			(byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9',
			(byte) '+', (byte) '/' };

	private static final byte[] decodingTable;
	static {
		decodingTable = new byte[128];
		for (int i = 0; i < 128; i++) {
			decodingTable[i] = (byte) -1;
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			decodingTable[i] = (byte) (i - 'A');
		}
		for (int i = 'a'; i <= 'z'; i++) {
			decodingTable[i] = (byte) (i - 'a' + 26);
		}
		for (int i = '0'; i <= '9'; i++) {
			decodingTable[i] = (byte) (i - '0' + 52);
		}
		decodingTable['+'] = 62;
		decodingTable['/'] = 63;
	}

	public static String encodeS(byte[] data) {
		return new String(encode(data));
	}

	public static byte[] encode(byte[] data) {
		byte[] bytes;
		int modulus = data.length % 3;
		if (modulus == 0) {
			bytes = new byte[(4 * data.length) / 3];
		} else {
			bytes = new byte[4 * ((data.length / 3) + 1)];
		}
		int dataLength = (data.length - modulus);
		int a1;
		int a2;
		int a3;
		for (int i = 0, j = 0; i < dataLength; i += 3, j += 4) {
			a1 = data[i] & 0xff;
			a2 = data[i + 1] & 0xff;
			a3 = data[i + 2] & 0xff;
			bytes[j] = encodingTable[(a1 >>> 2) & 0x3f];
			bytes[j + 1] = encodingTable[((a1 << 4) | (a2 >>> 4)) & 0x3f];
			bytes[j + 2] = encodingTable[((a2 << 2) | (a3 >>> 6)) & 0x3f];
			bytes[j + 3] = encodingTable[a3 & 0x3f];
		}
		int b1;
		int b2;
		int b3;
		int d1;
		int d2;
		switch (modulus) {
		case 0: /* nothing left to do */
			break;
		case 1:
			d1 = data[data.length - 1] & 0xff;
			b1 = (d1 >>> 2) & 0x3f;
			b2 = (d1 << 4) & 0x3f;
			bytes[bytes.length - 4] = encodingTable[b1];
			bytes[bytes.length - 3] = encodingTable[b2];
			bytes[bytes.length - 2] = (byte) '=';
			bytes[bytes.length - 1] = (byte) '=';
			break;
		case 2:
			d1 = data[data.length - 2] & 0xff;
			d2 = data[data.length - 1] & 0xff;
			b1 = (d1 >>> 2) & 0x3f;
			b2 = ((d1 << 4) | (d2 >>> 4)) & 0x3f;
			b3 = (d2 << 2) & 0x3f;
			bytes[bytes.length - 4] = encodingTable[b1];
			bytes[bytes.length - 3] = encodingTable[b2];
			bytes[bytes.length - 2] = encodingTable[b3];
			bytes[bytes.length - 1] = (byte) '=';
			break;
		}
		return bytes;
	}

	public static byte[] decode(byte[] data) {
		byte[] bytes;
		byte b1;
		byte b2;
		byte b3;
		byte b4;
		data = discardNonBase64Bytes(data);
		if (data[data.length - 2] == '=') {
			bytes = new byte[(((data.length / 4) - 1) * 3) + 1];
		} else if (data[data.length - 1] == '=') {
			bytes = new byte[(((data.length / 4) - 1) * 3) + 2];
		} else {
			bytes = new byte[((data.length / 4) * 3)];
		}
		for (int i = 0, j = 0; i < (data.length - 4); i += 4, j += 3) {
			b1 = decodingTable[data[i]];
			b2 = decodingTable[data[i + 1]];
			b3 = decodingTable[data[i + 2]];
			b4 = decodingTable[data[i + 3]];
			bytes[j] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[j + 1] = (byte) ((b2 << 4) | (b3 >> 2));
			bytes[j + 2] = (byte) ((b3 << 6) | b4);
		}
		if (data[data.length - 2] == '=') {
			b1 = decodingTable[data[data.length - 4]];
			b2 = decodingTable[data[data.length - 3]];
			bytes[bytes.length - 1] = (byte) ((b1 << 2) | (b2 >> 4));
		} else if (data[data.length - 1] == '=') {
			b1 = decodingTable[data[data.length - 4]];
			b2 = decodingTable[data[data.length - 3]];
			b3 = decodingTable[data[data.length - 2]];
			bytes[bytes.length - 2] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[bytes.length - 1] = (byte) ((b2 << 4) | (b3 >> 2));
		} else {
			b1 = decodingTable[data[data.length - 4]];
			b2 = decodingTable[data[data.length - 3]];
			b3 = decodingTable[data[data.length - 2]];
			b4 = decodingTable[data[data.length - 1]];
			bytes[bytes.length - 3] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[bytes.length - 2] = (byte) ((b2 << 4) | (b3 >> 2));
			bytes[bytes.length - 1] = (byte) ((b3 << 6) | b4);
		}
		return bytes;
	}

	public static byte[] decode(String data) {
		byte[] bytes;
		byte b1;
		byte b2;
		byte b3;
		byte b4;
		data = discardNonBase64Chars(data);
		if (data.charAt(data.length() - 2) == '=') {
			bytes = new byte[(((data.length() / 4) - 1) * 3) + 1];
		} else if (data.charAt(data.length() - 1) == '=') {
			bytes = new byte[(((data.length() / 4) - 1) * 3) + 2];
		} else {
			bytes = new byte[((data.length() / 4) * 3)];
		}
		for (int i = 0, j = 0; i < (data.length() - 4); i += 4, j += 3) {
			b1 = decodingTable[data.charAt(i)];
			b2 = decodingTable[data.charAt(i + 1)];
			b3 = decodingTable[data.charAt(i + 2)];
			b4 = decodingTable[data.charAt(i + 3)];
			bytes[j] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[j + 1] = (byte) ((b2 << 4) | (b3 >> 2));
			bytes[j + 2] = (byte) ((b3 << 6) | b4);
		}
		if (data.charAt(data.length() - 2) == '=') {
			b1 = decodingTable[data.charAt(data.length() - 4)];
			b2 = decodingTable[data.charAt(data.length() - 3)];
			bytes[bytes.length - 1] = (byte) ((b1 << 2) | (b2 >> 4));
		} else if (data.charAt(data.length() - 1) == '=') {
			b1 = decodingTable[data.charAt(data.length() - 4)];
			b2 = decodingTable[data.charAt(data.length() - 3)];
			b3 = decodingTable[data.charAt(data.length() - 2)];
			bytes[bytes.length - 2] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[bytes.length - 1] = (byte) ((b2 << 4) | (b3 >> 2));
		} else {
			b1 = decodingTable[data.charAt(data.length() - 4)];
			b2 = decodingTable[data.charAt(data.length() - 3)];
			b3 = decodingTable[data.charAt(data.length() - 2)];
			b4 = decodingTable[data.charAt(data.length() - 1)];
			bytes[bytes.length - 3] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[bytes.length - 2] = (byte) ((b2 << 4) | (b3 >> 2));
			bytes[bytes.length - 1] = (byte) ((b3 << 6) | b4);
		}
		return bytes;
	}

	private static byte[] discardNonBase64Bytes(byte[] data) {
		byte[] temp = new byte[data.length];
		int bytesCopied = 0;
		for (int i = 0; i < data.length; i++) {
			if (isValidBase64Byte(data[i])) {
				temp[bytesCopied++] = data[i];
			}
		}
		byte[] newData = new byte[bytesCopied];
		System.arraycopy(temp, 0, newData, 0, bytesCopied);
		return newData;
	}

	private static String discardNonBase64Chars(String data) {
		StringBuffer sb = new StringBuffer();
		int length = data.length();
		for (int i = 0; i < length; i++) {
			if (isValidBase64Byte((byte) (data.charAt(i)))) {
				sb.append(data.charAt(i));
			}
		}
		return sb.toString();
	}

	private static boolean isValidBase64Byte(byte b) {
		if (b == '=') {
			return true;
		} else if ((b < 0) || (b >= 128)) {
			return false;
		} else if (decodingTable[b] == -1) {
			return false;
		}
		return true;
	}

	public static byte[] compressBytes(byte input[]) {
		int cachesize = 1024;

		Deflater compresser = new Deflater();

		compresser.reset();
		compresser.setInput(input);
		compresser.finish();
		byte output[] = new byte[0];
		ByteArrayOutputStream o = new ByteArrayOutputStream(input.length);
		try {
			byte[] buf = new byte[cachesize];
			int got;
			while (!compresser.finished()) {
				got = compresser.deflate(buf);
				o.write(buf, 0, got);
			}
			output = o.toByteArray();
		} finally {
			try {
				o.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return output;
	}

	public static byte[] decompressBytes(byte input[]) {
		int cachesize = 1024;
		Inflater decompresser = new Inflater();

		byte output[] = new byte[0];
		decompresser.reset();
		decompresser.setInput(input);
		ByteArrayOutputStream o = new ByteArrayOutputStream(input.length);
		try {
			byte[] buf = new byte[cachesize];
			int got;
			while (!decompresser.finished()) {
				got = decompresser.inflate(buf);
				o.write(buf, 0, got);
			}
			output = o.toByteArray();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				o.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return output;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		String bs = "eyJtYXRjaExpc3QiOlt7Im1hdGNoSWQiOjIwMTQwMjEzNDAwMSwic2NtIjpbIjMiLCIxIiwiMCJdLCJzcCI6WyIxLjExIiwiMS4yMiIsIjEuMzMiXSwiZ3Vlc3ROYW1lIjoiMeWPt+WuouWcunh4eCIsImhvc3ROYW1lIjoiMeWPt+S4u+WcuiIsIm1hdGNoTnVtQ24iOiLlkajkuowwMDEiLCJycSI6MX0seyJtYXRjaElkIjoyMDE0MDIxMzQwMDIsInNjbSI6WyIxIiwiMCJdLCJzcCI6WyIyLjIyIiwiMi4yMiIsIjEuMzMiXSwiZ3Vlc3ROYW1lIjoiMuWPt+WuouWcunh4eCIsImhvc3ROYW1lIjoiMuWPt+S4u+WcuiIsIm1hdGNoTnVtQ24iOiLlkajkuowwMDIiLCJycSI6MH0seyJtYXRjaElkIjoyMDE0MDIxMzQwMDMsInNjbSI6WyIzIl0sInNwIjpbIjMuMTUiLCIyLjIyIiwiMS4zMyJdLCJndWVzdE5hbWUiOiIz5Y+35a6i5Zy6IiwiaG9zdE5hbWUiOiIz5Y+35Li75Zy6IiwibWF0Y2hOdW1DbiI6IuWRqOS6jDAwMyIsInJxIjotMX0seyJtYXRjaElkIjoyMDE0MDIxMzQwMDQsInNjbSI6WyIwIl0sInNwIjpbIjIuMTEiLCIxLjIyIiwiMi4zMyJdLCJndWVzdE5hbWUiOiI05Y+35a6i5Zy6IiwiaG9zdE5hbWUiOiI05Y+35Li75Zy6IiwibWF0Y2hOdW1DbiI6IuWRqOS6jDAwNCIsInJxIjotMX0seyJtYXRjaElkIjoyMDE0MDIxMzQwMDUsInNjbSI6WyIxIiwiMyJdLCJzcCI6WyIxLjMzIiwiMS4yMiIsIjEuMzMiXSwiZ3Vlc3ROYW1lIjoiNeWPt+WuouWcuiIsImhvc3ROYW1lIjoiNeWPt+S4u+WcuiIsIm1hdGNoTnVtQ24iOiLlkajkuowwMDUiLCJycSI6LTF9LHsibWF0Y2hJZCI6MjAxNDAyMTM0MDA2LCJzY20iOlsiMyIsIjEiLCIwIl0sInNwIjpbIjEuMTEiLCIxLjIyIiwiMS4zMyJdLCJndWVzdE5hbWUiOiIx5Y+35a6i5Zy6IiwiaG9zdE5hbWUiOiIx5Y+35Li75Zy6IiwibWF0Y2hOdW1DbiI6IuWRqOS6jDAwMSIsInJxIjoxfSx7Im1hdGNoSWQiOjIwMTQwMjEzNDAwNywic2NtIjpbIjEiLCIwIl0sInNwIjpbIjIuMjIiLCIyLjIyIiwiMS4zMyJdLCJndWVzdE5hbWUiOiIy5Y+35a6i5Zy6IiwiaG9zdE5hbWUiOiIy5Y+35Li75Zy6IiwibWF0Y2hOdW1DbiI6IuWRqOS6jDAwMiIsInJxIjowfSx7Im1hdGNoSWQiOjIwMTQwMjEzNDAwOCwic2NtIjpbIjMiXSwic3AiOlsiMy4xNSIsIjIuMjIiLCIxLjMzIl0sImd1ZXN0TmFtZSI6IjPlj7flrqLlnLoiLCJob3N0TmFtZSI6IjPlj7fkuLvlnLoiLCJtYXRjaE51bUNuIjoi5ZGo5LqMMDAzIiwicnEiOi0xfSx7Im1hdGNoSWQiOjIwMTQwMjEzNDAwOSwic2NtIjpbIjAiXSwic3AiOlsiMi4xMSIsIjEuMjIiLCIyLjMzIl0sImd1ZXN0TmFtZSI6IjTlj7flrqLlnLoiLCJob3N0TmFtZSI6IjTlj7fkuLvlnLoiLCJtYXRjaE51bUNuIjoi5ZGo5LqMMDA0IiwicnEiOi0xfSx7Im1hdGNoSWQiOjIwMTQwMjEzNDAxMCwic2NtIjpbIjEiLCIzIl0sInNwIjpbIjEuMzMiLCIxLjIyIiwiMS4zMyJdLCJndWVzdE5hbWUiOiI15Y+35a6i5Zy6IiwiaG9zdE5hbWUiOiI15Y+35Li75Zy6IiwibWF0Y2hOdW1DbiI6IuWRqOS6jDAwNSIsInJxIjotMX0seyJtYXRjaElkIjoyMDE0MDIxMzQwMTEsInNjbSI6WyIzIiwiMSIsIjAiXSwic3AiOlsiMS4xMSIsIjEuMjIiLCIxLjMzIl0sImd1ZXN0TmFtZSI6IjHlj7flrqLlnLoiLCJob3N0TmFtZSI6IjHlj7fkuLvlnLoiLCJtYXRjaE51bUNuIjoi5ZGo5LqMMDAxIiwicnEiOjF9LHsibWF0Y2hJZCI6MjAxNDAyMTM0MDEyLCJzY20iOlsiMSIsIjAiXSwic3AiOlsiMi4yMiIsIjIuMjIiLCIxLjMzIl0sImd1ZXN0TmFtZSI6IjLlj7flrqLlnLoiLCJob3N0TmFtZSI6IjLlj7fkuLvlnLoiLCJtYXRjaE51bUNuIjoi5ZGo5LqMMDAyIiwicnEiOjB9LHsibWF0Y2hJZCI6MjAxNDAyMTM0MDEzLCJzY20iOlsiMyJdLCJzcCI6WyIzLjE1IiwiMi4yMiIsIjEuMzMiXSwiZ3Vlc3ROYW1lIjoiM+WPt+WuouWcuiIsImhvc3ROYW1lIjoiM+WPt+S4u+WcuiIsIm1hdGNoTnVtQ24iOiLlkajkuowwMDMiLCJycSI6LTF9LHsibWF0Y2hJZCI6MjAxNDAyMTM0MDE0LCJzY20iOlsiMCJdLCJzcCI6WyIyLjExIiwiMS4yMiIsIjIuMzMiXSwiZ3Vlc3ROYW1lIjoiNOWPt+WuouWcuiIsImhvc3ROYW1lIjoiNOWPt+S4u+WcuiIsIm1hdGNoTnVtQ24iOiLlkajkuowwMDQiLCJycSI6LTF9LHsibWF0Y2hJZCI6MjAxNDAyMTM0MDE1LCJzY20iOlsiMSIsIjMiXSwic3AiOlsiMS4zMyIsIjEuMjIiLCIxLjMzIl0sImd1ZXN0TmFtZSI6IjXlj7flrqLlnLoiLCJob3N0TmFtZSI6IjXlj7fkuLvlnLoiLCJtYXRjaE51bUNuIjoi5ZGo5LqMMDA1IiwicnEiOi0xfV0sImJldFRpbWVzIjoiMTAiLCJnYW1lRXh0cmEiOiI3XzEiLCJnYW1lSWQiOiIyMDExMTExNTIxWVgwMDYwMjAwMSJ9";
		System.out.println(new String(Base64.decode(bs), "UTF-8"));
		System.out.println(new String(Base64.decode(bs), "GBK"));
		System.out.println(new String(Base64.decode(bs)));
		
		
		String a = "\u4e2d\u6587\u6c49\u5b57 ";
		System.out.println(a);
		String u = "尾页";//\u5c3e\u9875
		//String u = "胜平负"; //\u80dc\u5e73\u8d1f
		//String u = "比分"; //\u6bd4\u5206
		//String u = "总进球数"; //\u603b\u8fdb\u7403\u6570
		//String u = "半全场";//\u534a\u5168\u573a
		//String u = "其他";//\u5176\u4ed6
		//System.out.println(u + "+" + u.length());
		//System.out.println(Character.isHighSurrogate(u.charAt(0)));
		//System.out.println((int) u.charAt(0));
		System.out.println("\\u" + Integer.toHexString(u.codePointAt(0)));
		System.out.println("\\u" + Integer.toHexString(u.codePointAt(1)));
		//System.out.println("\\u" + Integer.toHexString(u.codePointAt(2)));
		//System.out.println("\\u" + Integer.toHexString(u.codePointAt(3)));
	}
}