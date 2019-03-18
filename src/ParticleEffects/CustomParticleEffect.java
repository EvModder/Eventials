package ParticleEffects;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.WeatherType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public enum CustomParticleEffect {
	TRAIL_1(){
		@Override public void display(Player p, long time){
			//if(time%15 == 0) p.getWorld().FOOTSTEP.display(0, 0, 0, 0, 1, p.getLocation(), 20);
			if(time%15 == 0) p.getWorld().spawnParticle(Particle.BLOCK_DUST, p.getLocation(), 1);
		}
	},
	TRAIL_1_LIFTED(){
		@Override public void display(Player p, long time){
			//if(time%10 == 0) ParticleEffect.FOOTSTEP.display(0, 0, 0, 0, 1, p.getLocation().add(0,.1,0), 20);
			if(time%15 == 0) p.getWorld().spawnParticle(Particle.BLOCK_DUST, p.getLocation().add(0,.1,0), 1);
		}
	},
	TRAIL_2(){
		@Override public void display(Player p, long time){
			Location behind = p.getEyeLocation().add(p.getEyeLocation().getDirection().setY(0).normalize().multiply(-1.5));
			//ParticleEffect.LAVA.display(.5F, .5F, .5F, 0, 5, behind, 50);
			p.getWorld().spawnParticle(Particle.LAVA, behind, 5, .5, .5, .5);
		}
	},
	TRAIL_3(){
		@Override public void display(Player p, long time){
//			if(time%5 == 0) ParticleEffect.FLAME.display(0, 0, 0, 0, 1, p.getLocation(), 20);
			if(time%4 == 0) p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 1, 0);
		}
	},
	LOVE(){
		@Override public void display(Player p, long time){
//			if(time%10 == 0) ParticleEffect.HEART.display(0, 0, 0, 0, 1, p.getEyeLocation().add(0, .6, 0), 50);
			if(time%10 == 0) p.getWorld().spawnParticle(Particle.HEART, p.getEyeLocation().add(0, .6, 0), 1);
		}
	},
	NOVA(){
		float r, theta, theta_mod, mod_incr =  23 * MathUtils.degreesToRadians;
		double tempX;
		@Override public void display(Player p, long time){
			r = (time%50)/25F + .05F;
			theta = r*MathUtils.PI + theta_mod;
			if(r == .05){
				theta_mod += mod_incr;
				if(theta_mod > MathUtils.PI2) theta_mod -= MathUtils.PI2;
				if(!p.isSprinting()){
//					ParticleEffect.SUSPENDED_DEPTH.display(0, 0, 0, 0, 200, p.getLocation().add(0,1,0), 10);
					p.getWorld().spawnParticle(Particle.SUSPENDED_DEPTH, p.getLocation().add(0, 1, 0), 200);
				}
			}
			Vector dir = new Vector(r*MathUtils.sin(theta), r/8, r*MathUtils.cos(theta));
//			ParticleEffect.SUSPENDED_DEPTH.display(0, 0, 0, 0, 1, p.getLocation().add(0,1,0).add(dir), 10);
//			ParticleEffect.SUSPENDED_DEPTH.display(0, 0, 0, 0, 1, p.getLocation().add(0,1,0).add(dir.multiply(-1)), 10);
			p.getWorld().spawnParticle(Particle.SUSPENDED_DEPTH, p.getLocation().add(0,1,0).add(dir), 1);
			p.getWorld().spawnParticle(Particle.SUSPENDED_DEPTH, p.getLocation().add(0,1,0).add(dir.multiply(-1)), 1);
			tempX = dir.getX(); dir.setX(dir.getZ()); dir.setZ(-tempX);
//			ParticleEffect.SUSPENDED_DEPTH.display(0, 0, 0, 0, 1, p.getLocation().add(0,1,0).add(dir), 10);
//			ParticleEffect.SUSPENDED_DEPTH.display(0, 0, 0, 0, 1, p.getLocation().add(0,1,0).add(dir.multiply(-1)), 10);
			p.getWorld().spawnParticle(Particle.SUSPENDED_DEPTH, p.getLocation().add(0,1,0).add(dir), 1);
			p.getWorld().spawnParticle(Particle.SUSPENDED_DEPTH, p.getLocation().add(0,1,0).add(dir.multiply(-1)), 1);
		}
	},
	FUNNEL(){
		float x,y,z,t, theta_mod = 0;
		@Override public void display(Player p, long time){
			t = (time % 45)*8*MathUtils.degreesToRadians;
			if(t == 0){
				if(theta_mod == MathUtils.PI2) theta_mod = 0;
				else theta_mod += MathUtils.degreesToRadians;
			}
			x = MathUtils.sin(t) + MathUtils.sin(theta_mod);
			z = MathUtils.cos(t) + MathUtils.cos(theta_mod);
			y = (float) (2*Math.sqrt(t));
//			ParticleEffect.FLAME.display(0, 0, 0, 0, 1, p.getLocation().add(x,y,z), 10);
			p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(x, y, z), 1, 0);
		}
	},
	TRAIL_COLOR(){
//		@SuppressWarnings("deprecation")
		@Override public void display(Player p, long time){
//			byte color = (byte) rand.nextInt(16);
//			ParticleEffect.BLOCK_CRACK.display(new ParticleData(Material.STAINED_CLAY,color),0,0,0,0,1,p.getLocation(),p);
//			p.getWorld().spawnParticle(Particle.BLOCK_DUST, p.getLocation(), 1, new MaterialData(Material.STAINED_CLAY, color));
			p.getWorld().spawnParticle(Particle.BLOCK_DUST, p.getLocation(), 1,
					(rand.nextInt(255)+1)/255.0, (rand.nextInt(255)+1)/255.0, (rand.nextInt(255)+1)/255.0, 0);
		}
	},
	ELLIPTIC_PARABOLOID(){
		double t_squared, MAX_T = Math.sqrt(1.5);
		@Override public void display(Player p, long time){
			t_squared = Math.pow((time*.001F % MAX_T), 2);
			for(float theta=0; theta<MathUtils.PI2; theta+=MathUtils.degreesToRadians){
				double modX = MathUtils.sin(theta)*t_squared, modZ = MathUtils.cos(theta)*t_squared;
//				ParticleEffect.ENCHANTMENT_TABLE.display(0, 0, 0, 0, 1, p.getLocation().add(modX, 3-t_squared*2, modZ), p);
				p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, p.getLocation().add(modX, 3-t_squared*2, modZ), 1);
			}
		}
	},
	SPIKEMAN(){
		Vector randV;
		int spikeLen;
		Location loc;
		@Override public void display(Player p, long time){
			randV = Vector.getRandom().subtract(Vector.getRandom()).normalize().multiply(.05);
			spikeLen = rand.nextInt(80);
			loc = p.getLocation();
			for(short i=0; i<spikeLen; ++i){
				loc.add(randV);
//				ParticleEffect.PORTAL.display(0, 0, 0, 0, 1, loc, p);
				p.getWorld().spawnParticle(Particle.PORTAL, loc, 1, 0);
			}
		}
	},
	LAME(){
		@Override public void display(Player p, long time){
//			if(time%5 == 0) ParticleEffect.SPELL_MOB_AMBIENT.display(1, 1, 1, 1, 1, p.getLocation(), p);
			p.getWorld().spawnParticle(Particle.SPELL_MOB_AMBIENT, p.getLocation(), 3, 1, 1, 1, 1);
		}
	},
	BROKEN_SYSTEM(){
		final float spread = .2F;
		Vector[] directions = new Vector[]{
			new Vector(spread,0,0), new Vector(-spread,0,0),
			new Vector(0,spread,0), new Vector(0,-spread,0),
			new Vector(0,0,spread), new Vector(0,0,-spread)
		};
		
		@Override public void display(Player p, long time){
			Vector randomOffset = Vector.getRandom().setY(0).normalize().multiply(.2);
			
			Location loc = p.getEyeLocation().add(randomOffset);
			int maxLength = 8;
			int numArms = rand.nextInt(10)+3;
			int currentLength;
			Vector currentDir = directions[rand.nextInt(directions.length)];

			for(int i=0; i<numArms; ++i){
				currentLength = rand.nextInt(maxLength);
				if(maxLength > 3 && i%2 == 0) --maxLength;

				for(int j=0; j<currentLength; ++j){
//					ParticleEffect.FLAME.display(0, 0, 0, 0, 1, loc, 20);
					p.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0);
					loc.add(currentDir);
				}

				Vector newDir = directions[rand.nextInt(directions.length)];
				while(newDir.equals(currentDir) && newDir.multiply(-1).equals(currentDir)){
					newDir = directions[rand.nextInt(directions.length)];
				}
				currentDir = newDir;
			}
		}
	},
	/*TRAIL_RAINBOW(){
		ParticleComboRainbowTrail rainbow = new ParticleComboRainbowTrail();
		@Override public void display(Player p, long time){
			if(time%2 == 0) rainbow.display(p.getEyeLocation(), 50);
			if(p.isSprinting() || p.getVehicle() != null){
				Location loc = p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(-.07));
				rainbow.display(loc, 50);
				rainbow.display(loc.add(loc.getDirection().multiply(-.07)), 50);
			}
		}
	},*/
	OFFLIMITS(){
		@Override public void display(Player p, long time){
//			ParticleEffect.BARRIER.display(3, 3, 3, 0, 20, p.getEyeLocation(), 5);
			if(time%4 == 0) p.getWorld().spawnParticle(Particle.BARRIER, p.getEyeLocation(), 20, 3, 3, 3);
		}
	},
	BATWINGS(){
		@Override public void display(Player p, long time){
			//TODO
		}
	},
	SADNESS(){
		@Override public void display(Player p, long time){
////		ParticleEffect.SMOKE_LARGE.display(.5F, .2F, .5F, 0, 20, p.getEyeLocation().add(0, 2, 0), 10);
//			ParticleEffect.CLOUD.display(.5F, .2F, .5F, 0, 45, p.getEyeLocation().add(0, 2, 0), 10);
//			ParticleEffect.DRIP_WATER.display(.5F, .01F, .5F, 0, 2, p.getEyeLocation().add(0, 1.8, 0), 10);
//			ParticleEffect.WATER_DROP.display(.5F, .01F, .5F, 0, 2, p.getEyeLocation().add(0, 1.8, 0), 10);
			p.getWorld().spawnParticle(Particle.CLOUD, p.getEyeLocation().add(0, 2, 0), 45, .5, .1, .5, 0);
			p.getWorld().spawnParticle(Particle.DRIP_WATER, p.getEyeLocation().add(0, 1.8, 0), 2, .5, .01, .5);
			p.getWorld().spawnParticle(Particle.WATER_DROP, p.getEyeLocation().add(0, 1.8, 0), 2, .5, .01, .5);
			
			if(rand.nextInt(144000) == 0) p.getWorld().strikeLightning(p.getLocation());
			if(rand.nextInt(48000) == 0) p.setPlayerWeather(WeatherType.DOWNFALL);
		}
	},
	/*LUVRAINBOWZ(){
		ParticleComboRainbowArc rainbow = new ParticleComboRainbowArc();
		
		@Override public void display(Player p, long time){
			Vector looking = p.getEyeLocation().getDirection().setY(0).normalize().multiply(1.25);
			double x = looking.getX(); looking.setX(-looking.getZ()); looking.setZ(x);//swap X & Z
			
			Location toLeft = p.getEyeLocation().add(looking).add(0, 1.2, 0);
			Location toRight = p.getEyeLocation().add(looking.multiply(-1)).add(0, 1.2, 0);
//			ParticleEffect.CLOUD.display(.3F, .1F, .3F, 0, 10, toLeft, 13);
//			ParticleEffect.CLOUD.display(.3F, .1F, .3F, 0, 10, toRight, 13);
			p.getWorld().spawnParticle(Particle.CLOUD, toLeft, 10, .3, .1, .3, 0);//TODO: test added 0
			p.getWorld().spawnParticle(Particle.CLOUD, toRight, 10, .3, .1, .3, 0);
			
			//display rainbow
			if(time%2 == 0) rainbow.display(p.getEyeLocation().setDirection(looking.normalize()), 10);
		}
	},*/
	DARK_KNIGHT(){
		ParticleComboBatman batSymbol = new ParticleComboBatman();
		@Override public void display(Player p, long time){
			if(time % 10 == 0) batSymbol.display(p.getEyeLocation(), 50);
		}
	},
	LAZER(){
		@Override public void display(Player p, long time){
			if(time % 10 == 0){
				double dist = Double.MAX_VALUE;
				Entity nearest = null;
				for(Entity e : p.getNearbyEntities(12, 7, 12)){
					double d = e.getLocation().distanceSquared(p.getLocation());
					if(d < dist){
						dist = d;
						nearest = e;
					}
				}
				if(nearest != null){
					//TODO: guardian lazer from p to nearest
				}
			}
		}
	},
	RANDPARTICLE(){
		@Override public void display(Player p, long time){
//			try{
//				ParticleEffect effect = ParticleEffect.values()[rand.nextInt(ParticleEffect.values().length)];
//				if(effect != ParticleEffect.BARRIER && effect != ParticleEffect.MOB_APPEARANCE &&
//				effect != ParticleEffect.EXPLOSION_HUGE && effect != ParticleEffect.CLOUD &&
//				effect != ParticleEffect.EXPLOSION_LARGE && effect != ParticleEffect.EXPLOSION_NORMAL)
//					effect.display(3, 1, 3, 0, 1, p.getEyeLocation(), 5);
//			}
//			catch(IllegalArgumentException ex){}
//			catch(ParticleDataException ex){}
			Particle particle = Particle.values()[rand.nextInt(Particle.values().length)];
			if(particle != Particle.MOB_APPEARANCE)
				p.getWorld().spawnParticle(particle, p.getEyeLocation(), 1, 3, 1, 3, 0);
		}
	},
	FOOFCLOUD(){
		@Override public void display(Player p, long time){
//			ParticleEffect.EXPLOSION_NORMAL.display(1F, .2F, 1F, .1F, 50, p.getLocation().add(0,1,0), 30);
			p.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, p.getLocation(), 50, 1, .2, 1, .1, 0);
		}
	},
	ADMIN679(){
		ParticleComboText text = new ParticleComboText(ParticleEffect.FLAME, "Admin");
		@Override public void display(Player p, long time){
			if(time%8 == 0) text.display(p.getLocation().add(0,3.5,0), 30);
		}
	},
	BEACON(){
		@Override public void display(Player p, long time){
//			ParticleEffect.REDSTONE.display(.1F, 15F, .1F, 1, 1000, p.getLocation().add(0,40,0), 100);
			if(time%2 == 0) p.getWorld().spawnParticle(Particle.REDSTONE, p.getLocation().add(0,40,0), 1000, .1, 15, .1, 1);
		}
	},
	IMAGINE_(){
		@Override public void display(Player p, long time){
//			ParticleEffect.NOTE.display(10F, 10F, 10F, 1, 10, p.getLocation(), p);
			if(time%2 == 0) p.getWorld().spawnParticle(Particle.NOTE, p.getLocation(), 10, 10, 10, 10, 1);
		}
	},
	/*TEXASFLAG(){
		ParticleComboTexasFlag flag = new ParticleComboTexasFlag();
		@Override public void display(Player p, long time){
			if(time%50 == 0) flag.display(p.getEyeLocation(), 250);
		}
	},*/
	BLIZZARD(){
		@Override public void display(Player p, long time){
			Location loc = p.getLocation(); loc.setY(loc.getY()+7);
//			ParticleEffect.SNOW_SHOVEL.display(10F, 7F, 10F, .1F, 800, loc, p);
//			ParticleEffect.SNOWBALL.display(10F, 7F, 10F, 0, 500, loc, p);
//			ParticleEffect.FIREWORKS_SPARK.display(10F, 7F, 10F, 0, 10, loc, p);
//			ParticleEffect.EXPLOSION_NORMAL.display(10F, 7F, 10F, .5F, 1000, loc, p);
			p.getWorld().spawnParticle(Particle.SNOW_SHOVEL, loc, 800, 10, 7, 10, .1);
			p.getWorld().spawnParticle(Particle.SNOWBALL, loc, 500, 10, 7, 10);
			p.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 10, 10, 7, 10);
			p.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 1000, 10, 7, 10, .5);
			loc.setY(loc.getY()-6.5);
//			ParticleEffect.EXPLOSION_NORMAL.display(2F, .5F, 2F, .3F, 30, loc, p);
			p.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 30, 2, .5, 2, .3);
		}
	};
	Random rand = new Random();

	abstract public void display(Player p, long time);
}