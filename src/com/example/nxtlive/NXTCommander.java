package com.example.nxtlive;



public class NXTCommander {
	static int A = 1;
	static int B = 2;
	static int C = 4;
	
	public static byte[] startProgramm(String programmName){
		byte[] send = {(byte) (programmName.length()+3), (byte)0x00, (byte)0x80, (byte)0x00};

		return (new String(send)+programmName+'\0').getBytes();
	} 
	
	/*public static byte[] run(String programmName){
		char[] send = {(char) programmName.length(), 0x00, 0x80, 0x00};

		return (new String(send)+programmName+'\0').getBytes();
	} */
	
	public static byte[] sendMesage(String msg, int inputBox){
		byte[] send = {(byte) (msg.length()+5),(byte) 0x00,(byte) 0x80,(byte) 0x09, (byte) inputBox,(byte) (msg.length())};

		return (new String(send)+msg+'\0').getBytes();
	}
	
	public static byte[] playTone(int  freq, int duration){
		byte[] send = {(byte)0x04, (byte)0x00, (byte)0x80, (byte)0x03, (byte) freq,(byte) duration};

		return (new String(send)).getBytes();
	}
}
