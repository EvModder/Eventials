package ParticleEffects;

import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_12_R1.PlayerConnection;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleComboRainbowArc extends ParticleComboEffect{
	private Location lastLoc;
	private PacketPlayOutWorldParticles[] packets;
	
	//arc stuff
	final float r = 1.73F; //by calculation; center=playerhead, clouddist=2.5, cloudelev=1.2
	final float span = .8058F;//angle (from vertical)
	final int arc_res = 10;
	final float theta_incr = span/arc_res;//basically resolution
	
	//rainbow stuff
	final int color_res=7;
	final double height = .7;
	final double particleSpacing = height/color_res;
	
	private void determineRainbow(Location center){
		packets = new PacketPlayOutWorldParticles[color_res*(arc_res*2+1)];
		i=-1;
		for(float theta = -span; theta<= span; theta+=theta_incr){
			Vector v = center.getDirection().multiply(MathUtils.sin(theta)*r);
			
			addRainbowLine(center.clone(), new Vector(v.getX(), MathUtils.cos(theta)*r, v.getZ()));
		}
	}
	private int i;
	private void addRainbowLine(Location loc, Vector dir){
		Vector addDir = dir.clone().normalize().multiply(particleSpacing);
		loc.add(dir.add(dir.clone().multiply(-height/2))).add(0, .4, 0);
		
		packets[++i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
				(float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), 1F, 0F, 0F, 1, 0);//red
		loc.add(addDir);
		packets[++i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
				(float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), 1F, .6F, 0F, 1, 0);//orange
		loc.add(addDir);
		packets[++i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
				(float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), 0F, 1F, 0F, 1, 0);//yellow
		loc.add(addDir);
		packets[++i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
				(float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), .5F, 1F, 0F, 1, 0);//green
		loc.add(addDir);
		packets[++i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
				(float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), .01F, .4F, 1F, 1, 0);//blue
		loc.add(addDir);
		packets[++i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
				(float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), .5F, 0F, 1F, 1, 0);//indigo
		loc.add(addDir);
		packets[++i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
				(float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), .8F, 0F, 1F, 1, 0);//violet
	}
	
	@Override
	public void display(Location loc, Player... ppl){
		if(!loc.equals(lastLoc)){
			lastLoc = loc;
			determineRainbow(loc);
		}
		for(Player player : ppl){
			PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
			for(PacketPlayOutWorldParticles packet : packets) connection.sendPacket(packet);
		}
	}
}
