package com.tmall.search.httpclient.compress;

import java.util.List;

import com.tmall.search.httpclient.util.ProtocolException;

public final class DecoderUtils {
	
	/**
	 * 根据当前压缩算法名,得到对应decoder
	 * @param acceptEncodingList
	 * @param compressName
	 * @return
	 * @throws ProtocolException 
	 */
	public static AcceptDecoder getAcceptDecoder(List<AcceptDecoder> acceptEncodingList,String compressName) throws ProtocolException{
		if(acceptEncodingList==null || acceptEncodingList.isEmpty()){
			throw new ProtocolException("Compression algorithm is not set");
		}
		AcceptDecoder acceptDecoder = null;
		for(AcceptDecoder ad : acceptEncodingList){
			if(compressName.equals(ad.getAlgorithmName())){
				acceptDecoder = ad;
				break;
			}
		}
		if(acceptDecoder==null){
			throw new ProtocolException("No matching compression algorithm");
		}
		return acceptDecoder;
	}
	
	public static String acceptEncodingStr(List<AcceptDecoder> acceptEncodingList){
		StringBuilder sb = new StringBuilder(4);
		for(int i=0;i<acceptEncodingList.size();i++){
			if(i==0){
				sb.append(acceptEncodingList.get(i).getAlgorithmName());
			}else{
				sb.append(",").append(acceptEncodingList.get(i).getAlgorithmName());
			}
		}
		return sb.toString();
	}
	
}
