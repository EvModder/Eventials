package Eventials.mailbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MailboxUtils{
	static String readBinaryFileAsString(File file){
		try{
			String data = new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
			return data.replace("|", " --pipesep-- *").replace("\n", " --linebreak-- ");
		}
		catch(IOException e){return null;}
	}
	static boolean saveBinaryStringToFile(File file, String data){
		data = data.replace(" --pipesep-- ", "|").replace(" --linebreak-- ", "\n");
		try(FileOutputStream fos = new FileOutputStream(file)){
			fos.write(data.getBytes(StandardCharsets.ISO_8859_1));
			return true;
		}
		catch(IOException e){return false;}
	}
}
