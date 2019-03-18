package ParticleEffects;

import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ParticleComboRainbowTrail extends ParticleComboEffect{
	Location lastLoc;
	final int resolution=24;
	final double colorChange = 254.0/(resolution/6.0);//254 because bugs.
	final int skipEndColors = 2;
	final double height = 1.0;
	final double particleSpacing = height/(resolution-skipEndColors);

	@Override
	public void display(Location loc, Player... ppl){
		if(!loc.equals(lastLoc)){
			lastLoc = loc;
			loc.add(loc.getDirection().setY(0).normalize().multiply(-.3));
			float x = (float)loc.getX(), y = (float)loc.getY()-.5F, z = (float)loc.getZ();

			PacketPlayOutWorldParticles[] packets = new PacketPlayOutWorldParticles[resolution-skipEndColors];

			int i=0;
			float r=255, g=1, b=1;
			for(; g<255; g+=colorChange, ++i) packets[i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
					x, y+=particleSpacing, z, r/255F, g/255F, b/255F, 1, 0);

			for(; r>1; r-=colorChange, ++i) packets[i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
					x, y+=particleSpacing, z, r/255F, g/255F, b/255F, 1, 0);

			for(; b<255; b+=colorChange, ++i) packets[i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
					x, y+=particleSpacing, z, r/255F, g/255F, b/255F, 1, 0);

			for(; g>1; g-=colorChange, ++i) packets[i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
					x, y+=particleSpacing, z, r/255F, g/255F, b/255F, 1, 0);

			for(; r<255; r+=colorChange, ++i) packets[i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
					x, y+=particleSpacing, z, r/255F, g/255F, b/255F, 1, 0);

			//doesn't go all the way back to the bottom (that would just be red)
			for(; b>1+colorChange*skipEndColors; b-=colorChange, ++i) packets[i] = new PacketPlayOutWorldParticles(
					EnumParticle.REDSTONE, true, x, y+=particleSpacing, z, r/255F, g/255F, b/255F, 1, 0);

			for(Player player : ppl){
				PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
				for(PacketPlayOutWorldParticles packet : packets) connection.sendPacket(packet);
			}
		}
	}
}