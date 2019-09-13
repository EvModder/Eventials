package Eventials.mailbox;

import java.io.File;
import java.util.UUID;

public interface MailboxFetcher{
	public interface MailListener{
		public abstract void playerMailboxLoaded(UUID playerUUID, File mailbox, String message);
		public abstract void playerMailboxSaved(UUID playerUUID, String message);
	};

	abstract public void loadMailbox(UUID playerUUID, MailListener callback, boolean lock);
	abstract public void saveMailbox(UUID playerUUID, File mailboxFile, MailListener callback, boolean lock);
}