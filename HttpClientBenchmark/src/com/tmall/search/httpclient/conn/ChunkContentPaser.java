package com.tmall.search.httpclient.conn;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.util.ByteList;
import com.tmall.search.httpclient.util.ByteUtil;
import com.tmall.search.httpclient.util.ChunkContext;
import com.tmall.search.httpclient.util.HttpException;

public class ChunkContentPaser implements ContentPaser {

	public ChunkContentPaser(HttpConnection conn, Header header, ByteBuffer readBuffer) {
		this.conn = conn;
		this.readBuffer = readBuffer;
		this.pos = header.getLength();
		//System.out.println(convertString(this.readBuffer));
	}

	private HttpConnection conn;
	private int pos;
	private ByteBuffer readBuffer;
	private static final byte[] terminated = new byte[] { Header.CR, Header.LF };

	private void readNextChunk() throws HttpException {
		try {
			this.readBuffer = conn.read();
			//System.out.println(convertString(this.readBuffer));
			this.pos = 0;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new HttpException("Paser Chunk data error", e);
		}
	}

	/**
	 *  4\r\nWiki\r\n
		5\r\npedia\r\n
		e\r\nin\r\n
		\r\nchunks.\r\n
		0\r\n\r\n
	
	public byte[] paser(HttpConnection conn, Header header, ByteBuffer readBuffer) throws HttpException {
		byte[] respData;
		ByteBuffer buffer = readBuffer;
		ChunkStateInfo xiaolin = new ChunkStateInfo();
		boolean end = false;
		while (!end) {
			end = ByteUtil.isChunkEnd(xiaolin, buffer.array(), buffer.limit());

			ByteUtil.fillChunkBody(xiaolin, buffer.array(), xiaolin.getChunkData() == null ? header.getLength() : 0, buffer.limit());
			buffer.clear();
			if (!end) {
				try {
					buffer = conn.read();
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					throw new HttpException("Paser Chunk data error", e);
				}
			}
		}
		respData = xiaolin.getChunkData();
		return respData;
	} */

	public byte[] paser(HttpConnection conn, Header header, ByteBuffer readBuffer) throws HttpException {
		ChunkContext csi = new ChunkContext();
		int chunkLength;
		while ((chunkLength = nextChunkLength()) != 0) {
			csi.setReadLength(chunkLength);
			readNextChunkData(csi);
		}
		csi.setReadLength(0);
		readNextChunkData(csi);
		return csi.getChunkData();
		/*for(int i=0;i<24;i++){
			readNextChunk();
		}
		return new byte[0];*/
	}

	private int nextChunkLength() throws HttpException {
		ByteList snippet = new ByteList();
		int length = -1, mark = 0, posCR = 0;
		do {
			for (int cursor = pos; cursor < readBuffer.limit(); cursor++) {
				if (readBuffer.get(cursor) == Header.CR) {
					mark = 13;
					posCR = cursor;
				} else if (readBuffer.get(cursor) == Header.LF) {
					if (mark == 13 && (cursor - posCR == 1 || cursor == 0)) {
						String pasString;
						if (cursor < 2) {
							pasString = new String(snippet.array());
						} else {
							pasString = new String(ByteUtil.mergeByteArray(snippet.array(), readBuffer.array(), pos, cursor - pos - 1));
						}
						length = Integer.parseInt(pasString, 16);
						pos = cursor + 1;
						return length;
					} else {
						throw new HttpException("");
					}
				}
			}
			int l;
			if(mark == 13){
				l=readBuffer.limit() - pos - 1;
			}else{
				l=readBuffer.limit() - pos;
			}
			snippet.add(readBuffer.array(), pos, l);
			readNextChunk();
		} while (length == -1);
		return length;
	}

	private void readNextChunkData(ChunkContext csi) throws HttpException {
		if (pos >= readBuffer.limit()) {
			readNextChunk();
		}
		if (csi.getRemaining() > 0) {
			int remaining = csi.getRemaining();
			for (int i = 2 - csi.getRemaining(); i <= remaining; i++) {
				if (terminated[i] != readBuffer.get(pos++)) {
					throw new HttpException("xx");
				} else {
					remaining--;
				}
			}
			csi.setRemaining(remaining);
		}
		if (csi.getReadLength() > readBuffer.limit() - pos) {
			csi.setChunkData(ByteUtil.mergeByteArray(csi.getChunkData(), readBuffer.array(), pos, readBuffer.limit() - pos));
			csi.setReadLength(csi.getReadLength() - (readBuffer.limit() - pos));
			pos = readBuffer.limit();
			readNextChunkData(csi);
		} else {
			if (csi.getReadLength() >= 0) {
				csi.setChunkData(ByteUtil.mergeByteArray(csi.getChunkData(), readBuffer.array(), pos, csi.getReadLength()));
				pos += csi.getReadLength();
				csi.setReadLength(-1);
				if (pos + 2 <= readBuffer.limit()) {
					if (readBuffer.get(pos) == Header.CR && readBuffer.get(pos + 1) == Header.LF) {
						pos += 2;
					} else {
						throw new HttpException("...");
					}
				} else {
					int finsh = 0;
					for (; pos < readBuffer.limit(); pos++) {
						if (terminated[finsh++] != readBuffer.get(pos)) {
							throw new HttpException("xx");
						}
					}
					csi.setRemaining(2 - finsh);
					readNextChunkData(csi);
				}
			}
		}
	}

	private String convertString(ByteBuffer b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.limit(); i++) {
			sb.append(b.get(i)).append(",");
		}
		return sb.toString();
	}

}
