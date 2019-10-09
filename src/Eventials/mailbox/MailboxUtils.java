package Eventials.mailbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class MailboxUtils{
	static String readBinaryFileAsString(File file){
		try{
			String data = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
			return data.replace("|", " --pipesep-- ").replace("\n", " --linebreak-- ");
		}
		catch(IOException e){return null;}
	}
	static boolean saveBinaryStringToFile(File file, String data){
		data = data.replace(" --pipesep-- ", "|").replace(" --linebreak-- ", "\n");
		try(FileOutputStream fos = new FileOutputStream(file)){
			fos.write(Base64.getDecoder().decode(data));
			return true;
		}
		catch(IOException e){return false;}
	}
}
