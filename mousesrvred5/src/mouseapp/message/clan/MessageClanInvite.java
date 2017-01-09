package mouseapp.message.clan;

import mouseapp.clan.ClanInfo;
import mouseapp.message.Message;

public class MessageClanInvite extends Message{
	public ClanInfo claninfo;
	
	public MessageClanInvite(byte type, int roomId, ClanInfo claninfo){
		super(type, roomId);
	
		this.claninfo = claninfo;
	}
}
