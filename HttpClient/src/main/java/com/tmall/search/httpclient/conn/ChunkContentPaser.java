package com.tmall.search.httpclient.conn;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.util.ByteList;
import com.tmall.search.httpclient.util.ByteUtil;
import com.tmall.search.httpclient.util.ChunkContext;
import com.tmall.search.httpclient.util.HttpException;
import com.tmall.search.httpclient.util.IllegalChunkDataException;

/**
 * chunk paser  byte copy
 * @author xiaolin.mxl
 */
public class ChunkContentPaser implements ContentPaser {

	public ChunkContentPaser(HttpConnection conn, Header header, ByteBuffer readBuffer) {
		this.conn = conn;
		this.readBuffer = readBuffer;
		this.pos = header.getLength();
	}

	private HttpConnection conn;
	private int pos;
	private ByteBuffer readBuffer;
	private boolean done = false;
	private static final byte[] terminated = new byte[] { Header.CR, Header.LF };

	private void readNextChunk() throws HttpException {
		try {
			this.readBuffer = conn.read();
			this.pos = 0;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new HttpException("Paser Chunk data error", e);
		}
	}

	/**
	 * 入口方法
	 */
	public byte[] paser() throws HttpException {
		if (done) {
			throw new HttpException("finished reading the buffer ,Please Invoke reset() method");
		}
		ChunkContext csi = new ChunkContext();
		int chunkLength = -1;
		while (chunkLength != 0) {
			chunkLength = nextSize();
			csi.grow(chunkLength);
			csi.setReadLength(chunkLength);
			nextData(csi);
		}
		done = true;
		return csi.getData();
	}

	private int nextSize() throws HttpException {
		ByteList snippet = new ByteList(); //save the last time not handle byteArray
		int length = -1, mark = 0, pos13 = 0;
		do {
			for (int cursor = pos; cursor < readBuffer.limit(); cursor++) {
				if (readBuffer.get(cursor) == Header.CR) {
					mark = 13;
					pos13 = cursor;
				} else if (readBuffer.get(cursor) == Header.LF) {
					if (mark == 13 && (cursor - pos13 == 1 || cursor == 0)) { //if x,13,10 || x,13 next-> 10,x
						String pasString;
						if (cursor < 2) {//current chunk data "13,10,x,x,x,x" or "10,x,x,x"
							pasString = new String(snippet.array());
						} else {
							pasString = new String(ByteUtil.mergeByteArray(snippet.array(), readBuffer.array(), pos, cursor - pos - 1));
						}
						length = Integer.parseInt(pasString, 16);
						pos = cursor + 1;
						return length;
					} else {
						throw new IllegalChunkDataException("Not properly terminated by CRLF");
					}
				}
			}
			int saveLength;
			if (mark == 13) { //remove suffix CR
				saveLength = readBuffer.limit() - pos - 1; //if mark==13 current chunk "x,x,13"
			} else {
				saveLength = readBuffer.limit() - pos;// "x,x" next-> "x,x,13,10,x"
			}
			snippet.add(readBuffer.array(), pos, saveLength);
			readNextChunk();
		} while (length == -1);
		return length;
	}

	private void nextData(ChunkContext csi) throws HttpException {
		if (pos >= readBuffer.limit()) {
			readNextChunk();
		}
		if (csi.getRemaining() > 0) { //match perfix
			int remaining = csi.getRemaining();
			for (int i = 2 - csi.getRemaining(); i <= remaining; i++) {
				if (terminated[i] != readBuffer.get(pos++)) {
					throw new IllegalChunkDataException("Not properly terminated by CRLF");
				} else {
					remaining--;
				}
			}
			csi.setRemaining(remaining);
		}
		if (csi.getReadLength() > readBuffer.limit() - pos) {//copy all
			//csi.setData(ByteUtil.mergeByteArray(csi.getData(), readBuffer.array(), pos, readBuffer.limit() - pos));
			csi.put(readBuffer.array(), pos, readBuffer.limit() - pos);
			csi.setReadLength(csi.getReadLength() - (readBuffer.limit() - pos));
			pos = readBuffer.limit();
			nextData(csi);
		} else {
			if (csi.getReadLength() >= 0) {
				//csi.setData(ByteUtil.mergeByteArray(csi.getData(), readBuffer.array(), pos, csi.getReadLength()));
				csi.put(readBuffer.array(), pos, csi.getReadLength());
				pos += csi.getReadLength();
				csi.setReadLength(-1);
				if (pos + 2 <= readBuffer.limit()) {
					if (readBuffer.get(pos) == Header.CR && readBuffer.get(pos + 1) == Header.LF) {
						pos += 2; //pos step over CRLF
					} else {
						throw new IllegalChunkDataException("Not properly terminated by CRLF");
					}
				} else {
					int finsh = 0;
					for (; pos < readBuffer.limit(); pos++) { //match suffix
						if (terminated[finsh++] != readBuffer.get(pos)) {
							throw new IllegalChunkDataException("Not properly terminated by CRLF");
						}
					}
					csi.setRemaining(2 - finsh);
					nextData(csi);
				}
			}
		}
	}

	@Override
	public void reset(HttpConnection conn, Header header, ByteBuffer buffer) {
		this.conn = conn;
		this.readBuffer = buffer;
		this.pos = header.getLength();
		this.done = false;
	}
	
}
