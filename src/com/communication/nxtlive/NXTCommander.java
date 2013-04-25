package com.communication.nxtlive;

public class NXTCommander {
	public static final byte A = 1;
	public static final byte B = 7;
	public static final byte C = 11;

	public static byte[] toBytes(short s) {
		return new byte[] { (byte) (s & 0x00FF), (byte) ((s & 0xFF00) >> 8) };
	}

	public static byte[] startProgramm(String programmName) {
		byte[] send = { (byte) (programmName.length() + 3), (byte) 0x00,
				(byte) 0x80, (byte) 0x00 };

		return (new String(send) + programmName + '\0').getBytes();
	}

	public static byte[] run(int motors, int power) {

		int cL = 14; //command length

		byte z[];

		switch (motors) {
		case A:
			return setOutput(0, power);
		case B:
			return setOutput(1, power);
		case C:
			return setOutput(2, power);
		case A + B:
			z = new byte[cL*2];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, power), 0, z, cL, cL);
			return z;
		case A - B:
			z = new byte[cL*2];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, -power), 0, z, cL, cL);
			return z;
		case -A + B:
			z = new byte[cL*2];
			System.arraycopy(setOutput(0, -power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, power), 0, z, cL, cL);
			return z;
		case -A - B:
			z = new byte[cL*2];
			System.arraycopy(setOutput(0, -power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, -power), 0, z, cL, cL);
			return z;
		case A + C:
			z = new byte[cL*2];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(2, power), 0, z, cL, cL);
			return z;
		case A - C:
			z = new byte[cL*2];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL, cL);
			return z;
		case -A + C:
			z = new byte[cL*2];
			System.arraycopy(setOutput(0, -power), 0, z, 0, cL);
			System.arraycopy(setOutput(2, power), 0, z, cL, cL);
			return z;
		case -A - C:
			z = new byte[cL*2];
			System.arraycopy(setOutput(0, -power), 0, z, 0, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL, cL);
			return z;
		case B + C:
			z = new byte[cL*2];
			System.arraycopy(setOutput(1, power), 0, z, 0, cL);
			System.arraycopy(setOutput(2, power), 0, z, cL, cL);
			return z;
		case B - C:
			z = new byte[cL*2];
			System.arraycopy(setOutput(1, power), 0, z, 0, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL, cL);
			return z;
		case -B + C:
			z = new byte[cL*2];
			System.arraycopy(setOutput(1, -power), 0, z, 0, cL);
			System.arraycopy(setOutput(2, power), 0, z, cL, cL);
			return z;
		case -B - C:
			z = new byte[cL*2];
			System.arraycopy(setOutput(1, -power), 0, z, 0, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL, cL);
			return z;
		case A + B + C:
			z = new byte[cL*2];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, power), 0, z, cL, cL);
			System.arraycopy(setOutput(2, power), 0, z, 0, cL);
			return z;
		case A + B - C:
			z = new byte[cL*3];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, power), 0, z, cL, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL*2, cL);
			return z;
		case A - B + C:
			z = new byte[cL*3];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, power), 0, z, cL, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL*2, cL);
			return z;
		case A - B - C:
			z = new byte[cL*3];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, -power), 0, z, cL, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL*2, cL);
		case -A + B + C:
			z = new byte[cL*3];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, power), 0, z, cL, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL*2, cL);
			return z;
		case -A + B - C:
			z = new byte[cL*3];
			System.arraycopy(setOutput(0, power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, power), 0, z, cL, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL*2, cL);
			return z;
		case -A - B + C:
			z = new byte[cL*3];
			System.arraycopy(setOutput(0, -power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, -power), 0, z, cL, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL*2, cL);
			return z;
		case -A - B - C:
			z = new byte[cL*3];
			System.arraycopy(setOutput(0, -power), 0, z, 0, cL);
			System.arraycopy(setOutput(1, -power), 0, z, cL, cL);
			System.arraycopy(setOutput(2, -power), 0, z, cL*2, cL);
			return z;
		}

		return null;
	}

/*	public static int textToMotors(String text){
		int motors = 0;
		
		if(text.indexOf('A') >= 0)
			if(text.indexOf('A') > 0)
				if(text.charAt(text.indexOf('A'))-1 == '-')
					motors -= A;
		
		
		
		return motors;
	}*/

	public static byte[] setOutput(int motor, int power) {

		byte[] send = { 12, 0x00, -128, 4, (byte) (motor & 0x00FF), (byte) (power & 0x00FF), 1, 0, 100, 32, 0, 0, 0, 0};

		return send;
	}

	public static byte[] sendMesage(String msg, int mailBox) {

		byte z[] = new byte[msg.length()+7];


		byte[] send = { (byte) (msg.length() + 5), 0, -128,
				9, (byte) (mailBox & 0x00FF), (byte) (msg.length() & 0x00FF) };

		System.arraycopy(send, 0, z, 0, send.length);
		System.arraycopy(msg.getBytes(), 0, z, send.length, msg.length());
		z[z.length-1] = 0;

		return z;
	}

	public static byte[] playTone(short freq, short duration) {

		byte[] f = toBytes(freq);
		byte[] d = toBytes(duration);

		byte[] send = { 6, 0, -128, 3, f[0], f[1], d[0], d[1] };

		return send;
	}
}