package ParticleEffects;

import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleComboTexasFlag extends ParticleComboEffect{
	private float PX_SPREAD = .15F;
	private int FLAG_WIDTH = (int) (6/PX_SPREAD);//6 blocks (FLAG_WIDTH = # of pixels = # of blocks/PX_SPREAD)
	private int FLAG_HEIGHT = (int) (FLAG_WIDTH*(2.0/3.0));
	private int FLAGPOLE_HEIGHT = (int) (7/PX_SPREAD);
	
	PacketPlayOutWorldParticles[] packets;
	Location lastLoc;
	
	List<Point> redParticles, whiteParticles, blueParticles, yellowParticles;
	public ParticleComboTexasFlag(){
		Shape star = createStar(5, (int)(FLAG_WIDTH/6), (int)(FLAG_WIDTH/3), FLAG_WIDTH/8, FLAG_WIDTH/16, MathUtils.PI/2);
		redParticles = new ArrayList<Point>();
		whiteParticles = new ArrayList<Point>();
		blueParticles = new ArrayList<Point>();
		yellowParticles = new ArrayList<Point>();
		
		//make flag pole
		for(int y=(int)(1/PX_SPREAD); y<FLAGPOLE_HEIGHT; ++y){//9 blocks tall, starts 1 block above head
			yellowParticles.add(new Point(0, y));
		}
		//make blue rectangle and star
		for(int x=0; x<FLAG_WIDTH/3; ++x)for(int y=0; y<FLAG_HEIGHT; ++y){//4 blocks tall, 2 blocks wide
			if(star.contains(x, y)) whiteParticles.add(new Point(x, y+FLAGPOLE_HEIGHT));
			else blueParticles.add(new Point(x, y+FLAGPOLE_HEIGHT));
		}
		
		//make red rectangle
		for(int x=FLAG_WIDTH/3; x<FLAG_WIDTH; ++x) for(int y=0; y<FLAG_HEIGHT/2; ++y){//2 blocks tall, 4 blocks wide
			redParticles.add(new Point(x, y+FLAGPOLE_HEIGHT));
		}
		//make white rectangle
		for(int x=FLAG_WIDTH/3; x<FLAG_WIDTH; ++x) for(int y=FLAG_HEIGHT/2; y<FLAG_HEIGHT; ++y){//2 blocks tall, 4 blocks wide
			whiteParticles.add(new Point(x, y+FLAGPOLE_HEIGHT));
		}
		int totalParticles = yellowParticles.size() + redParticles.size() + whiteParticles.size() + blueParticles.size();
		packets = new PacketPlayOutWorldParticles[totalParticles];
	}
	
	public Shape createStar(int arms, int cx, int cy, float rOuter, float rInner, float startAngle){
		float angle = MathUtils.PI / arms;

		GeneralPath path = new GeneralPath();

		for (int i = 0; i < 2 * arms; ++i){
			float r = (i & 1) == 0 ? rOuter : rInner;
			Point2D.Double p = new Point2D.Double(cx + MathUtils.cos(i*angle + startAngle) * r,
												  cy + MathUtils.sin(i*angle + startAngle) * r);
			if (i == 0) path.moveTo(p.getX(), p.getY());
			else path.lineTo(p.getX(), p.getY());
		}
		path.closePath();
		return path;
	}
	
	private void initializeParticles(Location loc){
		Vector direction = loc.getDirection().setY(0).normalize().multiply(-PX_SPREAD);
		float changeX = (float) direction.getX(), changeZ = (float) direction.getZ();
		float xo = (float)loc.getX(), yo = (float)loc.getY(), zo = (float)loc.getZ();
		
		int i=0;
		for(Point pt : yellowParticles){
			packets[i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
							xo+changeX*pt.x, yo+pt.y*PX_SPREAD, zo+changeZ*pt.x, .656F, .512F, .01F, 1, 0);
			++i;
		}
		for(Point pt : blueParticles){
			packets[i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
							xo+changeX*pt.x, yo+pt.y*PX_SPREAD, zo+changeZ*pt.x, .001F, .1569F, .4078F, 1, 0);
			++i;
		}
		for(Point pt : whiteParticles){
			packets[i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
							xo+changeX*pt.x, yo+pt.y*PX_SPREAD, zo+changeZ*pt.x, 1F, 1F, 1F, 1, 0);
			++i;
		}
		for(Point pt : redParticles){
			packets[i] = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
							xo+changeX*pt.x, yo+pt.y*PX_SPREAD, zo+changeZ*pt.x, .7461F, .0391F, .1875F, 1, 0);
			++i;
		}
	}
	
	@Override
	public void display(Location loc, Player... ppl){
		if(!loc.equals(lastLoc)){initializeParticles(loc); lastLoc = loc;}
		
		for(Player player : ppl){
			PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
			for(PacketPlayOutWorldParticles packet : packets) connection.sendPacket(packet);
		}
	}
}
