package com.tmall.search.httpclient.util;

/**
 * simple byte list , be careful
 * @author xiaolin.mxl
 */
public class ByteList {

	byte[] data ;
	int pos = 0;
	
	public ByteList(int initialCapacity) {
		data = new byte[initialCapacity];
	}
	
	public ByteList() {
		this(8);
	}
	
	public void add(byte b){
		check(1);
		data[pos++] = b;
	}
	
	public void add(byte[] array){
		check(array.length);
		for(int i=0;i<array.length;i++){
			data[pos++] = array[i];
		}
	}
	
	/**
	 * 没有做任何条件检查.
	 * @param array
	 * @param start
	 * @param length
	 */
	public void add(byte[] array , int start ,int length){
		check(length);
		for(int i=start;i<start+length;i++){
			data[pos++] = array[i];
		}
	}
	
	private void check(int length){
		if(pos+length>=data.length){
			byte[] newData = new byte[pos+length];
			System.arraycopy(data, 0, newData, 0, data.length);
			data = newData;
		}
	}
	
	public byte[] array(){
		if(pos==data.length){
			return data;
		}else{
			byte[] result = new byte[pos];
			System.arraycopy(data, 0, result, 0, pos);
			return result;
		}
		
	}
	
	public void clear(){
		data = null;
	}
	
	
	public static void main(String[] args) {
		
		ByteList bl = new ByteList();
		
		bl.add((byte)1);
		
		byte[] a = new byte[]{10,11,12,13,14,15,16,17,18};
		
		byte[] b = new byte[]{20,21,22,23,24,25,26,27,28};
		
		bl.add(a);
		
		bl.add(b,2,2);
		
		byte[] xxx = bl.array();
		
		//[1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 22, 23]
		
		System.out.println(new String(xxx));
		
	}
	
}
