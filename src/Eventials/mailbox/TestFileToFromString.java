package Eventials.mailbox;

import java.io.File;

public class TestFileToFromString{
	public static void main(String... args){
		String data = "load uuid|"+MailboxUtils.readBinaryFileAsString(new File("empty_inv_playerdata.dat"));
		MailboxUtils.saveBinaryStringToFile(new File("test.dat"), data.substring(10));
	}
}