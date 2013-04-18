package com.example.nxtlive;



public class NXTCommander {
	static int A = 1;
	static int B = 2;
	static int C = 4;
	
	public static byte[] startProgramm(String programmName){
		char[] send = {(char) (programmName.length()+3), 0x00, 0x80, 0x00};

		return (new String(send)+programmName+'\0').getBytes();
	} 
	
	/*public static byte[] run(String programmName){
		char[] send = {(char) programmName.length(), 0x00, 0x80, 0x00};

		return (new String(send)+programmName+'\0').getBytes();
	} */
	
	public static byte[] sendMesage(String msg, int inputBox){
		char[] send = {(char) (msg.length()+5), 0x00, 0x80, 0x09, (char) inputBox,(char) (msg.length())};

		return (new String(send)+msg+'\0').getBytes();
	}
	
	public static byte[] playTone(int  freq, int duration){
		char[] send = {0x04, 0x00, 0x80, 0x03, (char) freq,(char) duration};

		return (new String(send)).getBytes();
	}
}
